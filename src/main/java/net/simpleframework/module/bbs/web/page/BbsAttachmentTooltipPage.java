package net.simpleframework.module.bbs.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;

import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.web.BbsLogRef.BbsDownloadLogPage;
import net.simpleframework.module.bbs.web.IBbsWebContext;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.common.web.content.page.AbstractAttachmentTooltipPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsAttachmentTooltipPage extends AbstractAttachmentTooltipPage
		implements IBbsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		if (((IBbsWebContext) bbsContext).getLogRef() != null) {
			addComponentBean(pp, "AttachmentTooltipPage_logPage", AjaxRequestBean.class)
					.setUrlForward(url(BbsDownloadLogPage.class));
			addComponentBean(pp, "AttachmentTooltipPage_logWin", WindowBean.class)
					.setContentRef("AttachmentTooltipPage_logPage").setHeight(480).setWidth(800)
					.setTitle($m("NewsFormAttachPage.5"));
		}
	}

	@Override
	protected AttachmentFile getAttachment(final PageParameter pp) {
		final IAttachmentService<Attachment> service = bbsContext.getAttachmentService();
		try {
			return service.createAttachmentFile(service.getBean(pp.getParameter("id")));
		} catch (final IOException e) {
			return null;
		}
	}

	@Override
	protected Object getTopic(final PageParameter pp, final AttachmentFile attachment) {
		return new LinkElement(attachment.getTopic())
				.setOnclick("$Actions['BbsPostViewTPage_download']('id=" + attachment.getId() + "');");
	}

	@Override
	protected Object getDownloads(final PageParameter pp, final AttachmentFile attachment) {
		final int downloads = attachment.getDownloads();
		if (downloads <= 0) {
			return 0;
		}
		if (((IBbsWebContext) bbsContext).getLogRef() != null) {
			return LinkButton.corner(downloads).setOnclick(
					"$Actions['AttachmentTooltipPage_logWin']('beanId=" + attachment.getId() + "');");
		} else {
			return downloads;
		}
	}
}
