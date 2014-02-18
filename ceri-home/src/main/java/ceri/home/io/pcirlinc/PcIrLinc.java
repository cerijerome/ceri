package ceri.home.io.pcirlinc;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PcIrLinc implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int BASE16 = 16;
	private static final int RESPONSE_DATA_OFFSET = 5;
	private static final byte[] EMPTY_RESPONSE = new byte[0];
	private static final int PRESET_CMD_LEN = 6;
	private static final int LEARN_CMD_EXTRA_LEN = 9;
	private static final int READ_BUFFER_SIZE = 1024;
	private static final byte[] LEARN_CMD = { 0x03, 0x12, 0x00, 0x00 };
	private final SerialPort serialPort;
	private final PcIrLincProperties properties;

	public PcIrLinc(PcIrLincProperties properties) throws IOException {
		this.properties = properties;
		serialPort = openPort(properties.serialPort(), properties.baud(), properties.timeoutMs());
	}

	@Override
	public void close() {
		serialPort.close();
	}

	public static byte[] hexToBytes(String hex) {
		byte[] data = new byte[hex.length() / 2];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) Integer.parseInt(hex.substring(i * 2, (i * 2) + 2), BASE16);
		return data;
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder buffer = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			String s = Integer.toHexString(b);
			if (s.length() < 2) buffer.append('0');
			buffer.append(s);
		}
		return buffer.toString();
	}

	public void sendPreset(PcIrLincType type, short vendor, PcIrLincButton button)
		throws IOException, InterruptedException {
		logger.debug("Sending: {}, {}, {}", type, vendor, button);
		int i = 0;
		byte[] presetCmd = new byte[PRESET_CMD_LEN];
		presetCmd[i++] = (byte) (presetCmd.length - 1);
		presetCmd[i++] = 1;
		presetCmd[i++] = (byte) ((type.id << 4) | (vendor >> 8));
		presetCmd[i++] = (byte) (vendor & 0xff);
		presetCmd[i++] = (byte) (button.id & 0xff);
		presetCmd[i++] = 0;

		serialPort.setRTS(true);
		Thread.sleep(properties.delayMs());
		serialPort.getOutputStream().flush();
		serialPort.getOutputStream().write(presetCmd);
		serialPort.setRTS(false);
		Thread.sleep(properties.delayMs());

		byte[] response =
			getResponse(serialPort.getInputStream(), properties.delayMs(), properties
				.responseWaitMs());
		verifyResponseCode(response);
	}

	public byte[] learnIr(int waitMs) throws IOException, InterruptedException {
		serialPort.setRTS(true);
		Thread.sleep(properties.delayMs());
		logger.debug("Tx: {}", bytesToHex(LEARN_CMD));
		serialPort.getOutputStream().flush();
		serialPort.getOutputStream().write(LEARN_CMD);
		serialPort.setRTS(false);
		Thread.sleep(properties.delayMs());

		byte[] response = getResponse(serialPort.getInputStream(), properties.delayMs(), waitMs);
		return getResponseData(response);
	}

	public void sendLearnedIr(byte[] code, int count) throws IOException, InterruptedException {
		int i = 0;
		byte[] buffer = new byte[code.length + LEARN_CMD_EXTRA_LEN];
		buffer[i++] = (byte) (code.length + LEARN_CMD_EXTRA_LEN - 1);
		buffer[i++] = 0x10;
		buffer[i++] = 0;
		buffer[i++] = 0;
		buffer[i++] = 0x10;
		buffer[i++] = 0;
		buffer[i++] = 0;
		buffer[i++] = (byte) code.length;
		System.arraycopy(code, 0, buffer, i, code.length);
		i += code.length;
		buffer[i++] = 0;

		serialPort.setRTS(true);
		for (i = 0; i < count; i++) {
			Thread.sleep(properties.delayMs());
			logger.debug("Tx: {}", bytesToHex(buffer));
			serialPort.getOutputStream().flush();
			serialPort.getOutputStream().write(buffer);
		}
		serialPort.setRTS(false);
		Thread.sleep(properties.delayMs());

		byte[] response =
			getResponse(serialPort.getInputStream(), properties.delayMs(), properties
				.responseWaitMs());
		verifyResponseCode(response);
	}

	private SerialPort openPort(String port, int baud, int timeoutMs) throws IOException {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);

			CommPort commPort = portIdentifier.open(getClass().getName(), timeoutMs);
			if (!(commPort instanceof SerialPort)) throw new IllegalArgumentException(port +
				" is not a serial port");

			SerialPort serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			serialPort.getOutputStream().flush();

			int available = serialPort.getInputStream().available();
			serialPort.getInputStream().skip(available);
			return serialPort;
		} catch (NoSuchPortException e) {
			throw new IOException(e);
		} catch (PortInUseException e) {
			throw new IOException(e);
		} catch (UnsupportedCommOperationException e) {
			throw new IOException(e);
		}
	}

	private void verifyResponseCode(byte[] response) throws IOException {
		if (response.length != 2) throw new IOException("Expected 2 byte response: " +
			bytesToHex(response));
		short code = (short) (response[1] << 8 | response[0]);
		if (code != 1) throw new IOException("Error from device: " + code);
	}

	private byte[] getResponseData(byte[] response) throws IOException {
		if (response.length <= RESPONSE_DATA_OFFSET - 1) throw new IOException(
			"Response too short: " + bytesToHex(response));
		byte dataLen = response[RESPONSE_DATA_OFFSET - 1];
		if (response.length != dataLen + RESPONSE_DATA_OFFSET) throw new IOException(
			"Response data length doesn't match: " + bytesToHex(response));
		return Arrays.copyOfRange(response, RESPONSE_DATA_OFFSET, response.length);
	}

	private byte[] getResponse(InputStream in, int waitMs, int maxWaitMs) throws IOException,
		InterruptedException {
		long ms = System.currentTimeMillis();
		while (true) {
			if (in.available() > 0) break;
			if (System.currentTimeMillis() > ms + maxWaitMs) break;
			Thread.sleep(waitMs);
		}
		if (in.available() == 0) return EMPTY_RESPONSE;
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		int count = in.read(buffer);
		if (count == 0) return EMPTY_RESPONSE;
		return Arrays.copyOf(buffer, count);
	}

}
