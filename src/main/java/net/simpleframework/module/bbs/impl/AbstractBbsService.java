package net.simpleframework.module.bbs.impl;

import java.io.Serializable;

import net.simpleframework.ctx.service.ado.db.AbstractDbBeanService;
import net.simpleframework.module.bbs.IBbsContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class AbstractBbsService<T extends Serializable> extends AbstractDbBeanService<T>
		implements IBbsContextAware {

	public BbsCategoryService getCategoryService() {
		return (BbsCategoryService) context.getCategoryService();
	}

	public BbsTopicService getTopicService() {
		return (BbsTopicService) context.getTopicService();
	}

	public BbsPostService getPostService() {
		return (BbsPostService) context.getPostService();
	}

	public BbsUserStatService getUserStatService() {
		return (BbsUserStatService) context.getUserStatService();
	}

	protected BbsAttachmentService getAttachmentService() {
		return (BbsAttachmentService) context.getAttachmentService();
	}
}
