package ceri.common.property;

import static ceri.common.validation.ValidationUtil.validate;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

public class Locator {
	public static final Locator NULL = new Builder(null).extension("").build();
	private static final String PROPERTIES_FILE_EXT = "properties";
	public final Class<?> cls;
	private final Path name;
	private final String extension;

	public static Locator of(Class<?> cls) {
		return builder(cls).build();
	}

	public static Locator of(Class<?> cls, String name) {
		return builder(cls, name).build();
	}

	public static class Builder {
		final Class<?> cls;
		final Collection<String> parts = new ArrayList<>();
		String extension = PROPERTIES_FILE_EXT;

		Builder(Class<?> cls) {
			this.cls = cls;
		}

		public Builder add(Enum<?>... enums) {
			for (Enum<?> en : enums)
				add(en.name());
			return this;
		}

		public Builder add(String... parts) {
			return add(Arrays.asList(parts));
		}

		public Builder add(Collection<String> parts) {
			parts.stream().map(StringUtil::trim).forEach(this.parts::add);
			return this;
		}

		public Builder extension(String extension) {
			this.extension = extension;
			return this;
		}

		public Locator build() {
			return new Locator(this);
		}
	}

	public static Builder builder(Class<?> cls) {
		validateNotNull(cls);
		return new Builder(cls).add(cls.getSimpleName());
	}

	public static Builder builder(Class<?> cls, String name) {
		validateNotNull(cls);
		validate(!StringUtil.blank(name), "Name cannot be blank");
		Builder b = new Builder(cls);
		int i = name.lastIndexOf('.');
		if (i == -1) return b.add(name);
		return b.add(name.substring(0, i)).extension(name.substring(i + 1));
	}

	Locator(Builder builder) {
		this(builder.cls, PathFactory.dash.path(builder.parts), builder.extension);
	}

	private Locator(Class<?> cls, Path name, String extension) {
		this.cls = cls;
		this.name = name;
		this.extension = extension;
	}

	public boolean isRoot() {
		return name.isRoot();
	}

	public List<Locator> ancestors() {
		if (isRoot()) return Collections.singletonList(this);
		List<Locator> ancestors = new ArrayList<>();
		for (Locator locator = this; !locator.isNull(); locator = locator.parent())
			ancestors.add(0, locator);
		return ancestors;
	}

	public Locator parent() {
		if (isRoot()) return Locator.NULL;
		return new Locator(cls, name.parent(), extension);
	}

	public Locator child(String... parts) {
		return new Locator(cls, name.child(parts), extension);
	}

	public Locator child(Enum<?>... enums) {
		String[] parts = new String[enums.length];
		for (int i = 0; i < enums.length; i++)
			parts[i] = enums[i].name();
		return new Locator(cls, name.child(parts), extension);
	}

	public boolean isNull() {
		return cls == null;
	}

	public String filename() {
		if (StringUtil.blank(extension)) return name.value;
		return name.value + "." + extension;
	}

	public InputStream resourceAsStream() {
		return cls.getResourceAsStream(filename());
	}

	@Override
	public int hashCode() {
		return Objects.hash(cls, name, extension);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Locator other)) return false;
		if (!Objects.equals(cls, other.cls)) return false;
		if (!Objects.equals(name, other.name)) return false;
		if (!Objects.equals(extension, other.extension)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, cls, filename());
	}

}
