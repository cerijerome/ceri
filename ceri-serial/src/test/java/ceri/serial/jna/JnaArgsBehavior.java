package ceri.serial.jna;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertMatch;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.sun.jna.Callback;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

public class JnaArgsBehavior {

	static interface TestCallback extends Callback {
		boolean invoke(String s, int i);
	}

	@Test
	public void shouldExpandArrays() {
		assertEquals(
			JnaArgs.DEFAULT.args(new int[][] { { 1 }, { 2, 3 } }, new double[] { 1.1, 2.2 }, "x"),
			"[[1], [2, 3]], [1.1, 2.2], x");
	}

	@Test
	public void shouldExpandIterable() {
		assertEquals(JnaArgs.DEFAULT.args(Set.of(Set.of(1)), List.of(2, 3, new int[] { 4, 5 })),
			"[[1]], [2, 3, [4, 5]]");
	}

	@Test
	public void shouldPrintPointer() {
		Memory m = new Memory(3);
		long peer = PointerUtil.peer(m);
		Pointer p = PointerUtil.pointer(peer + 1);
		assertEquals(JnaArgs.DEFAULT.args(m, p), String.format("@%x+3, @%x", peer, peer + 1));
	}

	@Test
	public void shouldPrintCallback() {
		TestCallback callback = (s, i) -> s.length() == i;
		assertMatch(JnaArgs.string(callback), "%s\\$\\$Lambda.*@\\w+", getClass().getSimpleName());
	}

	@Test
	public void shouldPrintHexIntIfInRange() {
		assertEquals(JnaArgs.DEFAULT.args(-256, -16, -15, 15, 16, 256),
			"-256/0xffffff00, -16/0xfffffff0, -15, 15, 16/0x10, 256/0x100");
	}

	@Test
	public void shouldMatchClassesToFormat() {
		var args =
			JnaArgs.builder().add(JnaArgs.matchClass(Byte.class, Short.class), "0x%04x").build();
		assertEquals(args.args((byte) 11, (short) 11, 11), "0x000b, 0x000b, 11");
	}

	@Test
	public void shouldMatchClassToFormat() {
		var args = JnaArgs.builder().add(Byte.class, "0x%02x").build();
		assertEquals(args.args((byte) 11, 11), "0x0b, 11");
	}

	@Test
	public void shouldMatchPredicateToFormat() {
		var args = JnaArgs.builder().add(arg -> arg instanceof String s && s.startsWith("!"), "%S")
			.build();
		assertEquals(args.args("x", "!x"), "x, !X");
	}

}
