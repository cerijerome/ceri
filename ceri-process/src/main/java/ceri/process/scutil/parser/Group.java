package ceri.process.scutil.parser;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.property.PathFactory;
import ceri.common.text.ToStringHelper;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.process.util.parse.Line;
import ceri.process.util.parse.NameValue;
import ceri.process.util.parse.Named;
import ceri.process.util.parse.Value;

public class Group implements Named {
	public static final Group NULL = new Builder(null, null).build();
	private final String name;
	public final String type;
	public final List<Value> values;
	private final Map<String, Named> namedValues;

	public static Group from(String output) {
		GroupParser groupParser = new GroupParser();
		new Parser(groupParser).parse(output);
		return groupParser.build();
	}
	
	public static class Builder {
		final String name;
		final String type;
		final List<Supplier<? extends Value>> values = new ArrayList<>();

		Builder(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public Builder add(String name, String value) {
			return add(new NameValue(name, value));
		}

		public Builder add(String line) {
			return add(new Line(line));
		}

		public Builder add(Value value) {
			return add(() -> value);
		}

		public Builder add(Supplier<? extends Value> supplier) {
			values.add(supplier);
			return this;
		}

		public Group build() {
			return new Group(this);
		}

	}

	public static Builder builder(String name, String type) {
		return new Builder(name, type);
	}

	Group(Builder builder) {
		name = builder.name;
		type = builder.type;
		values = unmodifiableList(builder.values.stream().map(s -> s.get()).collect(toList()));
		Stream<Named> stream = values.stream().map(v -> Named.asNamed(v)).filter(Objects::nonNull);
		namedValues = unmodifiableMap(stream.collect(Collectors.toMap(Named::name, identity())));
	}

	public static Group as(Value value) {
		if (value == null) return NULL;
		Group group = BasicUtil.<Group>castOrNull(Group.class, value);
		if (group != null) return group;
		Named named = BasicUtil.<Named>castOrNull(Named.class, value);
		if (named != null) return new Builder(named.name(), null).build();
		return NULL;
	}
	
	@Override
	public String name() {
		return name;
	}

	public Named valueByName(String name) {
		return namedValues.get(name);
	}

	public NameValue nameValue(String path) {
		return NameValue.as(valueByPath(path));
	}

	public Group group(String path) {
		return Group.as(valueByPath(path));
	}

	public Value valueByPath(String path) {
		return path(PathFactory.dot.split(path), 0);
	}

	private Value path(List<String> parts, int index) {
		Named named = valueByName(parts.get(index++));
		if (named == null) return null;
		if (index >= parts.size()) return named;
		Group group = BasicUtil.castOrNull(Group.class, named);
		if (group == null) return null;
		return group.path(parts, index);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(name, type, values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Group)) return false;
		Group other = (Group) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		if (!EqualsUtil.equals(values, other.values)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name, type, values).toString();
	}

}
