package ceri.common.svg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ceri.common.collection.Immutable;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.stream.Streams;
import ceri.common.text.ToString;

public class PathGroup implements Path<PathGroup> {
	public final List<Path<?>> paths;
	private final Position end;

	public static PathGroup of(Path<?>... paths) {
		return of(Arrays.asList(paths));
	}

	public static PathGroup of(Collection<? extends Path<?>> paths) {
		return new PathGroup(paths);
	}

	private PathGroup(Collection<? extends Path<?>> paths) {
		this.paths = Immutable.list(paths);
		end = SvgUtil.combinedEnd(this.paths);
	}

	@Override
	public PathGroup reverse() {
		var list = new ArrayList<>(paths);
		Collections.reverse(list);
		return of(Streams.from(list).map(Path::reverse).toList());
	}

	@Override
	public PathGroup reflect(Line2d line) {
		return of(Streams.from(paths).map(path -> path.reflect(line)).toList());
	}

	@Override
	public PathGroup scale(Ratio2d scale) {
		return of(Streams.from(paths).map(path -> path.scale(scale)).toList());
	}

	@Override
	public PathGroup translate(Point2d offset) {
		return of(Streams.from(paths).map(path -> path.translate(offset)).toList());
	}

	@Override
	public Position end() {
		return end;
	}

	@Override
	public String path() {
		return SvgUtil.combinedPath(paths);
	}

	@Override
	public int hashCode() {
		return Objects.hash(paths);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof PathGroup other) && Objects.equals(paths, other.paths);
	}

	@Override
	public String toString() {
		return ToString.ofClass(this, end).childrens(paths).toString();
	}
}
