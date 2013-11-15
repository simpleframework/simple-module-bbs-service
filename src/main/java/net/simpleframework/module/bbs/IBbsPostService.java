package net.simpleframework.module.bbs;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ctx.common.bean.TimePeriod;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IBbsPostService extends IDbBeanService<BbsPost> {

	/**
	 * 查询指定主题的帖子
	 * 
	 * @param topic
	 * @param timePeriod
	 * @return
	 */
	IDataQuery<BbsPost> query(BbsTopic topic, TimePeriod timePeriod);

	IDataQuery<BbsPost> query(BbsTopic topic);

	IDataQuery<BbsPost> queryWithASC(BbsTopic topic);

	/**
	 * 查询指定回帖的所有回复
	 * 
	 * @param topic
	 * @param replyId
	 * @return
	 */
	IDataQuery<BbsPost> queryByReplies(BbsTopic topic, Object replyId);

	IDataQuery<BbsPost> queryByUser(BbsTopic topic, Object userId);
}
