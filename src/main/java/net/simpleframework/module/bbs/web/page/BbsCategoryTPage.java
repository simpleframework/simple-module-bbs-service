package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.ColumnData;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.ETimePeriod;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.common.web.html.HtmlConst;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsPost;
import net.simpleframework.module.bbs.BbsTeam;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsTeamService;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.web.BbsPageletCreator;
import net.simpleframework.module.bbs.web.BbsUrlsFactory;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.TextForward;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.EElementEvent;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.menu.EMenuEvent;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.tooltip.ETipElement;
import net.simpleframework.mvc.component.ui.tooltip.ETipPosition;
import net.simpleframework.mvc.component.ui.tooltip.ETipStyle;
import net.simpleframework.mvc.component.ui.tooltip.TipBean;
import net.simpleframework.mvc.component.ui.tooltip.TipBean.HideOn;
import net.simpleframework.mvc.component.ui.tooltip.TipBean.Hook;
import net.simpleframework.mvc.component.ui.tooltip.TooltipBean;
import net.simpleframework.mvc.impl.DefaultPageResourceProvider;
import net.simpleframework.mvc.template.struct.CategoryItem;
import net.simpleframework.mvc.template.struct.EImageDot;
import net.simpleframework.mvc.template.struct.Pagelet;
import net.simpleframework.mvc.template.struct.Pagelets;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsCategoryTPage extends AbstractBbsTPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 菜单
		addTopicMenuBean(pp);

		// 高级搜索
		addSearchWindow(pp);

		// PageletTab
		addPageletTabAjaxRequest(pp);

		final TooltipBean tooltip = addComponentBean(pp, "BbsCategoryTPage_Tip", TooltipBean.class);
		tooltip.addTip(new TipBean(tooltip).setSelector(".BbsCategoryTPage .c_item").setDelay(0.5)
				.setTipStyle(ETipStyle.tipDarkgrey).setStem(ETipPosition.bottomMiddle)
				.setHook(new Hook(ETipPosition.topMiddle, ETipPosition.bottomMiddle))
				.setHideOn(new HideOn(ETipElement.tip, EElementEvent.mouseleave)).setWidth(300));
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final ElementList el = ElementList.of();
		el.appendAll(singleton(BbsTopicListTPage.class).getLeftElements(pp));

		if (pp.isLmember(getPageManagerRole(pp))) {
			// 管理菜单
			final MenuBean menu = (MenuBean) addComponentBean(pp, "BbsCategoryTPage_menu",
					MenuBean.class).setMenuEvent(EMenuEvent.click).setSelector("#menu_" + hashId);
			menu.addItem(MenuItem.of($m("BbsCategoryTPage.1"))
					.setOnclick("$Actions['BbsCategoryTPage_CategoryWin']();"));
			menu.addItem(MenuItem.sep());
			menu.addItem(MenuItem.of($m("BbsCategoryTPage.11"))
					.setOnclick("$Actions['BbsCategoryTPage_advWindow']();"));

			// 高级设置
			AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "BbsCategoryTPage_advPage",
					BbsAdvPage.class);
			addWindowBean(pp, "BbsCategoryTPage_advWindow", ajaxRequest)
					.setTitle($m("BbsCategoryTPage.11")).setHeight(280).setWidth(420);

			// 管理窗口
			ajaxRequest = addAjaxRequest(pp, "BbsCategoryTPage_CategoryPage", CategoryMgrPage.class);
			addWindowBean(pp, "BbsCategoryTPage_CategoryWin", ajaxRequest).setHeight(600).setWidth(480)
					.setTitle($m("BbsCategoryTPage.1"));

			el.append(SpanElement.SPACE, new LinkButton($m("BbsCategoryTPage.0"))
					.setId("menu_" + hashId).setMenuIcon(true).setIconClass(Icon.wrench));
		}
		return el;
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		return singleton(BbsTopicListTPage.class).getRightElements(pp);
	}

	@Override
	protected TabButtons getCategoryTabs(final PageParameter pp) {
		final BbsUrlsFactory uFactory = getUrlsFactory();
		final String url = uFactory.getUrl(pp, BbsTopicListPage.class, (BbsCategory) null);
		final TabButtons tabs = TabButtons.of(
				new TabButton($m("BbsCategoryTPage.5"), uFactory.getUrl(pp, BbsCategoryPage.class)),
				new TabButton($m("BbsCategoryTPage.6"), url),
				new TabButton($m("BbsTopic.0"), HttpUtils.addParameters(url, "list=best"))
						.setTabMatch(ETabMatch.params),
				new TabButton($m("BbsCategoryTPage.7"), HttpUtils.addParameters(url, "list=my"))
						.setTabMatch(ETabMatch.params));
		final PermissionUser user = pp.getUser(pp.getParameter("userId"));
		if (user.exists()) {
			tabs.append(new TabButton(user,
					uFactory.getUrl(pp, BbsTopicListPage.class, "userId=" + user.getId()))
							.setTabMatch(ETabMatch.params));
		}
		return tabs;
	}

	public IForward doPageletTab(final ComponentParameter cp) {
		final IBbsTopicService service = bbsContext.getTopicService();
		final BbsPageletCreator creator = ((IBbsWebContext) bbsContext).getPageletCreator();

		final ETimePeriod tp = Convert.toEnum(ETimePeriod.class, cp.getParameter("time"));

		final IDataQuery<?> dq;
		final String let = cp.getParameter("let");
		if ("recommendation".equals(let)) {
			dq = service.queryRecommendationBeans(null, new TimePeriod(tp));
		} else {
			dq = service.queryBeans(null, new TimePeriod(tp), ColumnData.DESC(let));
		}

		return new TextForward(
				cp.wrapHTMLContextPath(creator.create(cp, dq).setDotIcon(EImageDot.numDot).toString()));
	}

	@Override
	protected Pagelets getPagelets(final PageParameter pp) {
		final IBbsTopicService service = bbsContext.getTopicService();
		final BbsPageletCreator creator = ((IBbsWebContext) bbsContext).getPageletCreator();
		final Pagelets lets = Pagelets.of();

		// 推荐
		IDataQuery<?> dq = service.queryRecommendationBeans(null, TimePeriod.week);
		lets.add(new Pagelet(new CategoryItem($m("BbsCategoryTPage.10")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("let=recommendation")));

		// 按跟贴
		dq = service.queryBeans(null, TimePeriod.week, ColumnData.DESC("posts"));
		lets.add(new Pagelet(new CategoryItem($m("BbsCategoryTPage.8")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("let=posts")));

		// 按浏览次数
		dq = service.queryBeans(null, TimePeriod.week, ColumnData.DESC("views"));
		lets.add(new Pagelet(new CategoryItem($m("BbsCategoryTPage.9")),
				creator.create(pp, dq).setDotIcon(EImageDot.numDot))
						.setTabs(creator.createTimePeriodTabs("let=views")));

		// 历史记录
		lets.add(creator.getHistoryPagelet(pp));
		return lets;
	}

	protected String getItemStyles() {
		return "width: 220px; height: 80px";
	}

	protected String toItemHTML(final PageParameter pp, final BbsCategory category) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='icon_b ").append(category.getIconClass()).append("'></div>");
		final int topics = bbsContext.getTopicService()
				.queryBeans(category, new TimePeriod(ETimePeriod.day)).getCount();
		sb.append("<div class='l1'>");
		sb.append(new LinkElement(category.getText())
				.setHref(getUrlsFactory().getUrl(pp, BbsTopicListPage.class, category)));
		if (topics > 0) {
			sb.append(new SupElement(topics).setHighlight(true));
		}
		sb.append("</div>");

		sb.append("<div class='l2'>");
		sb.append($m("BbsCategoryTPage.2", SpanElement.num(category.getTopics()),
				SpanElement.num(category.getPosts())));
		sb.append("</div>");

		sb.append("<div class='l3'>").append($m("BbsCategoryTPage.3"));
		final BbsPost post = bbsContext.getPostService().getBean(category.getLastPostId());
		if (post != null) {
			sb.append(DateUtils.getRelativeDate(post.getCreateDate(), DATE_NUMBERCONVERT));
		} else {
			final BbsTopic topic = bbsContext.getTopicService().getBean(category.getLastTopicId());
			if (topic != null) {
				sb.append(DateUtils.getRelativeDate(topic.getCreateDate(), DATE_NUMBERCONVERT));
			} else {
				sb.append("?");
			}
		}
		sb.append("</div>");
		final StringBuilder ustr = new StringBuilder();
		final StringBuilder tstr = new StringBuilder();
		final IDataQuery<BbsTeam> dq = bbsContext.getTeamService().queryByOwner(category,
				IBbsTeamService.MANAGER);
		BbsTeam team;
		int i = 0;
		while ((team = dq.next()) != null) {
			if (i++ > 0) {
				tstr.append("; ");
				ustr.append("; ");
			}
			final PermissionUser user = pp.getUser(team.getUserId());
			tstr.append(user);
			ustr.append(createTopicsLink(pp, user));
		}
		sb.append("<div class='l4' title='").append(tstr).append("'>");
		sb.append($m("BbsCategoryTPage.4")).append(ustr.length() > 0 ? ustr : "?");
		sb.append("</div>");
		return sb.toString();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='BbsCategoryTPage'>");
		final IBbsCategoryService service = bbsContext.getCategoryService();
		final IDataQuery<BbsCategory> dq = service.queryChildren(null);
		BbsCategory category;
		while ((category = dq.next()) != null) {
			sb.append("<div class='c_title'>").append(category.getText());
			sb.append("<img src='")
					.append(pp.getCssResourceHomePath(DefaultPageResourceProvider.class))
					.append("/images/toggle.png' />");
			sb.append("</div>");
			final IDataQuery<BbsCategory> dq2 = service.queryChildren(category);
			if (dq2.getCount() > 0) {
				sb.append("<div class='c_list'>");
				BbsCategory category2;
				while ((category2 = dq2.next()) != null) {
					sb.append("<span class='c_item'");
					final String style = getItemStyles();
					if (StringUtils.hasText(style)) {
						sb.append(" style='").append(style).append("'");
					}
					sb.append(">").append(toItemHTML(pp, category2)).append("</span>");
					final String desc = category2.getDescription();
					if (StringUtils.hasText(desc)) {
						sb.append(BlockElement.tipText(desc));
					}
				}
				sb.append("</div>");
			}
		}
		sb.append("</div>");
		sb.append(HtmlConst.TAG_SCRIPT_START);
		sb.append("(function() {");
		sb.append(
				" $$('.BbsCategoryTPage .c_title img').each(function(img) { $UI.doImageToggle(img); });");
		sb.append("})();");
		sb.append(HtmlConst.TAG_SCRIPT_END);
		return sb.toString();
	}
}
