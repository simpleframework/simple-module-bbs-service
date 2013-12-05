package net.simpleframework.module.bbs;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.IModuleContext;
import net.simpleframework.organization.OrganizationRef;

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

		createRole_SystemChart(IBbsContext.ROLE_BBS_MANAGER, $m("BbsOrganizationRef.0"));
	}
}