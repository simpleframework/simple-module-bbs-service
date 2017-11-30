package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.FilterItem;
import net.simpleframework.ado.FilterItems;
import net.simpleframework.ado.query.DataQueryUtils;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.DateUtils;
import net.simpleframework.common.ETimePeriod;
import net.simpleframework.common.ID;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.TimePeriod;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.permission.PermissionConst;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.EBbsType;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.web.BbsLogRef;
import net.simpleframework.module.bbs.web.BbsUtils;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.module.bbs.web.page.t2.BbsPostViewPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicFormPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.module.common.content.AbstractContentBean.EContentStatus;
import net.simpleframework.module.common.log.LdescVal;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETabMatch;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.PhotoImage;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SearchInput;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.SupElement;
import net.simpleframework.mvc.common.element.TabButton;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.menu.MenuItems;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.AdvSearchPage;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;
import net.simpleframework.mvc.template.struct.FilterButton;
import net.simpleframework.mvc.template.struct.FilterButtons;
import net.simpleframework.mvc.template.struct.NavigationButtons;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsTopicListTPage extends AbstractBbsTPage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 菜单
		addTopicMenuBean(pp);
		// 类目选择
		addCategoryDict(pp);
		// 高级搜索
		addSearchWindow(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp);
		if (BbsUtils.isManager(pp, getCategory(pp))) {
			tablePager.addColumn(TablePagerColumn.OPE(75).setResize(false));

			// 删除
			addDeleteAjaxRequest(pp, "BbsTopicListTPage_delete");
			// 推荐
			final AjaxRequestBean ajaxRequest = addAjaxRequest(pp,
					"BbsTopicListTPage_recommendationPage", BbsTopicRecommendationPage.class);
			addWindowBean(pp, "BbsTopicListTPage_recommendation", ajaxRequest).setHeight(270)
					.setWidth(460).setTitle($m("AbstractContentBean.2"));

			// topic属性
			addAjaxRequest(pp, "BbsTopicListTPage_advPage", TopicAdvPage.class);
			addComponentBean(pp, "BbsTopicListTPage_adv", WindowBean.class)
					.setContentRef("BbsTopicListTPage_advPage").setHeight(270).setWidth(460)
					.setTitle($m("BbsTopicListTPage.10"));

			// 修改日志
			final IModuleRef ref = ((IBbsWebContext) bbsContext).getLogRef();
			if (ref != null) {
				((BbsLogRef) ref).addLogComponent(pp);
			}
		}
	}

	@Override
	protected WindowBean addSearchWindow(final PageParameter pp) {
		return super.addSearchWindow(pp).setXdelta(-150);
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
				"BbsTopicListTPage_tbl", TablePagerBean.class).setFilter(false).setShowCheckbox(false)
						.setPageItems(50).setPagerBarLayout(EPagerBarLayout.bottom)
						.setContainerId("tbl_" + hashId).setHandlerClass(TopicList.class);
		tablePager.addColumn(TablePagerColumn.ICON())
				.addColumn(new TablePagerColumn("topic", $m("BbsTopicListTPage.0")).setResize(false)
						.setNowrap(false).setSort(false))
				.addColumn(new TablePagerColumn("userId", $m("BbsTopicListTPage.1")).setWidth(100)
						.setResize(false).setSort(false))
				.addColumn(new TablePagerColumn("posts", $m("BbsTopicListTPage.2")).setWidth(45)
						.setResize(false))
				.addColumn(new TablePagerColumn("views", $m("BbsTopicListTPage.3")).setWidth(45)
						.setResize(false))
				.addColumn(new TablePagerColumn("favorites", $m("BbsTopicListTPage.4")).setWidth(45)
						.setResize(false))
				.addColumn(new TablePagerColumn("lastPost", $m("BbsTopicListTPage.5")).setWidth(110)
						.setResize(false).setSort(false));
		return tablePager;
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		final String list = pp.getParameter("list");
		return "my".equals(list) ? PermissionConst.ROLE_ALL_ACCOUNT : PermissionConst.ROLE_ANONYMOUS;
	}

	@Transaction(context = IBbsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("topicId"));
		bbsContext.getTopicService().delete(ids);
		return new JavascriptForward("$Actions['BbsTopicListTPage_tbl']();");
	}

	@Override
	protected boolean isShowPagelets(final PageParameter pp) {
		return false;
	}

	@Override
	protected TabButtons getCategoryTabs(final PageParameter pp) {
		final IBbsCategoryService service = bbsContext.getCategoryService();
		final TabButtons tabs = TabButtons.of();
		final BbsCategory category = getCategory(pp);
		if (category != null) {
			final IDataQuery<BbsCategory> dq = service
					.queryChildren(service.getBean(category.getParentId()));
			BbsCategory tmp;
			int i = 0;
			while ((tmp = dq.next()) != null) {
				tabs.add(new TabButton(tmp, getUrlsFactory().getUrl(pp, BbsTopicListPage.class, tmp))
						.setTabMatch(ETabMatch.params));
				if (++i == 8) {
					break;
				}
			}
		} else {
			tabs.addAll(singleton(BbsCategoryTPage.class).getCategoryTabs(pp));
			addSearchTab(pp, tabs);
		}
		return tabs;
	}

	protected void addSearchTab(final PageParameter pp, final TabButtons btns) {
		final String t = pp.getParameter("s");
		if (StringUtils.hasText(t)) {
			btns.append(new TabButton($m("BbsTopicListTPage.9"), "#"))
					.setSelectedIndex(btns.size() - 1);
		}
	}

	@Override
	public FilterButtons getFilterButtons(final PageParameter pp) {
		final String list = pp.getParameter("list");
		final String url = getUrlsFactory().getUrl(pp, BbsTopicListPage.class, getCategory(pp),
				StringUtils.hasText(list) ? "list=" + list : null);
		final FilterButtons btns = FilterButtons.of();
		final BbsAdvSearchPage sPage = singleton(BbsAdvSearchPage.class);
		FilterButton btn = sPage.createFilterButton(pp, url, "as_topic");
		if (btn != null) {
			btns.add(btn.setLabel($m("BbsAdvSearchPage.0")));
		}
		btn = sPage.createFilterButton(pp, url, "as_author");
		if (btn != null) {
			btns.add(btn.setLabel($m("BbsAdvSearchPage.1")));
		}
		btn = sPage.createFilterDateButton(pp, url, "as_time");
		if (btn != null) {
			btns.add(btn.setLabel($m("AdvSearchPage.0")));
		}
		return btns;
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		return ElementList.of(new LinkButton($m("BbsTopicListTPage.6"))
				.setId("idBbsTopicListTPage_topicMenu").setIconClass(Icon.file).setMenuIcon(true));
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final BbsCategory category = getCategory(pp);
		final String list = pp.getParameter("list");
		return ElementList
				.of(new SearchInput("AbstractBbsTPage_search")
						.setOnSearchClick(
								"$Actions.loc('"
										+ HttpUtils.addParameters(getUrlsFactory().getUrl(pp,
												BbsTopicListPage.class, (BbsCategory) null), "s=")
										+ "' + encodeURIComponent($F('AbstractBbsTPage_search')))")
						.setOnAdvClick(
								"$Actions['AbstractBbsTPage_SearchWindow']('"
										+ AdvSearchPage.encodeRefererUrl(
												getUrlsFactory().getUrl(pp, BbsTopicListPage.class, category,
														StringUtils.hasText(list) ? "list=" + list : null))
										+ "');")
						.setText(StringUtils.blank(pp.getLocaleParameter("s"))));
	}

	@Override
	public NavigationButtons getNavigationBar(final PageParameter pp) {
		final LinkElement home = new LinkElement(bbsContext.getModule())
				.setHref(getUrlsFactory().getUrl(pp, BbsCategoryPage.class));
		final BbsCategory category = getCategory(pp);
		final NavigationButtons btns = NavigationButtons.of();
		String cText = null;
		if (category != null) {
			cText = category.getText();
		} else {
			final String list = pp.getParameter("list");
			if ("my".equals(list)) {
				cText = $m("BbsCategoryTPage.7");
			} else {
				cText = $m("BbsCategoryTPage.6");
			}
		}
		btns.append(home,
				new SpanElement().addElements(new SpanElement(cText), createCategoryDictMenu(pp)));
		return btns;
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='BbsTopicListTPage'>");
		sb.append(" <div id='tbl_").append(hashId).append("'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class TopicList extends AbstractDbTablePagerHandler {

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final String s = cp.getLocaleParameter("s");
			final IBbsTopicService service = bbsContext.getTopicService();
			if (StringUtils.hasText(s)) {
				return service.getLuceneService().query(s, BbsTopic.class);
			}
			final BbsCategory category = getCategory(cp);
			if (category != null) {
				cp.addFormParameter("categoryId", category.getId());
			}

			// 综合查询
			final FilterItems params = FilterItems
					.of(new FilterItem("status", EContentStatus.publish));
			if (category != null) {
				params.add(new FilterItem("categoryId", category.getId()));
			}

			final String list = cp.getParameter("list");
			if ("best".equals(list)) {
				params.add(new FilterItem("best", true));
			} else if ("my".equals(list)) {
				final Object id = cp.getLoginId();
				if (id == null) {
					return DataQueryUtils.nullQuery();
				}
				params.add(new FilterItem("userId", id));
			} else {
				final PermissionUser user = cp.getUser(cp.getParameter("userId"));
				if (user.exists()) {
					params.append(new FilterItem("userId", user.getId()));
				}
			}

			final String topic = cp.getLocaleParameter("as_topic");
			if (StringUtils.hasText(topic)) {
				params.addLike("topic", cp.getLocaleParameter("as_topic"));
			}
			params.addEqual("createDate", new TimePeriod(cp.getParameter("as_time")));
			return service.queryByParams(params);
		}

		@Override
		public MenuItems getContextMenu(final ComponentParameter cp, final MenuBean menuBean,
				final MenuItem menuItem) {
			return MenuItems.of().append(MenuItem.itemEdit()
					.setOnclick("$Actions.loc('" + getUrlsFactory().getUrl(null, BbsTopicFormPage.class)
							+ "?topicId=' + $pager_action(item).rowId());"))
					.append(MenuItem.sep())
					.append(MenuItem.of($m("AbstractContentBean.2"))
							.setOnclick_act("BbsTopicListTPage_recommendation", "topicId"))
					.append(MenuItem.of($m("BbsTopicListTPage.10"))
							.setOnclick_act("BbsTopicListTPage_adv", "topicId"))
					.append(MenuItem.sep())
					.append(MenuItem.itemDelete().setOnclick_act("BbsTopicListTPage_delete", "topicId"))
					.append(MenuItem.sep())
					.append(MenuItem.itemLog().setOnclick_act("BbsTopicListTPage_logWin", "topicId"));
		}

		@Override
		public AbstractTablePagerSchema createTablePagerSchema() {
			return new DefaultTablePagerSchema() {
				@Override
				public Map<String, Object> getRowData(final ComponentParameter cp,
						final Object dataObject) {
					final BbsTopic topic = (BbsTopic) dataObject;
					final KVMap kv = new KVMap();
					final StringBuilder sb = new StringBuilder();
					sb.append(new SpanElement().setClassName("type_" + topic.getBbsType().name()));
					final int posts = bbsContext.getPostService()
							.query(topic, new TimePeriod(ETimePeriod.day)).getCount();
					if (posts > 0) {
						sb.append(new SupElement(posts).setTitle($m("BbsTopicListTPage.8", posts)));
					}
					kv.put(TablePagerColumn.ICON, sb.toString());
					BbsCategory category = getCategory(cp);
					sb.setLength(0);
					if (category == null) {
						category = bbsContext.getCategoryService().getBean(topic.getCategoryId());
						if (category != null) {
							sb.append("<span class='categoryTxt'>[");
							sb.append(new LinkElement(category.getText())
									.setHref(getUrlsFactory().getUrl(cp, BbsTopicListPage.class, category)));
							sb.append("]</span>");
						}
					}
					final LinkElement le = new LinkElement(topic.getTopic())
							.setHref(getUrlsFactory().getUrl(cp, BbsPostViewPage.class, topic))
							.setClassName("bbsTopic");
					if (topic.getRecommendation() > 0) {
						le.addClassName("recommendation");
					}
					sb.append(le);
					if (topic.isBest()) {
						sb.append(new SpanElement().setClassName("imgBest").setTitle($m("BbsTopic.0")));
					}
					kv.put("topic", sb.toString());
					kv.put("userId", getUserStat(cp, topic.getUserId(), topic.getCreateDate()));
					kv.put("posts", SpanElement.num(topic.getPosts()));
					kv.put("views", SpanElement.num(topic.getViews()));
					kv.put("favorites", SpanElement.num(topic.getFavorites()));
					final ID lastUserId = topic.getLastUserId();
					if (lastUserId != null) {
						kv.put("lastPost", getUserStat(cp, lastUserId, topic.getLastPostDate()));
					} else {
						kv.put("lastPost", getUserStat(cp, topic.getUserId(), topic.getCreateDate()));
					}
					if (getTablePagerColumns(cp).get(TablePagerColumn.OPE) != null) {
						sb.setLength(0);
						sb.append(ButtonElement.editBtn().setOnclick(
								JS.loc(getUrlsFactory().getUrl(cp, BbsTopicFormPage.class, topic))));
						sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
						kv.put(TablePagerColumn.OPE, sb.toString());
					}
					return kv;
				}

				private String getUserStat(final PageParameter pp, final ID userId, final Date date) {
					final StringBuilder sb = new StringBuilder();
					sb.append(PhotoImage.icon16(pp.getPhotoUrl(userId)));
					sb.append("<span class='userStat'>");
					sb.append(" <span class='us_1'>").append(createTopicsLink(pp, pp.getUser(userId)))
							.append("</span>");
					sb.append(" <span class='us_2'>")
							.append(DateUtils.getRelativeDate(date, DATE_NUMBERCONVERT)).append("</span>");
					sb.append("</span>");
					return sb.toString();
				}
			};
		}
	}

	public static class TopicAdvPage extends FormTableRowTemplatePage {

		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addFormValidationBean(pp)
					.addValidators(new Validator(EValidatorMethod.required, "#a_description"));
		}

		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final BbsTopic topic = BbsUtils.getTopic(cp);
			if (topic != null) {
				topic.setBest(cp.getBoolParameter("a_best"));
				topic.setBbsType(cp.getEnumParameter(EBbsType.class, "a_type"));
				LdescVal.set(topic, cp.getParameter("a_description"));
				bbsContext.getTopicService().update(new String[] { "best", "bbsType" }, topic);
			}
			final JavascriptForward js = super.onSave(cp);
			js.append("$Actions['BbsTopicListTPage_tbl']();");
			return js;
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final InputElement a_best = InputElement.checkbox("a_best");

			final Option[] opts = Option.from(EBbsType.values());
			final InputElement a_type = InputElement.select("a_type");

			final BbsTopic topic = BbsUtils.getTopic(pp);
			if (topic != null) {
				a_best.setChecked(topic.isBest());
				for (final Option opt : opts) {
					opt.setSelected(opt.getName().equals(topic.getBbsType().name()));
				}
			}

			final TableRow r1 = new TableRow(new RowField($m("TopicAdvPage.1"), a_best),
					new RowField($m("TopicAdvPage.2"), a_type.addElements(opts)));
			final TableRow r2 = new TableRow(new RowField($m("TopicAdvPage.0"),
					InputElement.textarea("a_description").setRows(5)));
			return TableRows.of(r1, r2);
		}

		@Override
		public String getLabelWidth(final PageParameter pp) {
			return "80px";
		}
	}
}
