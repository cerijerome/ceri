package ceri.common.io;

import java.util.Set;
import org.junit.jupiter.api.Test;
import ceri.common.data.BinaryState;
import ceri.common.test.Assert;

public class SignalBehavior {

	@Test
	public void shouldDetermineEdgeFromStateChange() {
		Assert.equal(Signal.edge(null), Signal.Edge.none);
		Assert.equal(Signal.edge(BinaryState.unknown), Signal.Edge.none);
		Assert.equal(Signal.edge(BinaryState.off), Signal.Edge.falling);
		Assert.equal(Signal.edge(BinaryState.on), Signal.Edge.rising);
	}

	@Test
	public void shouldDetermineEdgeLevel() {
		Assert.equal(Signal.level(null), Signal.Level.unknown);
		Assert.equal(Signal.level(Signal.Edge.none), Signal.Level.unknown);
		Assert.equal(Signal.level(Signal.Edge.falling), Signal.Level.low);
		Assert.equal(Signal.level(Signal.Edge.rising), Signal.Level.high);
		Assert.equal(Signal.level(Signal.Edge.both), Signal.Level.unknown);
	}

	@Test
	public void shouldDetermineIfEdgeExists() {
		Assert.equal(Signal.Edge.hasAny(null), false);
		Assert.equal(Signal.Edge.hasAny(Signal.Edge.none), false);
		Assert.equal(Signal.Edge.hasAny(Signal.Edge.falling), true);
		Assert.equal(Signal.Edge.hasAny(Signal.Edge.rising), true);
		Assert.equal(Signal.Edge.hasAny(Signal.Edge.both), true);
	}

	@Test
	public void shouldDetermineIfEdgeContainsAnotherEdge() {
		assertEdgeContains(Signal.Edge.none, Signal.Edge.none);
		assertEdgeContains(Signal.Edge.falling, Signal.Edge.falling);
		assertEdgeContains(Signal.Edge.rising, Signal.Edge.rising);
		assertEdgeContains(Signal.Edge.both, Signal.Edge.falling, Signal.Edge.rising,
			Signal.Edge.both);
	}

	@Test
	public void shouldDetermineIfEdgeContainsStateChange() {
		assertEdgeContains(Signal.Edge.none, new BinaryState[0]);
		assertEdgeContains(Signal.Edge.falling, BinaryState.off);
		assertEdgeContains(Signal.Edge.rising, BinaryState.on);
		assertEdgeContains(Signal.Edge.both, BinaryState.off, BinaryState.on);
	}

	@Test
	public void shouldDetermineLevelFromValue() {
		Assert.equal(Signal.level(-2), Signal.Level.unknown);
		Assert.equal(Signal.level(-1), Signal.Level.unknown);
		Assert.equal(Signal.level(0), Signal.Level.low);
		Assert.equal(Signal.level(1), Signal.Level.high);
		Assert.equal(Signal.level(2), Signal.Level.high);
	}

	@Test
	public void shouldDetermineValueFromLevel() {
		Assert.equal(Signal.Level.value(null), -1);
		Assert.equal(Signal.Level.value(Signal.Level.unknown), -1);
		Assert.equal(Signal.Level.value(Signal.Level.low), 0);
		Assert.equal(Signal.Level.value(Signal.Level.high), 1);
	}

	@Test
	public void shouldDetermineIfKnownLevel() {
		Assert.equal(Signal.Level.known(null), false);
		Assert.equal(Signal.Level.known(Signal.Level.unknown), false);
		Assert.equal(Signal.Level.known(Signal.Level.low), true);
		Assert.equal(Signal.Level.known(Signal.Level.high), true);
	}

	@Test
	public void shouldInvertLevel() {
		Assert.equal(Signal.invert(null), Signal.Level.unknown);
		Assert.equal(Signal.invert(Signal.Level.unknown), Signal.Level.unknown);
		Assert.equal(Signal.invert(Signal.Level.low), Signal.Level.high);
		Assert.equal(Signal.invert(Signal.Level.high), Signal.Level.low);
	}

	@Test
	public void shouldDetermineStateFromActiveLevel() {
		Assert.equal(Signal.activeState(null, null), BinaryState.unknown);
		Assert.equal(Signal.activeState(null, Signal.Level.unknown), BinaryState.unknown);
		Assert.equal(Signal.activeState(null, Signal.Level.low), BinaryState.unknown);
		Assert.equal(Signal.activeState(null, Signal.Level.high), BinaryState.unknown);
		Assert.equal(Signal.activeState(Signal.Level.unknown, null), BinaryState.unknown);
		Assert.equal(Signal.activeState(Signal.Level.unknown, Signal.Level.unknown),
			BinaryState.unknown);
		Assert.equal(Signal.activeState(Signal.Level.unknown, Signal.Level.low),
			BinaryState.unknown);
		Assert.equal(Signal.activeState(Signal.Level.unknown, Signal.Level.high),
			BinaryState.unknown);
		Assert.equal(Signal.activeState(Signal.Level.low, null), BinaryState.off);
		Assert.equal(Signal.activeState(Signal.Level.low, Signal.Level.unknown), BinaryState.off);
		Assert.equal(Signal.activeState(Signal.Level.low, Signal.Level.low), BinaryState.on);
		Assert.equal(Signal.activeState(Signal.Level.low, Signal.Level.high), BinaryState.off);
		Assert.equal(Signal.activeState(Signal.Level.high, null), BinaryState.on);
		Assert.equal(Signal.activeState(Signal.Level.high, Signal.Level.unknown), BinaryState.on);
		Assert.equal(Signal.activeState(Signal.Level.high, Signal.Level.low), BinaryState.off);
		Assert.equal(Signal.activeState(Signal.Level.high, Signal.Level.high), BinaryState.on);
	}

	@Test
	public void shouldDetermineStateFromLevel() {
		Assert.equal(Signal.state(null), BinaryState.unknown);
		Assert.equal(Signal.state(Signal.Level.unknown), BinaryState.unknown);
		Assert.equal(Signal.state(Signal.Level.low), BinaryState.off);
		Assert.equal(Signal.state(Signal.Level.high), BinaryState.on);
	}

	private static void assertEdgeContains(Signal.Edge edge, Signal.Edge... edges) {
		var set = Set.of(edges);
		Assert.no(edge.contains((Signal.Edge) null), "%s should not contain null", edge);
		for (var e : Signal.Edge.values()) {
			if (set.contains(e)) Assert.yes(edge.contains(e), "%s should contain %s", edge, e);
			else Assert.no(edge.contains(e), "%s should not contain %s", edge, e);
		}
	}

	private static void assertEdgeContains(Signal.Edge edge, BinaryState... changes) {
		var set = Set.of(changes);
		Assert.no(edge.contains((BinaryState) null), "%s should not contain null", edge);
		for (var b : BinaryState.values()) {
			if (set.contains(b)) Assert.yes(edge.contains(b), "%s should contain %s", edge, b);
			else Assert.no(edge.contains(b), "%s should not contain %s", edge, b);
		}
	}
}
