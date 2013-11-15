package net.simpleframework.module.bbs.impl;

import static net.simpleframework.common.I18n.$m;

import java.util.Calendar;
import java.util.Date;

import net.simpleframework.ado.db.DbEntityTable;
import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.Convert;
import net.simpleframework.ctx.IApplicationContext;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.ctx.permission.IPermissionConst;
import net.simpleframework.ctx.permission.LoginUser;
import net.simpleframework.ctx.task.ExecutorRunnable;
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
import net.simpleframework.module.common.DescriptionLocalUtils;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.AttachmentLob;
import net.simpleframework.module.common.content.IAttachmentService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class BbsContext extends AbstractCommonModuleContext implements IBbsContext {

	@Override
	public void onInit(final IApplicationContext application) throws Exception {
		super.onInit(application);

		getTaskExecutor().addScheduledTask(60 * 5, new ExecutorRunnable() {
			@Override
			protected void task() throws Exception {
				final IBbsTopicService tService = getTopicService();
				final IDataQuery<BbsTopic> dq = tService.queryRecommendationBeans(null, null);
				BbsTopic topic;
				LoginUser.setAdmin();
				while ((topic = dq.next()) != null) {
					final Date rDate = topic.getRecommendationDate();
					final int dur = topic.getRecommendationDuration();
					if (rDate != null && dur > 0) {
						final Calendar cal = Calendar.getInstance();
						cal.setTime(rDate);
						cal.add(Calendar.SECOND, dur);
						if (cal.getTime().before(new Date())) {
							topic.setRecommendation(0);
							DescriptionLocalUtils.set(topic,
									$m("BbsContext.1", Convert.toDateString(rDate), dur));
							tService.update(new String[] { "recommendation" }, topic);
						}
					}
				}
			}
		});
	}

	@Override
	protected DbEntityTable[] getEntityTables() {
		return new DbEntityTable[] { BbsCategory.TBL, BbsTopic.TBL, BbsPost.TBL, BbsTeam.TBL,
				BbsUserStat.TBL, BbsAskVote.TBL, Attachment.TBL, AttachmentLob.TBL };
	}

	@Override
	public String getManagerRole() {
		return IPermissionConst.ROLE_MANAGER;
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
