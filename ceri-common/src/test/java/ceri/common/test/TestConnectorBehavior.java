package ceri.common.test;

import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
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
		con.out().write(ArrayUtil.bytes.of(1, 2, 3, 4, 5), 1, 3);
		Assert.equal(con.in().available(), 3);
		Assert.read(con.in(), 2, 3, 4);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPairConnectors() throws IOException {
		TestConnector con2 = TestConnector.of();
		con.pairWith((Connector) con2);
		con.open();
		con2.open();
		con.out().write(ArrayUtil.bytes.of(1, 2, 3));
		Assert.read(con2.out.from, 1, 2, 3);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToReadIfBroken() {
		con.broken();
		con.in.to.writeBytes(0, 0);
		Assert.thrown(con.in()::read);
		Assert.thrown(() -> con.in().read(new byte[1]));
		Assert.thrown(con.in()::available);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToReadIfNotConnected() {
		con.in.to.writeBytes(0, 0);
		Assert.thrown(con.in()::read);
		Assert.thrown(() -> con.in().read(new byte[1]));
		Assert.thrown(con.in()::available);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToWriteIfBroken() {
		con.broken();
		Assert.thrown(() -> con.out().write(0));
		Assert.thrown(() -> con.out().write(new byte[3]));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFailToWriteIfNotConnected() {
		Assert.thrown(() -> con.out().write(0));
		Assert.thrown(() -> con.out().write(new byte[3]));
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldBreakConnector() throws InterruptedException {
		con.in.to.writeBytes(1, 2, 3);
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = con.listeners().enclose(sync::signal)) {
			con.broken();
			Assert.equal(sync.await(), StateChange.broken);
			Assert.thrown(() -> con.in().read());
			con.broken();
			Assert.isNull(sync.value());
			Assert.thrown(() -> con.in().read());
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldFix() throws InterruptedException, IOException {
		con.in.to.writeBytes(1, 2, 3);
		con.broken();
		ValueCondition<StateChange> sync = ValueCondition.of();
		try (var _ = con.listeners().enclose(sync::signal)) {
			con.fixed();
			Assert.equal(sync.await(), StateChange.fixed);
			con.fixed();
			Assert.isNull(sync.value());
			Assert.read(con.in(), 1, 2, 3);
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
		Assert.find(con.toString(), ".+");
	}

}
