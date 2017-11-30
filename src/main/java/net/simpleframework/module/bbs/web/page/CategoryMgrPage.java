package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ext.category.ctx.CategoryBeanAwareHandler;
import net.simpleframework.mvc.component.ui.menu.MenuItem;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.template.lets.OneTreeTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class CategoryMgrPage extends OneTreeTemplatePage implements IBbsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		addCategoryBean(pp, "CategoryMgrPage_tree", BbsCategoryHandler.class);

		// 权限
		final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "CategoryMgrPage_teamPage",
				CategoryTeamPage.class);
		addWindowBean(pp, "CategoryMgrPage_teamWin", ajaxRequest).setHeight(480).setWidth(800)
				.setTitle($m("CategoryMgrPage.2"));
	}

	@Override
	public String toTopicHTML(final PageParameter pp) {
		return $m("CategoryMgrPage.0");
	}

	public static class BbsCategoryHandler extends CategoryBeanAwareHandler<BbsCategory> {

		@Override
		protected IBbsCategoryService getBeanService() {
			return bbsContext.getCategoryService();
		}

		@Override
		protected String[] getContextMenuKeys() {
			return new String[] { "Add", "Edit", "Delete", "-", "Team", "-", "Refresh", "-", "Move.up",
					"Move.up2", "Move.down", "Move.down2" };
		}

		@Override
		protected KVMap createContextMenuItems() {
			return super.createContextMenuItems().add("Team",
					MenuItem.of($m("CategoryMgrPage.2")).setOnclick(
							"$Actions['CategoryMgrPage_teamWin']('ownerId=' + $category_action(item).getId());"));
		}

		@Override
		public TreeNodes getCategoryTreenodes(final ComponentParameter cp, final TreeBean treeBean,
				final TreeNode parent) {
			if (parent == null) {
				final TreeNodes nodes = TreeNodes.of();
				final TreeNode tn = createRoot(treeBean, $m("CategoryMgrPage.1"));
				tn.setAcceptdrop(true);
				nodes.add(tn);
				return nodes;
			}
			final TreeNodes nodes = super.getCategoryTreenodes(cp, treeBean, parent);
			if (nodes != null) {
				for (final TreeNode tn : nodes) {
					tn.setJsDblclickCallback("$category_action(branch).edit();");
					final Object obj = tn.getDataObject();
					if (obj instanceof BbsCategory) {
						final int topics = ((BbsCategory) obj).getTopics();
						if (topics > 0) {
							tn.setPostfixText("(" + topics + ")");
						}
					}
				}
			}
			return nodes;
		}

		@Override
		public TreeNodes getCategoryDictTreenodes(final ComponentParameter cp,
				final TreeBean treeBean, final TreeNode parent) {
			return super.getCategoryTreenodes(cp, treeBean, parent);
		}

		@Override
		protected void onSave_setProperties(final ComponentParameter cp, final BbsCategory category,
				final boolean insert) {
			if (insert) {
				category.setUserId(cp.getLoginId());
			}
			// category.setMark(Convert.toEnum(ECategoryMark.class,
			// cp.getParameter("category_mark")));
		}
	}
}
