package net.simpleframework.module.bbs;

import net.simpleframework.ado.bean.AbstractIdBean;
import net.simpleframework.ado.db.DbEntityTable;
import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsUserStat extends AbstractIdBean {

	/* 主题数 */
	private int topics;

	/* 跟贴数 */
	private int posts;

	/* 最后一次提交的主题、回帖 */
	private ID lastTopicId, lastPostId;

	public int getTopics() {
		return topics;
	}

	public void setTopics(final int topics) {
		this.topics = topics;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(final int posts) {
		this.posts = posts;
	}

	public ID getLastTopicId() {
		return lastTopicId;
	}

	public void setLastTopicId(final ID lastTopicId) {
		this.lastTopicId = lastTopicId;
	}

	public ID getLastPostId() {
		return lastPostId;
	}

	public void setLastPostId(final ID lastPostId) {
		this.lastPostId = lastPostId;
	}

	public static DbEntityTable TBL = new DbEntityTable(BbsUserStat.class, "sf_bbs_user");

	private static final long serialVersionUID = -5682240153954667540L;
}
