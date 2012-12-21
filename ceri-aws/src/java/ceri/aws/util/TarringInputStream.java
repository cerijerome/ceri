package ceri.aws.util;

import static ceri.common.test.Debugger.DBG;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import ceri.aws.util.TarUtil.Compression;
import ceri.common.io.ByteBufferStream;
import ceri.common.io.FilenameIterator;
import ceri.common.io.IoUtil;
import ceri.common.util.ToStringHelper;

/**
 * Zips files under given root directory as bytes are read from the input
 * stream. Not thread safe.
 */
public class TarringInputStream extends FilterInputStream {
	private static final int BUFFER_SIZE_DEF = 32 * 1024; // 32k;
	private final FilenameIterator fileIterator;
	private final Checksum checksum;
	private final TarArchiveOutputStream tOut;
	private final ByteBufferStream byteBuffer;
	private final byte[] inBuffer;
	private InputStream in = null;

	public TarringInputStream(File dirToTar, Compression compression) throws IOException {
		this(new FilenameIterator(dirToTar), compression, null, BUFFER_SIZE_DEF, BUFFER_SIZE_DEF);
	}

	/**
	 * Creates an input stream to iterate over files. OutBufferSize is the initial size
	 * of the output byte buffer. InBufferSize is the buffer used for reading file content.
	 */
	public TarringInputStream(FilenameIterator fileIterator, Compression compression,
		Checksum checksum, int outBufferSize, int inBufferSize) throws IOException {
		super(null);
		if (outBufferSize == 0) outBufferSize = BUFFER_SIZE_DEF;
		if (inBufferSize == 0) inBufferSize = BUFFER_SIZE_DEF;
		byteBuffer = new ByteBufferStream(outBufferSize);
		super.in = byteBuffer.asInputStream();
		this.fileIterator = fileIterator;
		this.checksum = checksum;
		OutputStream cOut =
			checksum == null ? byteBuffer : new CheckedOutputStream(byteBuffer, checksum);
		OutputStream zOut = TarUtil.compressedOutputStream(cOut, compression);
		tOut = new TarArchiveOutputStream(zOut);
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
			int n = in.read(inBuffer);
			if (n == -1) closeFile();
			else {
				tOut.write(inBuffer, 0, n);
				tOut.flush();
			}
		}
	}

	private boolean fileIsOpen() {
		return in != null;
	}

	private boolean openFile() throws IOException {
		if (!fileIterator.hasNext()) {
			if (!byteBuffer.closed()) {
				tOut.finish();
				IoUtil.close(tOut);
			}
			return false;
		}
		String filePath = fileIterator.next();
		DBG.log("open: " + filePath);
		File file = new File(fileIterator.rootDir, filePath);
		TarArchiveEntry entry = new TarArchiveEntry(file);
		entry.setName(filePath);
		tOut.putArchiveEntry(entry);
		if (!file.isFile()) {
			closeFile();
			return openFile();
		}
		in = new BufferedInputStream(new FileInputStream(file));
		return true;
	}

	private void closeFile() throws IOException {
		IoUtil.close(in);
		in = null;
		tOut.flush();
		tOut.closeArchiveEntry();
	}

}
