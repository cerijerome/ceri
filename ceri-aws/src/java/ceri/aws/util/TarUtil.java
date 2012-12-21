package ceri.aws.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import ceri.common.collection.MapBuilder;
import ceri.common.io.FileTracker;
import ceri.common.io.FilenameIterator;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

public class TarUtil {
	public static enum Compression {
		none,
		gzip,
		bzip2,
	}

	private static final Map<String, Compression> SUFFIX_MAP =
		new MapBuilder<String, Compression>().putKeys(Compression.gzip, ".tgz", ".tar.gz").putKeys(
			Compression.bzip2, ".tbz", ".tbz2", ".tb2", ".tar.bz2").build();

	private TarUtil() {}

	/**
	 * Creates tar data from all files under given directory as byte array.
	 */
	public static byte[] tarAsBytes(File dirToTar, Compression compression) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		tar(dirToTar, bOut, compression);
		return bOut.toByteArray();
	}

	/**
	 * Creates tar data from all files under given directory. Writes to given
	 * output stream.
	 */
	public static void tar(File dirToTar, File tarFile, Compression compression) throws IOException {
		try (OutputStream out = new FileOutputStream(tarFile)) {
			tar(dirToTar, out, compression, null, 0);
		}
	}

	/**
	 * Creates tar data from all files under given directory. Writes to given
	 * output stream.
	 */
	public static void tar(File dirToTar, OutputStream out, Compression compression)
		throws IOException {
		tar(dirToTar, out, compression, null, 0);
	}

	/**
	 * Creates tar data from all files under given directory. Writes to given
	 * output stream. Use null for no checksum, and use 0 for default buffer
	 * size.
	 */
	public static void tar(File dirToTar, OutputStream out, Compression compression,
		Checksum checksum, int bufferSize) throws IOException {
		FilenameIterator iterator = new FilenameIterator(dirToTar);
		tar(iterator, out, compression, checksum, bufferSize);
	}
	
	/**
	 * Creates tar data from all files from given iterator. Writes to given
	 * output stream. Use null for no checksum, and use 0 for default buffer
	 * size.
	 */
	public static void tar(FilenameIterator iterator, OutputStream out, Compression compression,
		Checksum checksum, int bufferSize) throws IOException {
		OutputStream bOut = new BufferedOutputStream(out);
		OutputStream cOut = checksum == null ? bOut : new CheckedOutputStream(bOut, checksum);
		OutputStream zOut = compressedOutputStream(cOut, compression);
		try (TarArchiveOutputStream tOut = new TarArchiveOutputStream(zOut)) {
			for (String filePath : BasicUtil.forEach(iterator)) {
				File file = new File(iterator.rootDir, filePath);
				TarArchiveEntry entry = new TarArchiveEntry(file);
				entry.setName(filePath);
				tOut.putArchiveEntry(entry);
				if (file.isFile()) {
					try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
						IoUtil.transferContent(in, tOut, bufferSize);
					}
				}
				tOut.closeArchiveEntry();
			}
			tOut.finish();
		}
	}

	/**
	 * Create files from tar under given directory. If an exception occurs files
	 * are cleaned up.
	 */
	public static void untar(byte[] tarData, File untarDir, Compression compression)
		throws IOException {
		untar(new ByteArrayInputStream(tarData), untarDir, compression);
	}

	/**
	 * Create files from tar under given directory, guessing if compression is
	 * needed from file suffix. If an exception occurs files are cleaned up.
	 */
	public static void untar(File tarFile, File untarDir) throws IOException {
		try (InputStream in = new FileInputStream(tarFile)) {
			untar(in, untarDir, compressionFromFilename(tarFile.getName()));
		}
	}

	/**
	 * Create files from tar under given directory. If an exception occurs files
	 * are cleaned up.
	 */
	public static void untar(File tarFile, File untarDir, Compression compression)
		throws IOException {
		try (InputStream in = new FileInputStream(tarFile)) {
			untar(in, untarDir, compression);
		}
	}

	/**
	 * Create files from tar under given directory. If an exception occurs files
	 * are cleaned up.
	 */
	public static void untar(InputStream in, File untarDir, Compression compression)
		throws IOException {
		untar(in, untarDir, compression, null, 0);
	}

	/**
	 * Create files from tar under given directory. Use null for no checksum,
	 * and use 0 for default buffer size. If an exception occurs files are
	 * cleaned up. Note: when reading, checksum doesn't always match the one
	 * during writing. Seems to be unread data that doesn't make it into the
	 * checksum.
	 */
	public static void untar(InputStream in, File untarDir, Compression compression,
		Checksum checksum, int bufferSize) throws IOException {
		InputStream cIn = checksum == null ? in : new CheckedInputStream(in, checksum);
		InputStream bIn = new BufferedInputStream(cIn);
		InputStream zIn = compressedInputStream(bIn, compression);
		TarArchiveEntry entry;
		FileTracker tracker = new FileTracker();
		try (TarArchiveInputStream tIn = new TarArchiveInputStream(zIn)) {
			while ((entry = tIn.getNextTarEntry()) != null) {
				File file = new File(untarDir, entry.getName());
				if (entry.isDirectory()) {
					tracker.dir(file);
				} else {
					tracker.file(file);
					try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
						IoUtil.transferContent(tIn, out, bufferSize);
					}
				}
			}
			byte[] buffer = new byte[bufferSize];
			while (true) {
				int count = tIn.read(buffer);
				if (count == -1) break;
			}
		} catch (IOException e) {
			tracker.delete();
			throw e;
		} catch (RuntimeException e) {
			tracker.delete();
			throw e;
		}
	}

	public static Compression compressionFromFilename(String filename) {
		for (Map.Entry<String, Compression> entry : SUFFIX_MAP.entrySet())
			if (filename.endsWith(entry.getKey())) return entry.getValue();
		return Compression.none;
	}

	public static InputStream compressedInputStream(InputStream in, Compression compression)
		throws IOException {
		if (compression == null) return in;
		switch (compression) {
		case none:
			return in;
		case gzip:
			return new GzipCompressorInputStream(in);
		case bzip2:
			return new BZip2CompressorInputStream(in);
		}
		return in;
	}

	public static OutputStream compressedOutputStream(OutputStream out, Compression compression)
		throws IOException {
		if (compression == null) return out;
		switch (compression) {
		case gzip:
			return new GzipCompressorOutputStream(out);
		case bzip2:
			return new BZip2CompressorOutputStream(out);
		default:
			return out;
		}
	}

}
