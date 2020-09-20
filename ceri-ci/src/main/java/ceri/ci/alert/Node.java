package ceri.ci.alert;

import ceri.common.util.PrimitiveUtil;
import ceri.common.util.SystemVars;

/**
 * Used to distinguish configuration between multiple instances of the alert system. System integer
 * property <em>node</em> determines the node id.
 */
public class Node {
	public static final String NODE = "node";
	public final int index;
	public final String name;

	private Node(int index) {
		this.index = index;
		name = NODE + index;
	}

	public static Node createFromEnv() {
		String value = SystemVars.sys(NODE);
		return new Node(PrimitiveUtil.valueOf(value, 0));
	}

}
