package net.simpleframework.module.bbs.web;

import static net.simpleframework.common.I18n.$m;

import java.util.Calendar;
import java.util.Date;

import net.simpleframework.ctx.service.ado.IADOBeanService;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.web.page.t2.BbsPostViewPage;
import net.simpleframework.module.common.web.content.ListRowHandler;
import net.simpleframework.module.common.web.content.PageletCreator;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.template.struct.Pagelet;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsPageletCreator extends PageletCreator<BbsTopic> implements IBbsContextAware {
	public Pagelet getHistoryPagelet(final PageParameter pp) {
		return getHistoryPagelet(pp, "bbs_views");
	}

	@Override
	protected ListRowHandler<BbsTopic> getListRowHandler() {
		if (lrowHandler == null) {
			lrowHandler = new BbsListRowHandler();
		}
		return lrowHandler;
	}

	public static class BbsListRowHandler extends ListRowHandler<BbsTopic> {
		@Override
		protected String getHref(final PageParameter pp, final BbsTopic bean) {
			return ((IBbsWebContext) bbsContext).getUrlsFactory().getUrl(pp, BbsPostViewPage.class,
					bean);
		}

		@Override
		protected String[] getShortDesc(final BbsTopic topic) {
			final int c = topic.getPosts();
			final long v = topic.getViews();
			final StringBuilder sb = new StringBuilder();
			Date lastPostDate;
			if (c > 0 && (lastPostDate = topic.getLastPostDate()) != null) {
				final Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.HOUR_OF_DAY, -12);
				if (lastPostDate.after(cal.getTime())) {
					sb.append(new SpanElement(c).addStyle("color: red;"));
				}
			}
			if (sb.length() == 0) {
				sb.append(c);
			}
			sb.append("/").append(v);
			return new String[] { sb.toString(), $m("BbsPageletCreator.0", c, v) };
		}

		@Override
		protected IADOBeanService<BbsTopic> getBeanService() {
			return bbsContext.getTopicService();
		}
	}
}