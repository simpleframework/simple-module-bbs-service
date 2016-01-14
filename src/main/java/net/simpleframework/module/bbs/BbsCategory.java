package net.simpleframework.module.bbs;

import java.util.Date;

import net.simpleframework.ado.bean.IDateAwareBean;
import net.simpleframework.ado.bean.INameBeanAware;
import net.simpleframework.ado.db.common.EntityInterceptor;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.module.common.content.AbstractCategoryBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@EntityInterceptor(listenerTypes = { "net.simpleframework.module.log.EntityDeleteLogAdapter" })
public class BbsCategory extends AbstractCategoryBean implements INameBeanAware, IDateAwareBean {

	/* 名称或编码，唯一 */
	private String name;

	/* 显示图标css */
	private String iconClass;

	/* 主题数 */
	private int topics;
	/* 跟贴数 */
	private int posts;

	/* 最后一次提交的主题、回帖 */
	private ID lastTopicId, lastPostId;

	/* 创建人 */
	private ID userId;
	/* 创建日期 */
	private Date createDate;

	@Override
	public String getName() {
		return name != null ? name.trim() : null;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

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

	public String getIconClass() {
		return StringUtils.text(iconClass, "icon_forum");
	}

	public void setIconClass(final String iconClass) {
		this.iconClass = iconClass;
	}

	public ID getUserId() {
		return userId;
	}

	public void setUserId(final ID userId) {
		this.userId = userId;
	}

	@Override
	public Date getCreateDate() {
		if (createDate == null) {
			createDate = new Date();
		}
		return createDate;
	}

	@Override
	public void setCreateDate(final Date createDate) {
		this.createDate = createDate;
	}

	private static final long serialVersionUID = -3233077947300876730L;
}
