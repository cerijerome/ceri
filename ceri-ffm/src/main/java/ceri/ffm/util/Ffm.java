package ceri.ffm.util;

import java.lang.reflect.Method;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;

/**
 * Utility methods to support FFM.
 */
public class Ffm {

	private Ffm() {}

	public static String toString(Method method) {
		if (method == null) return Strings.NULL;
		var b = new StringBuilder();
		b.append(Reflect.name(method.getDeclaringClass())).append('.').append(method.getName())
			.append('(');
		var argTypes = method.getParameterTypes();
		for (int i = 0; i < argTypes.length; i++) {
			if (i > 0) b.append(", ");
			b.append(Reflect.name(argTypes[i]));
		}
		return b.append(')').toString();
	}

}
