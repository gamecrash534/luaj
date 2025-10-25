package org.luaj.vm2.lib;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;

class TableLibFunction extends LibFunction {
	public LuaValue call() {
		return argerror(1, "table expected, got no value");
	}
}
