package net.simpleframework.module.bbs;

import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.module.common.content.AbstractCategoryBean;
import net.simpleframework.module.common.content.IContentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IBbsTopicService extends IContentService<BbsTopic> {

	/**
	 * 获取全文检索服务
	 * 
	 * @return
	 */
	ILuceneManager getLuceneService();

	/**
	 * 查询推荐内容
	 * 
	 * @param category
	 * @param timePeriod
	 * @return
	 */
	IDataQuery<BbsTopic> queryRecommendationBeans(AbstractCategoryBean category,
			TimePeriod timePeriod);
}
