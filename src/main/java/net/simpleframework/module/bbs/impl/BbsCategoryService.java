package net.simpleframework.module.bbs.impl;

import net.simpleframework.common.StringUtils;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.IBbsCategoryService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsCategoryService extends AbstractBbsService<BbsCategory>
		implements IBbsCategoryService {

	@Override
	public BbsCategory getBeanByName(final String name) {
		return StringUtils.hasText(name) ? getBean("name=?", name) : null;
	}
}
