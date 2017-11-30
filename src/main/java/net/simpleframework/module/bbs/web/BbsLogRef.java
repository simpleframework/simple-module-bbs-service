package net.simpleframework.module.bbs.web;

import java.io.IOException;

import net.simpleframework.common.Convert;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.hdl.AbstractAttachmentLogHandler;
import net.simpleframework.module.log.web.page.DownloadLogPage;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsLogRef extends LogRef implements IBbsContextAware {

	public void addLogComponent(final PageParameter pp) {
		pp.addComponentBean("BbsTopicListTPage_logPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(BbsTopicLogPage.class));
		pp.addComponentBean("BbsTopicListTPage_logWin", WindowBean.class)
				.setContentRef("BbsTopicListTPage_logPage").setHeight(600).setWidth(960);
	}

	@Override
	public void logDownload(final Object beanId, final long length, final String filetype,
			final String topic) {
		super.logDownload(beanId, length, filetype, topic);

		// 更新计数
		final IAttachmentService<Attachment> service = bbsContext.getAttachmentService();
		final Attachment attachment = service.getBean(beanId);
		if (attachment != null) {
			attachment.setDownloads(getDownloadLogService().clog(beanId));
			service.update(new String[] { "downloads" }, attachment);
		}
	}

	public static class BbsTopicLogPage extends EntityUpdateLogPage {
		@Override
		protected IDbBeanService<?> getBeanService() {
			return bbsContext.getTopicService();
		}

		@Override
		public String getBeanIdParameter(final PageParameter pp) {
			return "topicId";
		}
	}

	public static class BbsDownloadLogPage extends DownloadLogPage implements IBbsContextAware {

		@Override
		protected IDbBeanService<?> getBeanService() {
			return bbsContext.getAttachmentService();
		}
	}

	public static class BbsTopicAttachmentAction
			extends AbstractAttachmentLogHandler<Attachment, BbsTopic> {

		@Override
		protected IAttachmentService<Attachment> getAttachmentService() {
			return bbsContext.getAttachmentService();
		}

		@Override
		protected IDbBeanService<BbsTopic> getOwnerService() {
			return bbsContext.getTopicService();
		}

		@Override
		protected String getOwnerIdParameterKey() {
			return "topicId";
		}

		@Override
		public AbstractElement<?> getDownloadLinkElement(final ComponentParameter cp,
				final AttachmentFile attachmentFile, final String id) throws IOException {
			if (Convert.toBool(cp.getParameter("opt_viewer"))) {
				final ImageElement iElement = createImageViewer(cp, attachmentFile, id);
				if (iElement != null) {
					return iElement;
				}
			}
			return new LinkElement(attachmentFile.getTopic())
					.setOnclick("$Actions['BbsPostViewTPage_download']('id=" + id + "');");
		}
	}
}