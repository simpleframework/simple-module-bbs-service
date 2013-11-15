package net.simpleframework.module.bbs;

import net.simpleframework.ctx.service.ado.IADOTreeBeanServiceAware;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface IBbsCategoryService extends IDbBeanService<BbsCategory>,
		IADOTreeBeanServiceAware<BbsCategory> {
}
