package net.simpleframework.module.bbs.impl;

import net.simpleframework.ado.db.IDbEntityManager;
import net.simpleframework.module.bbs.BbsCategory;
import net.simpleframework.module.bbs.IBbsCategoryService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public class BbsCategoryService extends AbstractBbsService<BbsCategory> implements
		IBbsCategoryService {

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
