package ceri.common.filter;

public class EnumFilters {

	private EnumFilters() {}

	public static <T extends Enum<T>> Filter<T> byName(String name) {
		return byName(Filters.eq(name));
	}
	
	public static <T extends Enum<T>> Filter<T> byName(Filter<String> filter) {
		return Filters.nonNull(t -> filter.filter(t.name()));
	}
	
}
