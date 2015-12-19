package net.simpleframework.module.bbs.impl;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ado.db.DbEntityTable;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.module.bbs.BbsAskVote;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsPost;
import net.simpleframework.module.bbs.BbsTeam;
import net.simpleframework.module.bbs.BbsTopic;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.IBbsAskVoteService;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContext;
import net.simpleframework.module.bbs.IBbsPostService;
import net.simpleframework.module.bbs.IBbsTeamService;
import net.simpleframework.module.bbs.IBbsTopicService;
import net.simpleframework.module.bbs.IBbsUserStatService;
import net.simpleframework.module.common.AbstractCommonModuleContext;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class BbsContext extends AbstractCommonModuleContext implements IBbsContext {

	@Override
	protected DbEntityTable[] createEntityTables() {
		return new DbEntityTable[] { new DbEntityTable(BbsCategory.class, "sf_bbs_category"),
				new DbEntityTable(BbsTopic.class, "sf_bbs_topic"),
				new DbEntityTable(BbsPost.class, "sf_bbs_post"),
				new DbEntityTable(BbsTeam.class, "sf_bbs_team"),
				new DbEntityTable(BbsUserStat.class, "sf_bbs_user"),
				new DbEntityTable(BbsAskVote.class, "sf_bbs_ask_vote") };
	}

	@Override
	public IBbsCategoryService getCategoryService() {
		return singleton(BbsCategoryService.class);
	}

	@Override
	public IBbsTopicService getTopicService() {
		return singleton(BbsTopicService.class);
	}

	@Override
	public IBbsPostService getPostService() {
		return singleton(BbsPostService.class);
	}

	@Override
	public IBbsUserStatService getUserStatService() {
		return singleton(BbsUserStatService.class);
	}

	@Override
	public IBbsAskVoteService getAskVoteService() {
		return singleton(BbsAskVoteService.class);
	}

	@Override
	public IAttachmentService<Attachment> getAttachmentService() {
		return singleton(BbsAttachmentService.class);
	}

	@Override
	public IBbsTeamService getTeamService() {
		return singleton(BbsTeamService.class);
	}

	@Override
	public IModuleRef getOrganizationRef() {
		return getRef("net.simpleframework.module.bbs.BbsOrganizationRef");
	}

	@Override
	protected Module createModule() {
		return new Module().setName(MODULE_NAME).setText($m("BbsContext.0")).setOrder(33);
	}
}
