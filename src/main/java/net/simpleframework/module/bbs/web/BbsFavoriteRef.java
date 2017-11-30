package net.simpleframework.module.bbs.web;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.common.ID;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.web.page.t2.BbsPostViewPage;
import net.simpleframework.module.favorite.IFavoriteContent;
import net.simpleframework.module.favorite.plugin.IFavoritePlugin;
import net.simpleframework.module.favorite.web.FavoriteWebRef;
import net.simpleframework.module.favorite.web.plugin.AbstractWebFavoritePlugin;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsFavoriteRef extends FavoriteWebRef implements IBbsContextAware {

	@Override
	protected Class<? extends IFavoritePlugin> getPluginClass() {
		return BbsWebFavoritePlugin.class;
	}

	public static class BbsWebFavoritePlugin extends AbstractWebFavoritePlugin {

		@Override
		public AbstractElement<?> toFavoriteOpElement(final PageParameter pp,
				final Object contentId) {
			final AbstractElement<?> ele = super.toFavoriteOpElement(pp, contentId);
			return ele
					.setClassName(
							getMyFavorite(pp, contentId) != null ? "favorite favorite_active" : "favorite")
					.setOnclick(getFavoriteOnclick(contentId));
		}

		@Override
		protected String getFavoriteText(final Object contentId) {
			final StringBuilder sb = new StringBuilder();
			sb.append("<sup id='favorite_").append(contentId).append("'>");
			sb.append(toFavoritesNum(contentId));
			sb.append("</sup>");
			return sb.toString();
		}

		@Override
		protected String toFavoritesNum(final Object contentId) {
			final StringBuilder sb = new StringBuilder();
			final int favorites = getFavoritesNum(contentId);
			if (favorites > 0) {
				sb.append(favorites);
			}
			return sb.toString();
		}

		@Override
		protected void doInsertFavorite(final PageParameter pp, final Object contentId) {
			super.doInsertFavorite(pp, contentId);

			final IBbsTopicService service = bbsContext.getTopicService();
			final BbsTopic topic = service.getBean(contentId);
			topic.setFavorites(getFavoritesNum(contentId));
			service.update(new String[] { "favorites" }, topic);
		}

		@Override
		public String getText() {
			return $m("BbsFavoriteRef.0");
		}

		@Override
		public IFavoriteContent getContent(final PageParameter pp, final Object contentId) {
			final BbsTopic topic = bbsContext.getTopicService().getBean(contentId);
			return new AbstractFavoriteContent(topic) {
				@Override
				public String getUrl() {
					return ((IBbsWebContext) bbsContext).getUrlsFactory().getUrl(pp,
							BbsPostViewPage.class, topic);
				}

				@Override
				public ID getCategoryId() {
					return topic.getCategoryId();
				}
			};
		}

		@Override
		public String getCategoryText(final Object categoryId) {
			final BbsCategory category = bbsContext.getCategoryService().getBean(categoryId);
			return category != null ? category.getText() : null;
		}

		@Override
		public int getOrder() {
			return 21;
		}
	}
}