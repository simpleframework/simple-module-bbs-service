package net.simpleframework.module.bbs;

import static net.simpleframework.common.I18n.$m;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public enum EBbsType {

	/**
	 * 交流贴
	 */
	normal {
		@Override
		public String toString() {
			return $m("EBbsType.normal");
		}
	},

	/**
	 * 问题贴
	 */
	ask {
		@Override
		public String toString() {
			return $m("EBbsType.ask");
		}
	}
}
