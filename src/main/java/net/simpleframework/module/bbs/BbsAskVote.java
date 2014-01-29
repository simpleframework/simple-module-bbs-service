package net.simpleframework.module.bbs;

import net.simpleframework.ado.bean.AbstractUserAwareBean;
import net.simpleframework.ado.bean.IDescriptionBeanAware;
import net.simpleframework.common.ID;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsAskVote extends AbstractUserAwareBean implements IDescriptionBeanAware {

	private ID postId;

	private String description;

	public ID getPostId() {
		return postId;
	}

	public void setPostId(final ID postId) {
		this.postId = postId;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

	private static final long serialVersionUID = 3847737582791563719L;
}
