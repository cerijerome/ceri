package ceri.aws.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import ceri.common.io.IoUtil;

/**
 * Writes data parts to an output stream in a sequence determine by position.
 * Will store out of sequence data up to a given maximum, to be written in sequence later.
 * Data passed in is expected to be of equal length but is not required.
 */
public class StreamReconstructor implements Closeable {
	private final int maxNonSequentialParts;
	private final Map<Long, byte[]> nonSeqParts = new HashMap<>();
	private final OutputStream out;
	private long position = 0;
	private boolean closed = false;
	private long bytesWritten = 0;

	public static StreamReconstructor forFile(File file, int maxNonSequentialParts) throws IOException {
		return new StreamReconstructor(new BufferedOutputStream(new FileOutputStream(file)), maxNonSequentialParts);
	}
	
	public StreamReconstructor(OutputStream out, int maxNonSequentialParts) {
		this.maxNonSequentialParts = maxNonSequentialParts;
		this.out = out;
	}

	public long bytesWritten() {
		return bytesWritten;
	}

	public long position() {
		return position;
	}

	public boolean closed() {
		return closed;
	}
	
	public boolean isComplete() {
		return closed && nonSeqParts.isEmpty();
	}
	
	/**
	 * Writes data based on position of data part. If out of sequence
	 */
	public void write(long position, byte[] data) throws IOException {
		if (closed) throw new IOException("Stream is closed");
		if (this.position != position) {
			addNonSeq(position, data);
			return;
		}
		writeToFile(data);
		while (checkAndWriteNonSeq());
	}

	@Override
	public void close() throws IOException {
		if (closed) return;
		out.flush();
		IoUtil.close(out);
		closed = true;
		if (!nonSeqParts.isEmpty()) throw new IllegalStateException("Unwritten data exists");
	}

	private void addNonSeq(long position, byte[] data) {
		if (position < this.position) return; // no need to store an earlier part
		if (!nonSeqParts.containsKey(position) && nonSeqParts.size() >= maxNonSequentialParts)
			throw new IllegalStateException("Too many parts out of sequence");
		nonSeqParts.put(position, data);
	}

	private boolean checkAndWriteNonSeq() throws IOException {
		byte[] nonSeqPart = nonSeqParts.remove(position);
		if (nonSeqPart == null) return false;
		writeToFile(nonSeqPart);
		return true;
	}

	private void writeToFile(byte[] data) throws IOException {
		out.write(data);
		position++;
		bytesWritten += data.length;
	}

}
