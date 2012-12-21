package ceri.common.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import ceri.common.io.ByteBufferStream;
import ceri.common.io.FileFilters;
import ceri.common.io.FilenameIterator;
import ceri.common.io.IoUtil;
import ceri.common.util.ToStringHelper;

/**
 * Zips files under given root directory as bytes are read from the input
 * stream. Not thread safe.
 */
public class ZippingInputStream extends FilterInputStream {
	private static final int BUFFER_SIZE_DEF = 32 * 1024; // 32k;
	private final FilenameIterator iterator;
	private final Checksum checksum;
	private final ZipOutputStream zOut;
	private final ByteBufferStream byteBuffer;
	private final byte[] inBuffer;
	private InputStream in = null;

	public ZippingInputStream(File rootDir) {
		this(rootDir, null, BUFFER_SIZE_DEF, BUFFER_SIZE_DEF);
	}

	public ZippingInputStream(File rootDir, Checksum checksum, int outBufferSize, int inBufferSize) {
		super(null);
		if (outBufferSize == 0) outBufferSize = BUFFER_SIZE_DEF;
		if (inBufferSize == 0) inBufferSize = BUFFER_SIZE_DEF;
		byteBuffer = new ByteBufferStream(outBufferSize);
		super.in = byteBuffer.asInputStream();
		iterator = new FilenameIterator(rootDir, FileFilters.FILE);
		this.checksum = checksum;
		OutputStream cOut =
			checksum == null ? byteBuffer : new CheckedOutputStream(byteBuffer, checksum);
		zOut = new ZipOutputStream(cOut);
		inBuffer = new byte[inBufferSize];
	}

	@Override
	public int read() throws IOException {
		if (available() < 1) load(1);
		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (available() < len) load(len);
		return super.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		IoUtil.close(in);
		in = null;
		super.close();
	}

	/**
	 * Returns the current checksum value, or 0 if none specified in the
	 * constructor.
	 */
	public long checksum() {
		if (checksum == null) return 0;
		return checksum.getValue();
	}

	@Override
	public String toString() {
		ToStringHelper helper = ToStringHelper.createByClass(this);
		helper.add(byteBuffer);
		helper.add("checksum", Long.toHexString(checksum()));
		return helper.toString();
	}

	private void load(int count) throws IOException {
		while (available() < count) {
			if (!fileIsOpen()) if (!openFile()) return;
			// Opening the next file would have written bytes to the buffer
			int len = Math.max(Math.min(count - available(), inBuffer.length), 0);
			int n = in.read(inBuffer, 0, len);
			if (n == -1) closeFile();
			else {
				zOut.write(inBuffer, 0, n);
				zOut.flush();
			}
		}
	}

	private boolean fileIsOpen() {
		return in != null;
	}

	private boolean openFile() throws IOException {
		if (!iterator.hasNext()) {
			if (!byteBuffer.closed()) IoUtil.close(zOut);
			return false;
		}
		String filePath = iterator.next();
		ZipEntry entry = new ZipEntry(filePath);
		zOut.putNextEntry(entry);
		File file = new File(iterator.rootDir, filePath);
		in = new BufferedInputStream(new FileInputStream(file));
		return true;
	}

	private void closeFile() throws IOException {
		IoUtil.close(in);
		in = null;
		zOut.flush();
		zOut.closeEntry();
	}

}
