package net.simpleframework.module.bbs.web;

import net.simpleframework.common.StringUtils;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.module.bbs.web.page.t2.BbsPostViewPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicFormPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.UrlsCache;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsUrlsFactory extends UrlsCache {

	public BbsUrlsFactory() {
		put(BbsCategoryPage.class);
		put(BbsTopicListPage.class);
		put(BbsTopicFormPage.class);
		put(BbsPostViewPage.class);
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final BbsCategory category) {
		return getUrl(pp, mClass, category, null);
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final BbsCategory category, final String params) {
		return getUrl(pp, mClass, StringUtils.join(
				new String[] { category != null ? "categoryId=" + category.getId() : null, params },
				"&"));
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final BbsTopic topic) {
		return getUrl(pp, mClass, topic, null);
	}

	public String getUrl(final PageParameter pp, final Class<? extends AbstractMVCPage> mClass,
			final BbsTopic topic, final String params) {
		return getUrl(pp, mClass, StringUtils
				.join(new String[] { topic != null ? "topicId=" + topic.getId() : null, params }, "&"));
	}
}
