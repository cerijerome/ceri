package ceri.common.util;

import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;
import ceri.common.property.PathFactory;
import ceri.common.text.StringUtil;
import ceri.common.text.ToStringHelper;

/**
 * A startup value that may be specified by main argument, system property, or environment variable.
 */
public class StartupValue {
	public final Integer argIndex;
	public final String sysPropertyName;
	public final String envVariableName;

	public static String arg(int index, String... args) {
		if (args == null || index < 0 || index >= args.length) return null;
		return StringUtil.trim(args[index]);
	}

	public static StartupValue of(Integer argIndex, String sysPropertyName) {
		return new StartupValue(argIndex, sysPropertyName, envVariableName(sysPropertyName));
	}

	public static StartupValue of(Integer argIndex, String sysPropertyName,
		String envVariableName) {
		return new StartupValue(argIndex, sysPropertyName, envVariableName);
	}

	public static StartupValue ofClass(Integer argIndex, Class<?> sysPropertyCls,
		String sysPropertySuffix) {
		String sysPropertyName = sysPropertyName(sysPropertyCls, sysPropertySuffix);
		return of(argIndex, sysPropertyName, envVariableName(sysPropertyName));
	}

	public static StartupValue ofClass(Integer argIndex, Class<?> sysPropertyCls,
		String sysPropertySuffix, String envVariableName) {
		return of(argIndex, sysPropertyName(sysPropertyCls, sysPropertySuffix), envVariableName);
	}

	private static String envVariableName(String sysPropertyName) {
		if (sysPropertyName == null) return null;
		return sysPropertyName.toUpperCase().replace('.', '_');
	}

	private static String sysPropertyName(Class<?> cls, String suffix) {
		suffix = StringUtil.trim(suffix);
		if (cls == null) return suffix;
		if (suffix == null || suffix.isEmpty()) return cls.getPackageName();
		return PathFactory.dot.path(cls.getPackageName(), suffix).value;
	}

	private StartupValue(Integer argIndex, String sysPropertyName, String envVariableName) {
		this.argIndex = argIndex;
		this.sysPropertyName = sysPropertyName;
		this.envVariableName = envVariableName;
	}

	public <E extends Exception, T> T apply(String[] args, ExceptionFunction<E, String, T> fn)
		throws E {
		return apply(args, fn, null);
	}

	public <E extends Exception, T> T apply(String[] args, ExceptionFunction<E, String, T> fn,
		ExceptionSupplier<E, T> def) throws E {
		String value = value(args);
		if (value != null && fn != null) return fn.apply(value);
		return def == null ? null : def.get();
	}

	public String value(String...args) {
		return value(args, null);
	}

	public String value(String[] args, String defaultValue) {
		String value = arg(args);
		if (value == null) value = sysProperty();
		if (value == null) value = envVariable();
		if (value == null) value = defaultValue;
		return value;
	}

	private String arg(String[] args) {
		return argIndex == null ? null : arg(argIndex, args);
	}

	private String sysProperty() {
		if (sysPropertyName == null || sysPropertyName.isEmpty()) return null;
		return System.getProperty(sysPropertyName);
	}

	private String envVariable() {
		if (envVariableName == null || envVariableName.isEmpty()) return null;
		return System.getenv(envVariableName);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(argIndex, sysPropertyName, envVariableName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof StartupValue)) return false;
		StartupValue other = (StartupValue) obj;
		if (!EqualsUtil.equals(argIndex, other.argIndex)) return false;
		if (!EqualsUtil.equals(sysPropertyName, other.sysPropertyName)) return false;
		if (!EqualsUtil.equals(envVariableName, other.envVariableName)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, argIndex, sysPropertyName, envVariableName)
			.toString();
	}

}
