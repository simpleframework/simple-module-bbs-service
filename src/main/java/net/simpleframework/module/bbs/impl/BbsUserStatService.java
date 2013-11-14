package net.simpleframework.module.bbs.impl;

import net.simpleframework.ado.IParamsValue;
import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.common.ID;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.service.ado.db.AbstractDbBeanService;
import net.simpleframework.module.bbs.BbsOrganizationRef;
import net.simpleframework.module.bbs.BbsUserStat;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.IBbsUserStatService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class BbsUserStatService extends AbstractDbBeanService<BbsUserStat> implements
		IBbsUserStatService, IBbsContextAware {

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

		final IModuleRef ref = context.getOrganizationRef();
		if (ref != null) {
			((BbsOrganizationRef) ref).getUserService().addListener(new DbEntityAdapterEx() {
				@Override
				public void onAfterDelete(final IDbEntityManager<?> manager,
						final IParamsValue paramsValue) {
					super.onAfterDelete(manager, paramsValue);
					// 删除统计状态
					getEntityManager().delete(paramsValue);
				}
			});
		}
	}
}
