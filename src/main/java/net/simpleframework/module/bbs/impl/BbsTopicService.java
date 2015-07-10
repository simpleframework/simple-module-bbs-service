package net.simpleframework.module.bbs.impl;

import static net.simpleframework.common.I18n.$m;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.IParamsValue;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.ado.lucene.AbstractLuceneManager;
import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.ado.lucene.LuceneDocument;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.BeanUtils;
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
import net.simpleframework.module.common.content.impl.AbstractRecommendContentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsTopicService extends AbstractRecommendContentService<BbsTopic> implements
		IBbsTopicService, IBbsContextAware {

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

		if (category != null) {
			filterItems.addEqual("categoryId", category);
		}
		if (timePeriod != null) {
			filterItems.addEqual("createdate", timePeriod);
		}

		if (status != null) {
			filterItems.addEqual("status", status);
		} else {
			filterItems.addNotEqual("status", EContentStatus.delete);
		}

		return queryByParams(filterItems,
				(orderColumns == null || orderColumns.length == 0) ? DEFAULT_ORDER : orderColumns);
	}

	private BbsTopicLuceneService luceneService;

	@Override
	public ILuceneManager getLuceneService() {
		return luceneService;
	}

	@Override
	public void onInit() throws Exception {
		super.onInit();

		luceneService = new BbsTopicLuceneService();
		if (!luceneService.indexExists()) {
			getModuleContext().getTaskExecutor().execute(new ExecutorRunnable() {
				@Override
				protected void task(final Map<String, Object> cache) throws Exception {
					getLog().info($m("BbsTopicService.0"));
					luceneService.rebuildIndex();
					getLog().info($m("BbsTopicService.1"));
				}
			});
		}

		final BbsCategoryService cService = (BbsCategoryService) bbsContext.getCategoryService();
		final BbsUserStatService uService = (BbsUserStatService) bbsContext.getUserStatService();

		addListener(new DbEntityAdapterEx() {
			@Override
			public void onBeforeDelete(final IDbEntityManager<?> manager,
					final IParamsValue paramsValue) throws Exception {
				super.onBeforeDelete(manager, paramsValue);
				final BbsPostService pService = (BbsPostService) bbsContext.getPostService();
				final BbsAttachmentService aService = (BbsAttachmentService) bbsContext
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
			public void onAfterInsert(final IDbEntityManager<?> manager, final Object[] beans)
					throws Exception {
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
					stat.setTopics(queryByUser(topic.getUserId()).getCount());
					stat.setLastTopicId(topic.getId());
					uService.update(new String[] { "topics", "lastTopicId" }, stat);
				}

				// 添加索引
				luceneService.doAddIndex(beans);
			}

			@Override
			public void onAfterDelete(final IDbEntityManager<?> manager, final IParamsValue paramsValue)
					throws Exception {
				super.onAfterDelete(manager, paramsValue);
				for (final BbsTopic topic : coll(paramsValue)) {
					final BbsCategory category = cService.getBean(topic.getCategoryId());
					if (category != null) {
						category.setTopics(queryBeans(category).getCount());
						cService.update(new String[] { "topics" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setTopics(queryByUser(topic.getUserId()).getCount());
					uService.update(new String[] { "topics" }, stat);

					// 删除索引
					luceneService.doDeleteIndex(topic);
				}
			}

			@Override
			public void onAfterUpdate(final IDbEntityManager<?> manager, final String[] columns,
					final Object[] beans) throws Exception {
				super.onAfterUpdate(manager, columns, beans);

				// 更新索引
				if (ArrayUtils.isEmpty(columns) || ArrayUtils.contains(columns, "topic", true)
						|| ArrayUtils.contains(columns, "content", true)) {
					luceneService.doUpdateIndex(beans);
				}
			}
		});
	}

	protected class BbsTopicLuceneService extends AbstractLuceneManager {

		public BbsTopicLuceneService() {
			super(new File(bbsContext.getTmpdir() + "index"));
		}

		@Override
		protected String[] getQueryFields() {
			return new String[] { "id", "topic", "content" };
		}

		@Override
		protected Object documentToObject(final LuceneDocument doc, final Class<?> beanClass) {
			Object obj;
			if (beanClass == null) {
				obj = super.documentToObject(doc, beanClass);
			} else {
				obj = getBean(doc.get("id"));
			}
			return (obj != null && BeanUtils.getProperty(obj, "status") == EContentStatus.publish) ? obj
					: null;
		}

		@Override
		protected IDataQuery<?> queryAll() {
			return bbsContext.getTopicService().queryAll();
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
