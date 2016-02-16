package ceri.common.svg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import ceri.common.collection.ImmutableUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.geom.Line2d;
import ceri.common.geom.Point2d;
import ceri.common.geom.Ratio2d;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class PathGroup implements Path<PathGroup> {
	public final List<Path<?>> paths;
	private final Position end;

	public static PathGroup of(Path<?>... paths) {
		return of(Arrays.asList(paths));
	}

	public static PathGroup of(Collection<Path<?>> paths) {
		return new PathGroup(paths);
	}

	private PathGroup(Collection<Path<?>> paths) {
		this.paths = ImmutableUtil.copyAsList(paths);
		end = SvgUtil.combinedEnd(paths);
	}

	@Override
	public PathGroup reverse() {
		List<Path<?>> list = new ArrayList<>(paths);
		Collections.reverse(list);
		return of(StreamUtil.toList(list.stream().map(path -> path.reverse())));
	}

	@Override
	public PathGroup reflect(Line2d line) {
		return of(StreamUtil.toList(paths.stream().map(path -> path.reflect(line))));
	}

	@Override
	public PathGroup scale(Ratio2d scale) {
		return of(StreamUtil.toList(paths.stream().map(path -> path.scale(scale))));
	}

	@Override
	public PathGroup translate(Point2d offset) {
		return of(StreamUtil.toList(paths.stream().map(path -> path.translate(offset))));
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
		return HashCoder.hash(paths);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof PathGroup)) return false;
		PathGroup other = (PathGroup) obj;
		if (!EqualsUtil.equals(paths, other.paths)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, end).childrens(paths).toString();
	}

}
