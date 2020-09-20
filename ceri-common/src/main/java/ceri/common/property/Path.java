package ceri.common.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents an immutable lookup key with a separator.
 */
public class Path {
	private final PathFactory factory;
	public final String value;

	Path(PathFactory factory, String value) {
		this.factory = factory;
		this.value = value;
	}

	public boolean isEmpty() {
		return value.isEmpty();
	}

	public Path child(String... parts) {
		return factory.path(value, parts);
	}

	public Path child(Collection<String> parts) {
		return factory.path(value, parts);
	}

	public Iterator<String> partIterator() {
		return asParts().iterator();
	}

	public List<String> asParts() {
		if (value.isEmpty()) return Collections.emptyList();
		return factory.split(value);
	}

	public String firstPart() {
		return factory.firstPart(value);
	}

	public String lastPart() {
		return factory.lastPart(value);
	}

	public int parts() {
		return factory.parts(value);
	}

	public boolean isRoot() {
		return !factory.canSplit(value);
	}

	public Path parent() {
		return factory.parentOf(value);
	}

	public Path orphan() {
		return factory.orphanOf(value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(factory, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Path)) return false;
		Path other = (Path) obj;
		if (!Objects.equals(factory, other.factory)) return false;
		if (!Objects.equals(value, other.value)) return false;
		return true;
	}

	@Override
	public String toString() {
		return value;
	}

}
