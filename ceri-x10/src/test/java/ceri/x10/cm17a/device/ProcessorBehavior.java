package ceri.x10.cm17a.device;

import static ceri.common.test.TestUtil.isObject;
import static ceri.x10.util.X10TestUtil.addr;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import ceri.x10.command.Command;

public class ProcessorBehavior {
	private Cm17aConnector connector;
	private BlockingQueue<Command> inQueue;
	private BlockingQueue<Command> outQueue;
	private Processor processor;

	@Before
	public void init() {
		connector = mock(Cm17aConnector.class);
		inQueue = new ArrayBlockingQueue<>(3);
		outQueue = new ArrayBlockingQueue<>(3);
		Cm17aDeviceConfig config = Cm17aDeviceBehavior.config;
		processor = new Processor(config, connector, inQueue, outQueue);
	}

	@After
	public void end() {
		processor.close();
	}

	@Test
	public void shouldDispatchCommands() throws InterruptedException {
		inQueue.add(Command.on(addr("M16")));
		inQueue.add(Command.dim(addr("A1"), 5));
		Command command = outQueue.poll(3000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(Command.on(addr("M16"))));
		command = outQueue.poll(3000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(Command.dim(addr("A1"), 5)));
	}

	@Test
	public void shouldSendCommandsToConnector() throws InterruptedException, IOException {
		inQueue.add(Command.off(addr("J10")));
		Command command = outQueue.poll(3000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(Command.off(addr("J10"))));
		InOrder inOrder = inOrder(connector);
		assertReset(inOrder);
		assertBits(inOrder, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0); // Header
		// J10 OFF 11110100 00110000
		assertBits(inOrder, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0); // Command
		assertBits(inOrder, 1, 0, 1, 0, 1, 1, 0, 1); // Footer
	}

	private void assertBits(InOrder inOrder, int... bits) throws IOException {
		for (int bit : bits) {
			if (bit == 1) {
				inOrder.verify(connector).setDtr(false);
				inOrder.verify(connector).setDtr(true);
			} else {
				inOrder.verify(connector).setRts(false);
				inOrder.verify(connector).setRts(true);
			}
		}
	}

	private void assertReset(InOrder inOrder) throws IOException {
		inOrder.verify(connector).setDtr(false);
		inOrder.verify(connector).setRts(false);
		inOrder.verify(connector).setDtr(true);
		inOrder.verify(connector).setRts(true);
	}

}
