package net.simpleframework.module.bbs;

import net.simpleframework.ado.bean.AbstractUserAwareBean;
import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsPostLikeLog extends AbstractUserAwareBean {

	private ID topicId;

	private ID postId;

	public ID getTopicId() {
		return topicId;
	}

	public void setTopicId(final ID topicId) {
		this.topicId = topicId;
	}

	public ID getPostId() {
		return postId;
	}

	public void setPostId(final ID postId) {
		this.postId = postId;
	}

	private static final long serialVersionUID = -7009267410621666286L;
}
