package net.simpleframework.module.bbs.impl;

import java.util.Date;

import net.simpleframework.common.ID;
import net.simpleframework.module.bbs.BbsAskVote;
import net.simpleframework.module.bbs.BbsPost;
import net.simpleframework.module.bbs.IBbsAskVoteService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsAskVoteService extends AbstractBbsService<BbsAskVote>
		implements IBbsAskVoteService {

	@Override
	public BbsAskVote getAskVote(final BbsPost post, final Object userId) {
		if (post == null || userId == null) {
			return null;
		}
		return getBean("postId=? and userId=?", post.getId(), userId);
	}

	@Override
	public void deleteVote(final BbsPost post, final Object userId) {
		final BbsAskVote vote = getAskVote(post, userId);
		if (vote != null) {
			delete(vote.getId());
			post.setVotes(post.getVotes() - 1);
			bbsContext.getPostService().update(new String[] { "votes" }, post);
		}
	}

	@Override
	public void insertVote(final BbsPost post, final Object userId, final String description) {
		if (post == null || userId == null) {
			return;
		}
		final BbsAskVote vote = createBean();
		vote.setPostId(post.getId());
		vote.setCreateDate(new Date());
		vote.setUserId(ID.of(userId));
		vote.setDescription(description);
		insert(vote);
		post.setVotes(post.getVotes() + 1);
		bbsContext.getPostService().update(new String[] { "votes" }, post);
	}
}