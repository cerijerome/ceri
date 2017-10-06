package ceri.common.property;

import static ceri.common.filter.Filters.not;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Represents an immutable lookup key with '.' separator.
 */
public class PathFactory {
	public static final PathFactory dot = new PathFactory(".");
	public static final PathFactory dash = new PathFactory("-");
	public final Pattern splitRegex;
	public final Path emptyPath;
	public final String separator;

	public static PathFactory create(String separator) {
		if (separator == null || separator.isEmpty())
			throw new IllegalArgumentException("Empty separator");
		if (dot.separator.equals(separator)) return dot;
		if (dash.separator.equals(separator)) return dash;
		return new PathFactory(separator);
	}

	private PathFactory(String separator) {
		this.separator = separator;
		emptyPath = new Path(this, "");
		splitRegex = Pattern.compile("\\Q" + separator + "\\E");
	}

	public Path path(String[] values) {
		if (values == null || values.length == 0) return emptyPath;
		List<String> parts = new ArrayList<>();
		for (String val : values)
			splitTo(parts, val);
		return pathFromParts(parts);
	}

	public Path path(String value, String... values) {
		List<String> parts = splitTo(new ArrayList<>(), value);
		if (values != null) for (String val : values)
			splitTo(parts, val);
		return pathFromParts(parts);
	}

	public Path path(Collection<String> values) {
		if (values == null || values.isEmpty()) return emptyPath;
		List<String> parts = new ArrayList<>();
		for (String val : values)
			splitTo(parts, val);
		return pathFromParts(parts);
	}

	public List<String> split(String path) {
		if (path == null || path.isEmpty()) return Collections.emptyList();
		return splitTo(new ArrayList<>(), path);
	}

	public int parts(String path) {
		return split(path).size();
	}

	public boolean canSplit(String value) {
		return value.indexOf(separator) != -1;
	}

	public Path parentOf(String path) {
		if (path == null || path.isEmpty()) return emptyPath;
		int i = path.lastIndexOf(separator);
		if (i == -1) return emptyPath;
		return new Path(this, path.substring(0, i));
	}

	public Path orphanOf(String path) {
		if (path == null || path.isEmpty()) return emptyPath;
		int i = path.indexOf(separator);
		if (i == -1 || i == path.length() - 1) return emptyPath;
		return new Path(this, path.substring(i + 1));
	}

	private Path pathFromParts(List<String> parts) {
		if (parts.isEmpty()) return emptyPath;
		return new Path(this, String.join(separator, parts));
	}

	private List<String> splitTo(List<String> list, String value) {
		if (value == null || value.isEmpty()) return list;
		Stream.of(splitRegex.split(value)).map(String::trim).filter(not(String::isEmpty))
			.forEach(list::add);
		return list;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(separator);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PathFactory)) return false;
		PathFactory other = (PathFactory) obj;
		if (!EqualsUtil.equals(separator, other.separator)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, separator).toString();
	}

}
