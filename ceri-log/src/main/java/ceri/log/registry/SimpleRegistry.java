package ceri.log.registry;

import java.util.Properties;
import java.util.function.Consumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.property.TypedProperties;

/**
 * A simple registry implementation that accesses properties without loading or saving. 
 */
public class SimpleRegistry implements Registry {
	public final Properties properties;
	private final TypedProperties typed;
	
	public static SimpleRegistry of(String... prefix) {
		var properties = new Properties();
		return new SimpleRegistry(properties, TypedProperties.from(properties, prefix));
	}
	
	private SimpleRegistry(Properties properties, TypedProperties typed) {
		this.properties = properties;
		this.typed = typed;
	}

	public String prefix() {
		return typed.prefix;
	}
	
	@Override
	public <E extends Exception, T> T apply(ExceptionFunction<E, TypedProperties, T> function)
		throws E {
		return function.apply(typed);
	}
	
	@Override
	public void queue(Object source, Consumer<TypedProperties> update) {
		update.accept(typed); // executes immediately
	}
	
	@Override
	public SimpleRegistry sub(String... subs) {
		return new SimpleRegistry(properties, typed.sub(subs));
	}
}
