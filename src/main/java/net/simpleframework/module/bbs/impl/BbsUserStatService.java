package net.simpleframework.module.bbs.impl;

import net.simpleframework.ado.IParamsValue;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.common.ID;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.bbs.BbsOrganizationRef;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.IBbsUserStatService;
import net.simpleframework.organization.User;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsUserStatService extends AbstractBbsService<BbsUserStat> implements
		IBbsUserStatService {

	@Override
	public BbsUserStat getUserStat(final ID userId) {
		BbsUserStat stat = getBean(userId);
		if (stat == null) {
			stat = createBean();
			stat.setId(userId);
			insert(stat);
		}
		return stat;
	}

	@Override
	public void onInit() throws Exception {
		super.onInit();

		final IModuleRef ref = bbsContext.getOrganizationRef();
		if (ref != null) {
			((BbsOrganizationRef) ref).getUserService().addListener(new DbEntityAdapterEx<User>() {
				@Override
				public void onAfterDelete(final IDbEntityManager<User> manager,
						final IParamsValue paramsValue) throws Exception {
					super.onAfterDelete(manager, paramsValue);
					// 删除统计状态
					getEntityManager().delete(paramsValue);
				}
			});
		}
	}
}
