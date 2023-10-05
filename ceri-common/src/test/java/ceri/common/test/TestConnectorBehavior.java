package ceri.common.test;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.ValueCondition;
import ceri.common.io.Connector;
import ceri.common.io.StateChange;

public class TestConnectorBehavior {
	private TestConnector con;

	@Before
	public void before() {
		con = TestConnector.of();
	}

	@After
	public void after() throws IOException {
		con.close();
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldEchoToInput() throws IOException {
		con.open();
		con.echoOn();
		con.out().write(bytes(1, 2, 3, 4, 5), 1, 3);
		assertEquals(con.in().available(), 3);
		assertRead(con.in(), 2, 3, 4);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPairConnectors() throws IOException {
		TestConnector con2 = TestConnector.of();
		con.pairWith((Connector) con2);
		con.open();
		con2.open();
		con.out().write(bytes(1, 2, 3));
		assertRead(con2.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToReadIfBroken() {
		con.broken();
		con.in.to.writeBytes(0, 0);
		assertThrown(con.in()::read);
		assertThrown(() -> con.in().read(new byte[1]));
		assertThrown(con.in()::available);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToReadIfNotConnected() {
		con.in.to.writeBytes(0, 0);
		assertThrown(con.in()::read);
		assertThrown(() -> con.in().read(new byte[1]));
		assertThrown(con.in()::available);
	}

	@Test
	public void shouldFailToWriteIfBroken() {
		con.broken();
		assertThrown(() -> con.out().write(0));
		assertThrown(() -> con.out().write(new byte[3]));
	}

	@Test
	public void shouldFailToWriteIfNotConnected() {
		assertThrown(() -> con.out().write(0));
		assertThrown(() -> con.out().write(new byte[3]));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakConnector() throws InterruptedException {
		con.in.to.writeBytes(1, 2, 3);
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = con.listeners().enclose(sync::signal)) {
			con.broken();
			assertEquals(sync.await(), StateChange.broken);
			assertThrown(() -> con.in().read());
			con.broken();
			assertNull(sync.value());
			assertThrown(() -> con.in().read());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFix() throws InterruptedException, IOException {
		con.in.to.writeBytes(1, 2, 3);
		con.broken();
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var enc = con.listeners().enclose(sync::signal)) {
			con.fixed();
			assertEquals(sync.await(), StateChange.fixed);
			con.fixed();
			assertNull(sync.value());
			assertRead(con.in(), 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldResetState() throws IOException {
		con.broken();
		con.in.read.error.setFrom(IOX);
		con.out.write.error.setFrom(IOX);
		con.reset();
		con.open();
		con.in.to.writeBytes(0);
		con.in().available();
		con.in().read();
		con.out().write(0);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		assertFind(con.toString(), ".+");
	}

}
