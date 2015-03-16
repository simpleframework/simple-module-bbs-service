package net.simpleframework.module.bbs;

import static net.simpleframework.common.I18n.$m;
import static net.simpleframework.module.bbs.impl.BbsContext.ROLE_BBS_MANAGER;
import net.simpleframework.ctx.IModuleContext;
import net.simpleframework.organization.ERoleType;
import net.simpleframework.organization.OrganizationRef;
import net.simpleframework.organization.RolenameW;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsOrganizationRef extends OrganizationRef {

	@Override
	public void onInit(final IModuleContext context) throws Exception {
		super.onInit(context);

		ROLE_BBS_MANAGER = RolenameW.toUniqueRolename(RolenameW.ROLECHART_ORG_DEFAULT, "bbsmgr");
		RolenameW.registRole("bbsmgr", $m("BbsOrganizationRef.0"), null, ERoleType.normal);
	}
}