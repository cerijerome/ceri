package ceri.x10.cm11a.device;

import static ceri.common.test.TestUtil.isObject;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.date.DateUtil;
import ceri.x10.cm11a.protocol.Data;
import ceri.x10.cm11a.protocol.InputBuffer;
import ceri.x10.cm11a.protocol.Protocol;
import ceri.x10.cm11a.protocol.WriteStatus;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandFactory;

@SuppressWarnings("resource")
public class ProcessorBehavior {
	private Cm11aTestConnector connector;
	private BlockingQueue<BaseCommand<?>> inQueue;
	private BlockingQueue<BaseCommand<?>> outQueue;
	private Processor processor;

	@Before
	public void init() {
		connector = new Cm11aTestConnector();
		inQueue = new ArrayBlockingQueue<>(3);
		outQueue = new ArrayBlockingQueue<>(3);
		processor = new Processor(Cm11aDeviceBehavior.config, connector, inQueue, outQueue);
	}

	@After
	public void end() {
		processor.close();
	}

	@Test
	public void shouldHandleInputWhileWaitingForReadySignal() throws InterruptedException {
		inQueue.add(CommandFactory.allUnitsOff('H'));
		assertThat(connector.from.readShortMsb(), is((short) 0x06d0));
		connector.to.writeByte(Data.shortChecksum(0x06d0));
		assertThat(connector.from.readByte(), is((byte) 0));
		//
		connector.to.writeByte(Protocol.DATA_POLL.value);
		BaseCommand<?> command = CommandFactory.allLightsOff('F');
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		InputBuffer buffer = InputBuffer.allFrom(entries).iterator().next();
		assertThat(connector.from.readUbyte(), is((short) Protocol.PC_READY.value));
		buffer.writeTo(connector.to);
		BaseCommand<?> command2 = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(command2));
		//
		connector.to.writeByte(0x55);
		command = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(CommandFactory.allUnitsOff('H')));
	}

	@Test
	public void shouldHandleInputWhileWaitingForChecksum() throws InterruptedException {
		inQueue.add(CommandFactory.dim("O11", 50));
		assertThat(connector.from.readShortMsb(), is((short) 0x0443));
		//
		connector.to.writeByte(Protocol.DATA_POLL.value);
		BaseCommand<?> command = CommandFactory.allLightsOn('F');
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		InputBuffer buffer = InputBuffer.allFrom(entries).iterator().next();
		assertThat(connector.from.readUbyte(), is((short) Protocol.PC_READY.value));
		buffer.writeTo(connector.to);
		BaseCommand<?> command2 = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(command2));
		//
		connector.to.writeByte(Data.shortChecksum(0x0443));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
	}

	@Test
	public void shouldDispatchInputCommand() throws InterruptedException {
		connector.to.writeByte(Protocol.DATA_POLL.value);
		BaseCommand<?> command = CommandFactory.extended("A1", 0x80, 0x7f);
		Collection<Entry> entries = EntryDispatcher.toEntries(command);
		InputBuffer buffer = InputBuffer.allFrom(entries).iterator().next();
		assertThat(connector.from.readUbyte(), is((short) Protocol.PC_READY.value));
		buffer.writeTo(connector.to);
		BaseCommand<?> command2 = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(command2));
	}

	@Test
	public void shouldReturnStatus() {
		connector.to.writeByte(Protocol.TIME_POLL.value);
		long t = System.currentTimeMillis() - 1000; // milliseconds are zeroed
		WriteStatus status = WriteStatus.decode(connector.from);
		assertTrue(DateUtil.epochMilli(status.date) >= t);
	}

	@Test
	public void shouldDispatchCommand() throws InterruptedException {
		inQueue.add(CommandFactory.off("M16"));
		assertThat(connector.from.readShortMsb(), is((short) 0x040c));
		connector.to.writeByte(Data.shortChecksum(0x040c));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
		assertThat(connector.from.readShortMsb(), is((short) 0x0603));
		connector.to.writeByte(Data.shortChecksum(0x0603));
		assertThat(connector.from.readByte(), is((byte) 0));
		connector.to.writeByte(0x55);
		BaseCommand<?> command = outQueue.poll(10000, TimeUnit.MILLISECONDS);
		assertThat(command, isObject(CommandFactory.off("M16")));
	}

}
