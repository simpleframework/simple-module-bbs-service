package net.simpleframework.module.bbs.impl;

import static net.simpleframework.common.I18n.$m;

import java.io.File;
import java.io.IOException;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.EFilterRelation;
import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.IParamsValue;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.ado.lucene.AbstractLuceneManager;
import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.ado.lucene.LuceneDocument;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.ctx.task.ExecutorRunnable;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.common.content.AbstractCategoryBean;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.content.impl.AbstractContentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsTopicService extends AbstractContentService<BbsTopic> implements IBbsTopicService,
		IBbsContextAware {

	static ColumnData[] DEFAULT_ORDER = new ColumnData[] { ColumnData.DESC("recommendation"),
			ColumnData.DESC("lastpostdate") };

	@Override
	protected ColumnData[] getDefaultOrderColumns() {
		return DEFAULT_ORDER;
	}

	@Override
	public IDataQuery<BbsTopic> queryBeans(final AbstractCategoryBean category,
			final EContentStatus status, final TimePeriod timePeriod, FilterItems filterItems,
			final ColumnData... orderColumns) {
		if (filterItems == null) {
			filterItems = FilterItems.of();
		}

		filterItems.addEqual("categoryId", category).addEqual("createdate", timePeriod);

		if (status != null) {
			filterItems.addEqual("status", status);
		} else {
			filterItems
					.add(new FilterItem("status", EFilterRelation.not_equal, EContentStatus.delete));
		}

		return queryByParams(filterItems,
				(orderColumns == null || orderColumns.length == 0) ? DEFAULT_ORDER : orderColumns);
	}

	BbsTopicLuceneService luceneService;

	@Override
	public ILuceneManager getLuceneService() {
		return luceneService;
	}

	@Override
	public void onInit() throws Exception {
		luceneService = new BbsTopicLuceneService(new File(context.getTmpdir() + "index"));
		if (!luceneService.indexExists()) {
			getModuleContext().getTaskExecutor().execute(new ExecutorRunnable() {
				@Override
				protected void task() throws Exception {
					log.info($m("BbsTopicService.0"));
					luceneService.rebuildIndex();
					log.info($m("BbsTopicService.1"));
				}
			});
		}

		final BbsCategoryService cService = (BbsCategoryService) context.getCategoryService();
		final BbsUserStatService uService = (BbsUserStatService) context.getUserStatService();

		addListener(new DbEntityAdapterEx() {
			@Override
			public void onBeforeDelete(final IDbEntityManager<?> manager,
					final IParamsValue paramsValue) {
				super.onBeforeDelete(manager, paramsValue);
				final BbsPostService pService = (BbsPostService) context.getPostService();
				final BbsAttachmentService aService = (BbsAttachmentService) context
						.getAttachmentService();
				for (final BbsTopic topic : coll(paramsValue)) {
					final ID id = topic.getId();
					// 帖子
					pService.deleteWith("contentId=?", id);
					// 附件
					aService.deleteWith("contentId=?", id);
				}
			}

			@Override
			public void onAfterInsert(final IDbEntityManager<?> manager, final Object[] beans) {
				super.onAfterInsert(manager, beans);
				for (final Object o : beans) {
					final BbsTopic topic = (BbsTopic) o;
					final BbsCategory category = cService.getBean(topic.getCategoryId());
					if (category != null) {
						category.setTopics(queryBeans(category).getCount());
						category.setLastTopicId(topic.getId());
						cService.update(new String[] { "topics", "lastTopicId" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setTopics(queryMyBeans(topic.getUserId()).getCount());
					stat.setLastTopicId(topic.getId());
					uService.update(new String[] { "topics", "lastTopicId" }, stat);
				}

				// 添加索引
				luceneService.doAddIndex(beans);
			}

			@Override
			public void onAfterDelete(final IDbEntityManager<?> manager, final IParamsValue paramsValue) {
				super.onAfterDelete(manager, paramsValue);
				for (final BbsTopic topic : coll(paramsValue)) {
					final BbsCategory category = cService.getBean(topic.getCategoryId());
					if (category != null) {
						category.setTopics(queryBeans(category).getCount());
						cService.update(new String[] { "topics" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setTopics(queryMyBeans(topic.getUserId()).getCount());
					uService.update(new String[] { "topics" }, stat);

					// 删除索引
					luceneService.doDeleteIndex(topic);
				}
			}

			@Override
			public void onAfterUpdate(final IDbEntityManager<?> manager, final String[] columns,
					final Object[] beans) {
				super.onAfterUpdate(manager, columns, beans);

				// 更新索引
				if (columns == null || columns.length == 0 || ArrayUtils.contains(columns, "topic")
						|| ArrayUtils.contains(columns, "content")) {
					luceneService.doUpdateIndex(beans);
				}
			}
		});
	}

	static class BbsTopicLuceneService extends AbstractLuceneManager {

		public BbsTopicLuceneService(final File indexPath) {
			super(indexPath, new String[] { "id", "topic", "content" });
		}

		@Override
		protected Object documentToObject(final LuceneDocument doc, final Class<?> beanClass) {
			final BbsTopic topic = context.getTopicService().getBean(doc.get("id"));
			return topic != null && topic.getStatus() == EContentStatus.publish ? topic : null;
		}

		@Override
		protected IDataQuery<?> queryAll() {
			return context.getTopicService().queryAll();
		}

		@Override
		protected void objectToDocument(final Object object, final LuceneDocument doc)
				throws IOException {
			super.objectToDocument(object, doc);
			final BbsTopic topic = (BbsTopic) object;
			doc.addTextField("topic", topic.getTopic(), false);
			String content = topic.getDescription();
			if (!StringUtils.hasText(content)) {
				content = trimContent(topic.getContent());
			}
			doc.addTextField("content", content, false);
		}
	}
}
