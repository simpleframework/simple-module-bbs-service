package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.DateUtils.NumberConvert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.EBbsType;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.web.BbsUrlsFactory;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicFormPage;
import net.simpleframework.module.bbs.web.page.t2.BbsTopicListPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.menu.EMenuEvent;
import net.simpleframework.mvc.component.ui.menu.MenuBean;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.tree.ITreeHandler;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.AdvSearchPage;
import net.simpleframework.mvc.template.lets.TopBar_PageletsPage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class AbstractBbsTPage extends TopBar_PageletsPage implements IBbsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		pp.addImportCSS(AbstractBbsTPage.class, "/bbs.css");
	}

	protected WindowBean addSearchWindow(final PageParameter pp) {
		pp.addComponentBean("AbstractBbsTPage_SearchPage", AjaxRequestBean.class)
				.setUrlForward(url(BbsAdvSearchPage.class));
		return pp.addComponentBean("AbstractBbsTPage_SearchWindow", WindowBean.class)
				.setContentRef("AbstractBbsTPage_SearchPage").setTitle($m("AbstractBbsTPage.0"))
				.setXdelta(-440).setYdelta(2).setWidth(500).setHeight(300).setPopup(true);
	}

	protected DictionaryBean addCategoryDict(final PageParameter pp) {
		addComponentBean(pp, "AbstractBbsTPage_category_tree", TreeBean.class).setShowTip(false)
				.setHandlerClass(getCategoryDictClass());
		return (DictionaryBean) addDictionaryBean(pp, "AbstractBbsTPage_category_dict")
				.setClearAction("false").addTreeRef(pp, "AbstractBbsTPage_category_tree")
				.setJsSelectCallback("$Actions.loc('"
						+ getUrlsFactory().getUrl(pp, BbsTopicListPage.class, (BbsCategory) null)
						+ "?categoryId=' + selects[0].id);")
				.setHeight(400).setWidth(320).setTitle($m("BbsTopicListTPage.7"));
	}

	protected MenuBean addTopicMenuBean(final PageParameter pp) {
		final MenuBean mb = (MenuBean) addComponentBean(pp, "BbsTopicListTPage_topicMenu",
				MenuBean.class).setMenuEvent(EMenuEvent.click)
						.setSelector("#idBbsTopicListTPage_topicMenu");
		final BbsCategory category = getCategory(pp);
		for (final EBbsType bbsType : EBbsType.values()) {
			final String name = bbsType.name();
			mb.addItem(MenuItem.of(bbsType.toString()).setIconClass("menu_type_" + name)
					.setUrl(HttpUtils.addParameters(
							getUrlsFactory().getUrl(pp, BbsTopicFormPage.class, category), "t=" + name)));
		}
		return mb;
	}

	public static BbsCategory getCategory(final PageParameter pp) {
		return getCacheBean(pp, bbsContext.getCategoryService(), "categoryId");
	}

	protected Class<? extends ITreeHandler> getCategoryDictClass() {
		return CategoryDict.class;
	}

	protected static BbsUrlsFactory getUrlsFactory() {
		return ((IBbsWebContext) bbsContext).getUrlsFactory();
	}

	protected static SpanElement createCategoryDictMenu(final PageParameter pp) {
		return new SpanElement().setOnclick("$Actions['AbstractBbsTPage_category_dict']();")
				.setClassName("bbs_down_chev");
	}

	protected static LinkElement createTopicsLink(final PageParameter pp,
			final PermissionUser user) {
		return new LinkElement(user)
				.setHref(getUrlsFactory().getUrl(pp, BbsTopicListPage.class, "userId=" + user.getId()));
	}

	public static class CategoryDict extends DictionaryTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			final TreeBean treeBean = (TreeBean) cp.componentBean;
			final BbsCategory category = parent != null ? (BbsCategory) parent.getDataObject() : null;
			final IDataQuery<?> dq = bbsContext.getCategoryService().queryChildren(category);
			if (dq != null && dq.getCount() > 0) {
				final TreeNodes nodes = TreeNodes.of();
				Object bean;
				while ((bean = dq.next()) != null) {
					final BbsCategory category2 = (BbsCategory) bean;
					final TreeNode tn = new TreeNode(treeBean, parent, category2);
					nodes.add(tn);
					final int topics = category2.getTopics();
					if (topics > 0) {
						tn.setPostfixText("(" + topics + ")");
					}
					if (parent == null) {
						tn.setOpened(true);
					}
				}
				return nodes;
			}
			return null;
		}

		@Override
		public Map<String, Object> getTreenodeAttributes(final ComponentParameter cp,
				final TreeNode treeNode, final TreeNodes children) {
			final KVMap kv = (KVMap) super.getTreenodeAttributes(cp, treeNode, children);
			if (treeNode.getParent() == null) {
				kv.put(TN_ATTRI_SELECT_DISABLE, Boolean.TRUE);
			}
			return kv;
		}
	}

	public static class BbsAdvSearchPage extends AdvSearchPage {
		@Override
		protected String[] getFilterParams() {
			return new String[] { "as_topic", "as_time" };
		}

		@Override
		public String toItemsHTML(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			sb.append(addSearchItem(pp, $m("BbsAdvSearchPage.0"), new InputElement("as_topic")));
			sb.append(addSearchDateItem(pp, "as_time"));
			return sb.toString();
		}
	}

	protected static NumberConvert DATE_NUMBERCONVERT = new NumberConvert() {
		@Override
		public Object convert(final Number n) {
			return SpanElement.num(n).addStyle("margin-right: 2px;");
		}
	};
}
