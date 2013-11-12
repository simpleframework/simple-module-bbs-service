package net.simpleframework.module.bbs;

import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.common.ICommonModuleContext;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface IBbsContext extends ICommonModuleContext {
	static final String ROLE_BBS_MANAGER = "bbs_manager";

	static final String MODULE_NAME = "simple-module-bbs";

	/**
	 * 获取类目服务
	 * 
	 * @return
	 */
	IBbsCategoryService getCategoryService();

	/**
	 * 获取主题服务
	 * 
	 * @return
	 */
	IBbsTopicService getTopicService();

	/**
	 * 获取主题的跟贴服务
	 * 
	 * @return
	 */
	IBbsPostService getPostService();

	IBbsUserStatService getUserStatService();

	IBbsAskVoteService getAskVoteService();

	@Override
	IAttachmentService<Attachment> getAttachmentService();

	/**
	 * 获取论坛的管理团队
	 * 
	 * @return
	 */
	IBbsTeamService getTeamService();

	/**
	 * 获取机构的引用
	 * 
	 * @return
	 */
	IModuleRef getOrganizationRef();
}
