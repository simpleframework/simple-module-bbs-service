package net.simpleframework.module.bbs;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.permission.PermissionUser;
import net.simpleframework.module.common.team.ITeamService;
import net.simpleframework.module.common.team.TeamRole;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public interface IBbsTeamService extends ITeamService<BbsTeam> {

	static TeamRole MANAGER = TeamRole.of("manager", $m("IBbsTeamService.0"));

	static TeamRole NORMAL = TeamRole.of("normal", $m("IBbsTeamService.1"));

	/**
	 * 判断是否为论坛管理员
	 * 
	 * @param category
	 * @param user
	 * @return
	 */
	boolean isManager(BbsCategory category, PermissionUser user);
}
