package net.simpleframework.module.bbs;

import net.simpleframework.ado.ColumnMeta;
import net.simpleframework.ado.bean.ITreeBeanAware;
import net.simpleframework.ado.db.DbEntityTable;
import net.simpleframework.ado.db.common.EntityInterceptor;
import net.simpleframework.common.ID;
import net.simpleframework.module.common.content.AbstractComment;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@EntityInterceptor(listenerTypes = { "net.simpleframework.module.log.EntityUpdateLogAdapter",
		"net.simpleframework.module.log.EntityDeleteLogAdapter" }, columns = { "bestAnswer" })
public class BbsPost extends AbstractComment implements ITreeBeanAware {

	/* 多级，帖子的评论，帖子的parentId为null */
	private ID parentId;

	/* 回复引用的帖子 */
	private ID replyId;

	/* 跟贴的回复数量(统计) */
	private int replies;

	/* 投票的人数(统计) */
	private int votes;

	/* 最佳答案 */
	@ColumnMeta(columnText = "#(BbsPost.0)")
	private boolean bestAnswer;

	@Override
	public ID getParentId() {
		return parentId;
	}

	@Override
	public void setParentId(final ID parentId) {
		this.parentId = parentId;
	}

	public ID getReplyId() {
		return replyId;
	}

	public void setReplyId(final ID replyId) {
		this.replyId = replyId;
	}

	public int getReplies() {
		return replies;
	}

	public void setReplies(final int replies) {
		this.replies = replies;
	}

	public int getVotes() {
		return votes;
	}

	public void setVotes(final int votes) {
		this.votes = votes;
	}

	public boolean isBestAnswer() {
		return bestAnswer;
	}

	public void setBestAnswer(final boolean bestAnswer) {
		this.bestAnswer = bestAnswer;
	}

	public static DbEntityTable TBL = new DbEntityTable(BbsPost.class, "sf_bbs_post");

	private static final long serialVersionUID = -1147517820464806915L;
}
