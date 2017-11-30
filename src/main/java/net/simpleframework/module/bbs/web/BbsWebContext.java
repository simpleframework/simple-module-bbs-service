package net.simpleframework.module.bbs.web;

import static net.simpleframework.common.I18n.$m;

import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.Module;
import net.simpleframework.ctx.ModuleFunctions;
import net.simpleframework.ctx.ModuleRefUtils;
import net.simpleframework.module.bbs.impl.BbsContext;
import net.simpleframework.module.bbs.web.page.t2.BbsCategoryPage;
import net.simpleframework.mvc.ctx.WebModuleFunction;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsWebContext extends BbsContext implements IBbsWebContext {

	@Override
	public IModuleRef getFavoriteRef() {
		return ModuleRefUtils.getRef("net.simpleframework.module.bbs.web.BbsFavoriteRef");
	}

	@Override
	public IModuleRef getLogRef() {
		return ModuleRefUtils.getRef("net.simpleframework.module.bbs.web.BbsLogRef");
	}

	@Override
	public BbsPageletCreator getPageletCreator() {
		return singleton(BbsPageletCreator.class);
	}

	@Override
	public BbsUrlsFactory getUrlsFactory() {
		return singleton(BbsUrlsFactory.class);
	}

	@Override
	protected Module createModule() {
		return super.createModule().setDefaultFunction(MODULE_NAME + "-BbsCategoryTPage");
	}

	@Override
	protected ModuleFunctions getFunctions() {
		return ModuleFunctions.of((WebModuleFunction) new WebModuleFunction(this)
				.setUrl(getUrlsFactory().getUrl(null, BbsCategoryPage.class))
				.setName(MODULE_NAME + "-BbsCategoryTPage").setText($m("BbsContext.0")));
	}
}