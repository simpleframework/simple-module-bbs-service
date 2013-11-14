package net.simpleframework.module.bbs;

import net.simpleframework.ado.lucene.ILuceneManager;
import net.simpleframework.module.common.content.IContentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface IBbsTopicService extends IContentService<BbsTopic> {

	/**
	 * 获取全文检索服务
	 * 
	 * @return
	 */
	ILuceneManager getLuceneService();
}
