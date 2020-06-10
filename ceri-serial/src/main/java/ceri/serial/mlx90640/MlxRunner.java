package ceri.serial.mlx90640;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.ExceptionTracker;
import ceri.common.util.Timer;
import ceri.log.util.LogUtil;
import ceri.serial.mlx90640.data.MlxDataException;
import ceri.serial.mlx90640.register.ControlRegister1;
import ceri.serial.mlx90640.register.StatusRegister;

/**
 * Generates the temperature grid in one thread, and mediates for other threads to receive the data.
 */
public class MlxRunner implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int RETRY_DELAY_MS_DEF = 1000;
	private final Lock lock = new ReentrantLock();
	private final Condition sync = lock.newCondition();
	private final ExecutorService exec = Executors.newSingleThreadExecutor();
	private final int retryDelayMs;
	private final MlxFrame frame = MlxFrame.of();
	private final Mlx90640 mlx;
	private final ExceptionTracker exceptions = ExceptionTracker.of();
	private Future<?> future = null;
	private boolean successfulInit = false;

	public static MlxRunner of(Mlx90640 mlx) {
		return of(mlx, RETRY_DELAY_MS_DEF);
	}

	public static MlxRunner of(Mlx90640 mlx, int retryDelayMs) {
		return new MlxRunner(mlx, retryDelayMs);
	}

	private MlxRunner(Mlx90640 mlx, int retryDelayMs) {
		this.mlx = mlx;
		this.retryDelayMs = retryDelayMs;
	}

	public void start(MlxRunnerConfig config) {
		stop();
		future = LogUtil.submit(logger, exec, () -> execute(config));
	}

	public void stop() {
		if (future == null) return;
		future.cancel(true);
	}

	public void awaitFrame(MlxFrame frame) throws InterruptedException {
		ConcurrentUtil.execute(lock, () -> {
			sync.await();
			frame.copyFrom(this.frame);
		});
	}

	@Override
	public void close() {
		LogUtil.close(logger, exec);
	}

	/**
	 * Runs in executor thread. Max one thread active at any time.
	 */
	private void execute(MlxRunnerConfig config) {
		exceptions.clear();
		while (true) {
			try {
				init();
				setControlRegister(config);
				executeFrames(config);
				return;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.catching(e);
				BasicUtil.delay(retryDelayMs);
			}
		}
	}

	private void executeFrames(MlxRunnerConfig config) throws IOException {
		Timer timer = Timer.micros(config.refreshRate.timeMicros());
		for (boolean first = true;; first = false) {
			StatusRegister status = mlx.waitForFrame();
			timer.start();
			executeFrame(config, status.lastSubPageNumber(), first);
			waitForRefresh(timer);
		}
	}

	private void executeFrame(MlxRunnerConfig config, int subPage, boolean first)
		throws IOException {
		try {
			mlx.loadFrame(subPage);
			mlx.status(StatusRegister.dataReset());
			updateFrame(config.emissivity, config.tr, first);
		} catch (MlxDataException e) {
			logger.warn(e);
		}
	}

	private void updateFrame(double emissivity, Double tr, boolean first) {
		ConcurrentUtil.execute(lock, () -> {
			mlx.calculateTo(frame, emissivity, tr);
			if (!first) sync.signal();
		});
	}

	private static void waitForRefresh(Timer timer) {
		Timer.Snapshot t = timer.snapshot();
		if (t.expired()) logger.warn("Processing overrun: {}/{} \u00b5s", t.elapsed(), t.period());
		else t.applyRemaining(BasicUtil::delayMicros);
	}

	private void init() throws IOException {
		if (successfulInit) return;
		mlx.init(); // only need to call this once successfully
		successfulInit = true;
	}

	private void setControlRegister(MlxRunnerConfig config) throws IOException {
		ControlRegister1 control1 = mlx.control1().subPagesMode(true).dataHold(false)
			.subPagesRepeat(true).refreshRate(config.refreshRate).resolution(config.resolution)
			.pattern(config.pattern);
		mlx.control1(control1);
	}

}
