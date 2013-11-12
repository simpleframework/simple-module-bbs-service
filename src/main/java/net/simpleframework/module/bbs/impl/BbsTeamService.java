package net.simpleframework.module.bbs.impl;

import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.BbsTeam;
import net.simpleframework.module.bbs.IBbsContextAware;
import net.simpleframework.module.bbs.IBbsTeamService;
import net.simpleframework.module.common.team.TeamRole;
import net.simpleframework.module.common.team.impl.AbstractTeamService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class BbsTeamService extends AbstractTeamService<BbsTeam> implements IBbsTeamService,
		IBbsContextAware {

	private static TeamRole[] TEAM_ROLE = new TeamRole[] { MANAGER, NORMAL };

	@Override
	public boolean isManager(final BbsCategory category, final PermissionUser user) {
		return isMemeber(category, user, MANAGER.getName());
	}

	@Override
	public TeamRole[] getTeamRoles() {
		return TEAM_ROLE;
	}

	@Override
	public TeamRole getTeamRole(final String name) {
		for (final TeamRole tr : getTeamRoles()) {
			if (tr.getName().equals(name)) {
				return tr;
			}
		}
		return null;
	}
}
