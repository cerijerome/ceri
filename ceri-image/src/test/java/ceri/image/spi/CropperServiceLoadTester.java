package ceri.image.spi;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.image.ebay.EpsImageType;
import ceri.image.test.TestImage;

/**
 * Sets up a test for a CropperService with multiple threads running for a given period of time.
 * Collects statistics for the test run.
 */
public class CropperServiceLoadTester {
	private static final Pattern epsPattern = Pattern.compile("http://([^/]+\\.ebayimg\\.com/.*)");
	private static final EpsImageType epsImageType = EpsImageType._3; // 800x800
	private static final List<String> epsPaths = epsPaths();
	private static Random rnd = new SecureRandom();
	private final CropperService service;
	private final int threads;
	private final int sleepMs;
	private final List<String> keys;
	private long startTime = 0;
	private volatile int totalDownloads = 0;
	private volatile long totalTimeMs = 0;

	public CropperServiceLoadTester(CropperService service, int threads, int sleepMs) {
		this.service = service;
		this.threads = threads;
		this.sleepMs = sleepMs;
		keys = new ArrayList<>(service.keys());
	}

	public static void main(String[] args) throws Exception {
		CropperService service = DefaultCropperService.create();
		CropperServiceLoadTester tester = new CropperServiceLoadTester(service, 10, 100);
		tester.run(5 * 1000);
	}

	public void printStats() {
		System.out.println("Downloads:    " + totalDownloads);
		long t = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed time: " + t + "ms");
		System.out.println("Time taken:   " + totalTimeMs + "ms");
		if (totalDownloads > 0 && t > 0) {
			System.out.println("Average time: " + totalTimeMs / totalDownloads + "ms");
			System.out.println("Throughput:   " + (totalDownloads * 1000) / t + " downloads/sec");
		}
	}

	public void run(final long maxTimeMs) throws InterruptedException {
		System.out.println("Test started (threads=" + threads + ", time=" + maxTimeMs + "ms)");
		startTime = System.currentTimeMillis();
		List<Thread> threadList = new ArrayList<>();
		for (int i = 0; i < threads; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					execute(maxTimeMs);
				}
			});
			threadList.add(thread);
			thread.start();
		}
		while (true) {
			boolean complete = true;
			for (Thread thread : threadList) {
				if (!thread.isAlive()) continue;
				complete = false;
				break;
			}
			if (complete) break;
			Thread.sleep(1000);
			long t = System.currentTimeMillis() - startTime;
			System.out.println(totalDownloads + ":" + t + "ms");
		}
		System.out.println();
		System.out.println("Test complete");
		printStats();
	}

	void execute(long maxTimeMs) {
		long endTime = System.currentTimeMillis() + maxTimeMs;
		try {
			while (System.currentTimeMillis() < endTime) {
				String key = keys.get(rnd.nextInt(keys.size()));
				String epsPath = epsPaths.get(rnd.nextInt(epsPaths.size()));
				String path = key + "/" + epsPath;
				long t = System.currentTimeMillis();
				System.out.print('.');
				service.cropImage(path);
				t = System.currentTimeMillis() - t;
				totalDownloads++;
				totalTimeMs += t;
				Thread.yield();
				Thread.sleep(rnd.nextInt(sleepMs - 1) + 1);
			}
		} catch (CropperServiceException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static List<String> epsPaths() {
		List<String> epsUrls = new ArrayList<>();
		for (TestImage testImage : TestImage.values()) {
			if (testImage.source == null) continue;
			Matcher m = epsPattern.matcher(epsImageType.url(testImage.source));
			if (m.find()) epsUrls.add(m.group(1));
		}
		return Collections.unmodifiableList(epsUrls);
	}

}
