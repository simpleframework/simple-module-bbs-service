package net.simpleframework.module.bbs;

import net.simpleframework.ado.db.DbEntityTable;
import net.simpleframework.module.common.team.Team;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsTeam extends Team {

	public static final DbEntityTable TBL = new DbEntityTable(BbsTeam.class, "sf_bbs_team");

	private static final long serialVersionUID = 7382297618409365430L;

}
