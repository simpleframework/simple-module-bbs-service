package net.simpleframework.module.bbs.web.page.t2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.web.page.BbsCategoryTPage;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.t2.T2TemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/bbs/category")
public class BbsCategoryPage extends T2TemplatePage implements IBbsContextAware {

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		return pp.includeUrl(BbsCategoryTPage.class);
	}

	@Override
	public String getTitle(final PageParameter pp) {
		return bbsContext.getModule().getText();
	}
}
