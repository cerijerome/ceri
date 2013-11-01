/*
 * Created on Aug 21, 2004
 */
package ceri.common.zip;

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
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import ceri.common.io.FileFilters;
import ceri.common.io.FileTracker;
import ceri.common.io.FilenameIterator;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

/**
 * I/O utility functions.
 */
public class ZipUtil {

	/**
	 * Creates ZIP data from all files under given directory as byte array.
	 */
	public static byte[] zipAsBytes(File dirToZip) throws IOException {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		zip(dirToZip, bOut, null, 0);
		return bOut.toByteArray();
	}

	/**
	 * Creates ZIP file from all files under given directory.
	 */
	public static void zip(File dirToZip, File outputFile) throws IOException {
		try (OutputStream out = new FileOutputStream(outputFile)) {
			zip(dirToZip, out);
		}
	}

	/**
	 * Creates ZIP data from all files under given directory. Writes to given
	 * output stream.
	 */
	public static void zip(File dirToZip, OutputStream out) throws IOException {
		zip(dirToZip, out, null, 0);
	}

	/**
	 * Creates ZIP data from all files under given directory. Writes to given
	 * output stream. Use null for no checksum, and use 0 for default buffer
	 * size.
	 */
	public static void zip(File dirToZip, OutputStream out, Checksum checksum, int bufferSize)
		throws IOException {
		FilenameIterator iterator = new FilenameIterator(dirToZip, FileFilters.FILE);
		zip(iterator, out, checksum, bufferSize);
	}

	/**
	 * Creates ZIP data from all files under given directory. Writes to given
	 * output stream. Use null for no checksum, and use 0 for default buffer
	 * size.
	 */
	public static void zip(FilenameIterator iterator, OutputStream out, Checksum checksum,
		int bufferSize) throws IOException {
		OutputStream bOut = new BufferedOutputStream(out);
		OutputStream cOut = checksum == null ? bOut : new CheckedOutputStream(bOut, checksum);
		try (ZipOutputStream zOut = new ZipOutputStream(cOut)) {
			for (String filePath : BasicUtil.forEach(iterator)) {
				File file = new File(iterator.rootDir, filePath);
				if (!file.isFile()) continue;
				ZipEntry entry = new ZipEntry(filePath);
				zOut.putNextEntry(entry);
				try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
					IoUtil.transferContent(in, zOut, bufferSize);
				}
				zOut.closeEntry();
			}
			zOut.flush();
		}
	}

	/**
	 * Create files from ZIP under given directory. If an exception occurs files
	 * are cleaned up.
	 */
	public static void unzip(byte[] zipData, File unzipDir) throws IOException {
		unzip(new ByteArrayInputStream(zipData), unzipDir, null, 0);
	}

	/**
	 * Create files from ZIP under given directory. If an exception occurs files
	 * are cleaned up.
	 */
	public static void unzip(File zipFile, File unzipDir) throws IOException {
		try (InputStream in = new FileInputStream(zipFile)) {
			unzip(in, unzipDir);
		}
	}

	/**
	 * Create files from ZIP under given directory. If an exception occurs files
	 * are cleaned up.
	 */
	public static void unzip(InputStream in, File unzipDir) throws IOException {
		unzip(in, unzipDir, null, 0);
	}

	/**
	 * Create files from ZIP under given directory. Use null for no checksum,
	 * and use 0 for default buffer size. If an exception occurs files are
	 * cleaned up. Note: when reading, checksum doesn't always match the one
	 * during writing, especially for large data. Seems to be unread data that
	 * doesn't make it into the checksum, so remaining data is read via skip.
	 */
	public static void unzip(InputStream in, File unzipDir, Checksum checksum, int bufferSize)
		throws IOException {
		InputStream cIn = checksum == null ? in : new CheckedInputStream(in, checksum);
		InputStream bIn = new BufferedInputStream(cIn);
		ZipEntry entry;
		FileTracker tracker = new FileTracker();
		try (ZipInputStream zIn = new ZipInputStream(bIn)) {
			while ((entry = zIn.getNextEntry()) != null) {
				File file = new File(unzipDir, entry.getName());
				if (entry.isDirectory()) {
					tracker.dir(file);
				} else {
					tracker.file(file);
					try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
						IoUtil.transferContent(zIn, out, bufferSize);
					}
				}
				zIn.closeEntry();
			}
			if (checksum != null) while (cIn.skip(bufferSize) > 0) {} // read remaining for checksum 
		} catch (IOException e) {
			tracker.delete();
			throw e;
		} catch (RuntimeException e) {
			tracker.delete();
			throw e;
		}
	}

}
