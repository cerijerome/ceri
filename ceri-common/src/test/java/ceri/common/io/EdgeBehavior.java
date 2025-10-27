package ceri.common.io;

import java.util.Set;
import org.junit.Test;
import ceri.common.data.BinaryState;
import ceri.common.test.Assert;

public class EdgeBehavior {

	@Test
	public void shouldDetermineEdgeFromStateChange() {
		Assert.equal(Edge.from(null), Edge.none);
		Assert.equal(Edge.from(BinaryState.unknown), Edge.none);
		Assert.equal(Edge.from(BinaryState.off), Edge.falling);
		Assert.equal(Edge.from(BinaryState.on), Edge.rising);
	}

	@Test
	public void shouldDetermineSignalLevel() {
		Assert.equal(Edge.level(null), Level.unknown);
		Assert.equal(Edge.level(Edge.none), Level.unknown);
		Assert.equal(Edge.level(Edge.falling), Level.low);
		Assert.equal(Edge.level(Edge.rising), Level.high);
		Assert.equal(Edge.level(Edge.both), Level.unknown);
	}

	@Test
	public void shouldDetermineIfEdgeExists() {
		Assert.equal(Edge.hasAny(null), false);
		Assert.equal(Edge.hasAny(Edge.none), false);
		Assert.equal(Edge.hasAny(Edge.falling), true);
		Assert.equal(Edge.hasAny(Edge.rising), true);
		Assert.equal(Edge.hasAny(Edge.both), true);
	}

	@Test
	public void shouldDetermineIfEdgeContainsAnotherEdge() {
		assertEdgeContains(Edge.none, Edge.none);
		assertEdgeContains(Edge.falling, Edge.falling);
		assertEdgeContains(Edge.rising, Edge.rising);
		assertEdgeContains(Edge.both, Edge.falling, Edge.rising, Edge.both);
	}

	@Test
	public void shouldDetermineIfEdgeContainsStateChange() {
		assertEdgeContains(Edge.none, new BinaryState[0]);
		assertEdgeContains(Edge.falling, BinaryState.off);
		assertEdgeContains(Edge.rising, BinaryState.on);
		assertEdgeContains(Edge.both, BinaryState.off, BinaryState.on);
	}

	private static void assertEdgeContains(Edge edge, Edge... edges) {
		var set = Set.of(edges);
		Assert.no(edge.contains((Edge) null), "%s should not contain null", edge);
		for (var e : Edge.values()) {
			if (set.contains(e)) Assert.yes(edge.contains(e), "%s should contain %s", edge, e);
			else Assert.no(edge.contains(e), "%s should not contain %s", edge, e);
		}
	}

	private static void assertEdgeContains(Edge edge, BinaryState... changes) {
		var set = Set.of(changes);
		Assert.no(edge.contains((BinaryState) null), "%s should not contain null", edge);
		for (var b : BinaryState.values()) {
			if (set.contains(b)) Assert.yes(edge.contains(b), "%s should contain %s", edge, b);
			else Assert.no(edge.contains(b), "%s should not contain %s", edge, b);
		}
	}
}
