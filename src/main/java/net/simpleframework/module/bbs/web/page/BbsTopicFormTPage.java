package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.template.struct.NavigationButtons;
import net.simpleframework.mvc.template.struct.Pagelets;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsTopicFormTPage extends AbstractBbsTPage implements IBbsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 类目选择
		addCategoryDict(pp);

		// PageletTab
		addPageletTabAjaxRequest(pp);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='BbsTopicFormTPage'>");
		sb.append(includeForm(pp));
		sb.append("</div>");
		return sb.toString();
	}

	protected String includeForm(final PageParameter pp) {
		return pp.includeUrl(BbsTopicForm.class);
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final NavigationButtons btns = NavigationButtons.of(new LinkElement(bbsContext.getModule())
				.setHref(getUrlsFactory().getUrl(pp, BbsCategoryPage.class)));
		final BbsCategory category = BbsPostViewTPage.getCategory(pp);
		if (category != null) {
			btns.append(new SpanElement().addElements(
					new LinkElement(category.getText())
							.setHref(getUrlsFactory().getUrl(pp, BbsTopicListPage.class, category)),
					createCategoryDictMenu(pp)));
		}
		return btns.append(new SpanElement($m("BbsTopicFormTPage.7")));
	}

	@Override
	protected TabButtons getCategoryTabs(final PageParameter pp) {
		return null;
	}

	public IForward doPageletTab(final ComponentParameter cp) {
		return singleton(BbsCategoryTPage.class).doPageletTab(cp);
	}

	@Override
	protected Pagelets getPagelets(final PageParameter pp) {
		return singleton(BbsCategoryTPage.class).getPagelets(pp);
	}
}
