package ceri.x10.cm11a;

import static ceri.common.test.TestUtil.isObject;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.x10.cm11a.protocol.Data;
import ceri.x10.cm11a.protocol.InputBuffer;
import ceri.x10.cm11a.protocol.Protocol;
import ceri.x10.cm11a.protocol.WriteStatus;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandFactory;

public class ProcessorBehavior {
	private Cm11aTestConnector connector;
	private BlockingQueue<BaseCommand<?>> inQueue;
	private BlockingQueue<BaseCommand<?>> outQueue;
	private Processor processor;

	@Before
	public void init() throws IOException {
		connector = new Cm11aTestConnector(1, 3000);
		inQueue = new ArrayBlockingQueue<>(3);
		outQueue = new ArrayBlockingQueue<>(3);
		processor =
			Processor.builder(connector, inQueue, outQueue).readPollMs(5).readTimeoutMs(1000)
				.queuePollTimeoutMs(100).maxSendAttempts(2).build();
	}

	@After
	public void end() {
		processor.close();
	}

	@Test
	public void shouldHandleInputWhileWaitingForReadySignal() throws InterruptedException,
		IOException {
		inQueue.add(CommandFactory.allUnitsOff('H'));
		assertThat(connector.from.readShort(), is((short) 0x06d0));
		connector.to.writeByte(Data.shortChecksum(0x06d0));
		assertThat(connector.from.readByte(), is((byte) 0));
		//
		connector.to.writeByte(Protocol.DATA_POLL.value);
		BaseCommand<?> command = CommandFactory.allLightsOff('F');
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		InputBuffer buffer = InputBuffer.create(entries).iterator().next();
		assertThat(connector.from.readByte(), is(Protocol.PC_READY.value));
		buffer.writeTo(connector.to);
		BaseCommand<?> command2 = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(command2));
		//
		connector.to.writeByte(0x55);
		command = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(CommandFactory.allUnitsOff('H')));
	}

	@Test
	public void shouldHandleInputWhileWaitingForChecksum() throws InterruptedException, IOException {
		inQueue.add(CommandFactory.dim("O11", 50));
		assertThat(connector.from.readShort(), is((short) 0x0443));
		//
		connector.to.writeByte(Protocol.DATA_POLL.value);
		BaseCommand<?> command = CommandFactory.allLightsOn('F');
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		InputBuffer buffer = InputBuffer.create(entries).iterator().next();
		assertThat(connector.from.readByte(), is(Protocol.PC_READY.value));
		buffer.writeTo(connector.to);
		BaseCommand<?> command2 = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(command2));
		//
		connector.to.writeByte(Data.shortChecksum(0x0443));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
	}

	@Test
	public void shouldDispatchInputCommand() throws InterruptedException, IOException {
		connector.to.writeByte(Protocol.DATA_POLL.value);
		BaseCommand<?> command = CommandFactory.extended("A1", Byte.MIN_VALUE, Byte.MAX_VALUE);
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		InputBuffer buffer = InputBuffer.create(entries).iterator().next();
		assertThat(connector.from.readByte(), is(Protocol.PC_READY.value));
		buffer.writeTo(connector.to);
		BaseCommand<?> command2 = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(command2));
	}

	@Test
	public void shouldReturnStatus() throws IOException {
		connector.to.writeByte(Protocol.TIME_POLL.value);
		long t = System.currentTimeMillis() - 1000; // milliseconds are zeroed
		WriteStatus status = WriteStatus.readFrom(connector.from);
		assertTrue(status.date.getTime() >= t);
	}

	@Test
	public void shouldDispatchCommand() throws InterruptedException, IOException {
		inQueue.add(CommandFactory.off("M16"));
		assertThat(connector.from.readShort(), is((short) 0x040c));
		connector.to.writeByte(Data.shortChecksum(0x040c));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
		assertThat(connector.from.readShort(), is((short) 0x0603));
		connector.to.writeByte(Data.shortChecksum(0x0603));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
		BaseCommand<?> command = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(CommandFactory.off("M16")));
	}

}