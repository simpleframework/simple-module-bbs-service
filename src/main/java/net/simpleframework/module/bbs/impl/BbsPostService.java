package net.simpleframework.module.bbs.impl;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.EOrder;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.IParamsValue;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.ctx.service.ado.db.AbstractDbBeanService;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsPost;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.EBbsType;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.IBbsPostService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsPostService extends AbstractDbBeanService<BbsPost> implements IBbsPostService,
		IBbsContextAware {

	private ColumnData[] getOrders(final BbsTopic topic, final boolean asc) {
		final ColumnData[] orders = topic.getBbsType() == EBbsType.ask ? new ColumnData[] {
				new ColumnData("bestAnswer", EOrder.desc),
				new ColumnData("votes", asc ? EOrder.asc : EOrder.desc),
				new ColumnData("createDate", EOrder.desc) } : new ColumnData[] { new ColumnData(
				"createDate", asc ? EOrder.asc : EOrder.desc) };
		return orders;
	}

	private IDataQuery<BbsPost> query(final BbsTopic topic, final TimePeriod timePeriod,
			final boolean asc) {
		final FilterItems filterItems = FilterItems.of().addEqualItem("contentId", topic.getId())
				.addEqualItem("createdate", timePeriod);
		return queryByParams(filterItems, getOrders(topic, asc));
	}

	@Override
	public IDataQuery<BbsPost> query(final BbsTopic topic, final TimePeriod timePeriod) {
		return query(topic, timePeriod, false);
	}

	@Override
	public IDataQuery<BbsPost> query(final BbsTopic topic) {
		return query(topic, null);
	}

	@Override
	public IDataQuery<BbsPost> queryWithASC(final BbsTopic topic) {
		return query(topic, null, true);
	}

	@Override
	public IDataQuery<BbsPost> queryByReplies(final BbsTopic topic, final Object replyId) {
		final FilterItems filterItems = FilterItems.of().addEqualItem("contentId", topic.getId())
				.addEqualItem("replyId", replyId);
		return queryByParams(filterItems, getOrders(topic, false));
	}

	@Override
	public IDataQuery<BbsPost> queryByUser(final BbsTopic topic, final Object userId) {
		final FilterItems filterItems = FilterItems.of().addEqualItem("contentId", topic.getId())
				.addEqualItem("userId", userId);
		return queryByParams(filterItems, getOrders(topic, false));
	}

	@Override
	public void doBestAnswer(final BbsPost post) {
		final IDataQuery<BbsPost> dq = query("contentId=? and bestAnswer=?", post.getContentId(),
				true);
		BbsPost _post;
		while ((_post = dq.next()) != null) {
			_post.setBestAnswer(false);
			update(new String[] { "bestAnswer" }, _post);
		}
		post.setBestAnswer(true);
		update(new String[] { "bestAnswer" }, post);
	}

	@Override
	public void onInit() throws Exception {
		final BbsCategoryService cService = (BbsCategoryService) context.getCategoryService();
		final BbsTopicService tService = (BbsTopicService) context.getTopicService();
		final BbsUserStatService uService = (BbsUserStatService) context.getUserStatService();

		addListener(new DbEntityAdapterEx() {
			@Override
			public void onAfterInsert(final IDbEntityManager<?> manager, final Object[] beans) {
				super.onAfterInsert(manager, beans);
				for (final Object o : beans) {
					final BbsPost post = (BbsPost) o;
					final BbsTopic topic = tService.getBean(post.getContentId());
					if (topic != null) {
						topic.setPosts(query(topic).getCount());
						topic.setLastPostDate(post.getCreateDate());
						topic.setLastUserId(post.getUserId());
						tService.update(new String[] { "posts", "lastPostDate", "lastUserId" }, topic);
					}
					final BbsPost reply = getBean(post.getReplyId());
					if (reply != null) {
						reply.setReplies(count("replyId=?", reply.getId()));
						update(new String[] { "replies" }, reply);
					}
				}
			}

			@Override
			public void onBeforeDelete(final IDbEntityManager<?> manager,
					final IParamsValue paramsValue) {
				super.onBeforeDelete(manager, paramsValue);
				for (final BbsPost post : coll(paramsValue)) {
					final BbsTopic topic = tService.getBean(post.getContentId());
					if (topic != null) {
						topic.setPosts(query(topic).getCount() - 1);
						tService.update(new String[] { "posts" }, topic);
					}
				}
			}
		});

		addListener(new DbEntityAdapterEx() {
			@Override
			public void onAfterInsert(final IDbEntityManager<?> manager, final Object[] beans) {
				super.onAfterInsert(manager, beans);
				for (final Object o : beans) {
					final BbsPost post = (BbsPost) o;
					final BbsTopic topic = tService.getBean(post.getContentId());
					BbsCategory category;
					if (topic != null && (category = cService.getBean(topic.getCategoryId())) != null) {
						category.setPosts(tService.sum("posts", "categoryId=?", category.getId()));
						category.setLastPostId(post.getId());
						cService.update(new String[] { "posts", "lastPostId" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setPosts(count("userId=?", post.getUserId()));
					stat.setLastPostId(post.getId());
					uService.update(new String[] { "posts", "lastPostId" }, stat);
				}
			}

			@Override
			public void onAfterDelete(final IDbEntityManager<?> manager, final IParamsValue paramsValue) {
				super.onAfterDelete(manager, paramsValue);
				for (final BbsPost post : coll(paramsValue)) {
					final BbsTopic topic = tService.getBean(post.getContentId());
					BbsCategory category;
					if (topic != null && (category = cService.getBean(topic.getCategoryId())) != null) {
						category.setPosts(tService.sum("posts", "categoryId=?", category.getId()));
						cService.update(new String[] { "posts" }, category);
					}

					final BbsUserStat stat = uService.getUserStat(topic.getUserId());
					stat.setPosts(count("userId=?", post.getUserId()));
					uService.update(new String[] { "posts" }, stat);
				}
			}
		});
	}
}