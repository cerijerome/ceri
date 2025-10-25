package ceri.common.io;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertTrue;
import java.util.Set;
import org.junit.Test;
import ceri.common.data.BinaryState;

public class EdgeBehavior {

	@Test
	public void shouldDetermineEdgeFromStateChange() {
		assertEquals(Edge.from(null), Edge.none);
		assertEquals(Edge.from(BinaryState.unknown), Edge.none);
		assertEquals(Edge.from(BinaryState.off), Edge.falling);
		assertEquals(Edge.from(BinaryState.on), Edge.rising);
	}

	@Test
	public void shouldDetermineSignalLevel() {
		assertEquals(Edge.level(null), Level.unknown);
		assertEquals(Edge.level(Edge.none), Level.unknown);
		assertEquals(Edge.level(Edge.falling), Level.low);
		assertEquals(Edge.level(Edge.rising), Level.high);
		assertEquals(Edge.level(Edge.both), Level.unknown);
	}

	@Test
	public void shouldDetermineIfEdgeExists() {
		assertEquals(Edge.hasAny(null), false);
		assertEquals(Edge.hasAny(Edge.none), false);
		assertEquals(Edge.hasAny(Edge.falling), true);
		assertEquals(Edge.hasAny(Edge.rising), true);
		assertEquals(Edge.hasAny(Edge.both), true);
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
		assertFalse(edge.contains((Edge) null), "%s should not contain null", edge);
		for (var e : Edge.values()) {
			if (set.contains(e)) assertTrue(edge.contains(e), "%s should contain %s", edge, e);
			else assertFalse(edge.contains(e), "%s should not contain %s", edge, e);
		}
	}

	private static void assertEdgeContains(Edge edge, BinaryState... changes) {
		var set = Set.of(changes);
		assertFalse(edge.contains((BinaryState) null), "%s should not contain null", edge);
		for (var b : BinaryState.values()) {
			if (set.contains(b)) assertTrue(edge.contains(b), "%s should contain %s", edge, b);
			else assertFalse(edge.contains(b), "%s should not contain %s", edge, b);
		}
	}
}
