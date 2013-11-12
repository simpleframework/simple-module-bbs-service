package net.simpleframework.module.bbs.impl;

import static net.simpleframework.common.I18n.$m;

import java.io.File;
import java.io.IOException;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.EFilterRelation;
import net.simpleframework.ado.EOrder;
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
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.ctx.common.bean.TimePeriod;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.ctx.task.ExecutorRunnable;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.common.content.EContentStatus;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class BbsTopicService extends AbstractBbsService<BbsTopic> implements IBbsTopicService {

	static ColumnData[] DEFAULT_ORDER = new ColumnData[] {
			new ColumnData("recommendation", EOrder.desc),
			new ColumnData("lastpostdate", EOrder.desc), new ColumnData("createdate", EOrder.desc) };

	@Override
	public IDataQuery<BbsTopic> queryByParams(final FilterItems params) {
		return queryByParams(params, DEFAULT_ORDER);
	}

	@Override
	public IDataQuery<BbsTopic> query(final BbsCategory category, final EContentStatus status,
			final TimePeriod timePeriod, FilterItems filterItems, final ColumnData... orderColumns) {
		if (filterItems == null) {
			filterItems = FilterItems.of();
		}

		filterItems.addEqualItem("categoryId", category).addEqualItem("createdate", timePeriod);

		if (status != null) {
			filterItems.addEqualItem("status", status);
		} else {
			filterItems
					.add(new FilterItem("status", EFilterRelation.not_equal, EContentStatus.delete));
		}

		return queryByParams(filterItems,
				(orderColumns == null || orderColumns.length == 0) ? DEFAULT_ORDER : orderColumns);
	}

	@Override
	public IDataQuery<BbsTopic> query(final BbsCategory category, final EContentStatus status,
			final TimePeriod timePeriod, final FilterItems filterItems) {
		return query(category, status, timePeriod, filterItems, DEFAULT_ORDER);
	}

	@Override
	public IDataQuery<BbsTopic> queryTopics(final BbsCategory category) {
		return queryTopics(category, (TimePeriod) null, DEFAULT_ORDER);
	}

	@Override
	public IDataQuery<BbsTopic> queryTopics(final BbsCategory category, final TimePeriod timePeriod,
			final ColumnData... orderColumns) {
		return query(category, EContentStatus.publish, timePeriod, null, orderColumns);
	}

	@Override
	public IDataQuery<BbsTopic> queryRecommendationTopics(final BbsCategory category,
			final TimePeriod timePeriod) {
		return query(category, EContentStatus.publish, timePeriod,
				FilterItems.of(new FilterItem("recommendation", EFilterRelation.gt, 0)));
	}

	@Override
	public IDataQuery<BbsTopic> queryMyTopics(final Object user) {
		return query(
				null,
				null,
				null,
				FilterItems.of().addEqualItem("userId",
						(user instanceof PermissionUser) ? ((PermissionUser) user).getId() : user));
	}

	private String tmpdir;

	protected String getTmpdir() {
		if (tmpdir == null) {
			final StringBuilder sb = new StringBuilder();
			final String fs = File.separator;
			sb.append(getModuleContext().getContextSettings().getTmpFiledir().getAbsolutePath());
			sb.append(fs).append("bbs").append(fs);
			tmpdir = sb.toString();
		}
		return tmpdir;
	}

	BbsTopicLuceneService luceneService;

	@Override
	public ILuceneManager getLuceneService() {
		return luceneService;
	}

	@Override
	public void onInit() throws Exception {
		luceneService = new BbsTopicLuceneService(new File(getTmpdir() + "index"));
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

		final BbsCategoryService cService = getCategoryService();
		final BbsUserStatService uService = getUserStatService();

		addListener(new DbEntityAdapterEx() {
			@Override
			public void onBeforeDelete(final IDbEntityManager<?> manager,
					final IParamsValue paramsValue) {
				super.onBeforeDelete(manager, paramsValue);
				final BbsPostService pService = getPostService();
				final BbsAttachmentService aService = getAttachmentService();
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
						category.setTopics(queryTopics(category).getCount());
						category.setLastTopicId(topic.getId());
						cService.update(new String[] { "topics", "lastTopicId" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setTopics(queryMyTopics(topic.getUserId()).getCount());
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
						category.setTopics(queryTopics(category).getCount());
						cService.update(new String[] { "topics" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setTopics(queryMyTopics(topic.getUserId()).getCount());
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
			return context.getTopicService().getBean(doc.get("id"));
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
