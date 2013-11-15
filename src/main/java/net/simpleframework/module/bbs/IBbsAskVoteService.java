package net.simpleframework.module.bbs;

import net.simpleframework.ctx.service.ado.db.IDbBeanService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IBbsAskVoteService extends IDbBeanService<BbsAskVote> {

	/**
	 * 获取投票
	 * 
	 * @param post
	 * @param userId
	 * @return
	 */
	BbsAskVote getAskVote(BbsPost post, Object userId);

	/**
	 * 插入投票
	 * 
	 * @param post
	 * @param userId
	 * @param description
	 */
	void insertVote(BbsPost post, Object userId, String description);

	/**
	 * 删除投票
	 * 
	 * @param post
	 * @param userId
	 */
	void deleteVote(BbsPost post, Object userId);
}
