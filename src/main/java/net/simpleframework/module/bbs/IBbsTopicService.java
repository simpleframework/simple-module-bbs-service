package net.simpleframework.module.bbs;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.ctx.common.bean.TimePeriod;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.common.content.EContentStatus;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface IBbsTopicService extends IDbBeanService<BbsTopic> {

	/**
	 * 按条件过滤
	 * 
	 * @param category
	 * @param status
	 * @param timePeriod
	 * @param filterItems
	 * @param orderColumns
	 * @return
	 */
	IDataQuery<BbsTopic> query(BbsCategory category, EContentStatus status, TimePeriod timePeriod,
			FilterItems filterItems, ColumnData... orderColumns);

	IDataQuery<BbsTopic> query(BbsCategory category, EContentStatus status, TimePeriod timePeriod,
			FilterItems filterItems);

	IDataQuery<BbsTopic> queryTopics(BbsCategory category);

	IDataQuery<BbsTopic> queryTopics(BbsCategory category, TimePeriod timePeriod,
			ColumnData... orderColumns);

	/**
	 * 查询我的主题
	 * 
	 * @param user
	 * @return
	 */
	IDataQuery<BbsTopic> queryMyTopics(Object user);

	IDataQuery<BbsTopic> queryRecommendationTopics(BbsCategory category, TimePeriod timePeriod);

	/**
	 * 获取全文检索服务
	 * 
	 * @return
	 */
	ILuceneManager getLuceneService();
}
