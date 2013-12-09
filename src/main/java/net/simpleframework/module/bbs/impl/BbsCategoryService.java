package net.simpleframework.module.bbs.impl;

import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.service.ado.db.AbstractDbBeanService;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.IBbsCategoryService;
import net.simpleframework.module.bbs.IBbsContextAware;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class BbsCategoryService extends AbstractDbBeanService<BbsCategory> implements
		IBbsCategoryService, IBbsContextAware {

	@Override
	public BbsCategory getBeanByName(final String name) {
		return StringUtils.hasText(name) ? getBean("name=?", name) : null;
	}

	@Override
	public void onInit() throws Exception {
		addListener(new DbEntityAdapterEx() {

			@Override
			public void onBeforeUpdate(final IDbEntityManager<?> manager, final String[] columns,
					final Object[] beans) {
				super.onBeforeUpdate(manager, columns, beans);
				for (final Object o : beans) {
					assertParentId((BbsCategory) o);
				}
			}
		});
	}
}
