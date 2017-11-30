package net.simpleframework.module.bbs.web;

import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.common.web.content.IContentRefAware;
import net.simpleframework.mvc.common.IDownloadHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IBbsWebContext extends IBbsContext, IContentRefAware {

	/**
	 * 获取小页面的创建类
	 * 
	 * @return
	 */
	BbsPageletCreator getPageletCreator();

	/**
	 * 获取url的构建工厂类
	 * 
	 * 子类覆盖
	 * 
	 * @return
	 */
	BbsUrlsFactory getUrlsFactory();

	public static class AttachmentDownloadHandler implements IDownloadHandler, IBbsContextAware {

		@Override
		public void onDownloaded(final Object beanId, final long length, final String filetype,
				final String topic) {
			final IModuleRef ref = ((IBbsWebContext) bbsContext).getLogRef();
			if (ref != null) {
				// 记录下载日志
				((BbsLogRef) ref).logDownload(beanId, length, filetype, topic);
			}
		}
	}
}
