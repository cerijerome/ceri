package ceri.common.test;

/**
 * Runnable that can throw exceptions. Used with TestUtil.assertException
 */
public interface TestRunnable {
	void run() throws Exception;
}