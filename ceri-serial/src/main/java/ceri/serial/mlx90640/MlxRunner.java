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
import ceri.common.util.Timer;
import ceri.log.util.LogUtil;

/**
 * Generates the temperature grid in one thread, and mediates for other threads to receive the data.
 */
public class MlxRunner implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Lock lock = new ReentrantLock();
	private final Condition sync = lock.newCondition();
	private final ExecutorService exec = Executors.newSingleThreadExecutor();
	private final Mlx90640 mlx;
	private final MlxFrame frame;
	private Future<?> future = null;

	// TODO:
	// check write register verify yes/no
	// add try-catch for errors
	// reset i2c if i2c error?
	// add getimage
	// add pixel fixes
	// check mlx/adafruit algorithm for averages/fixes

	public static MlxRunner of(Mlx90640 mlx) {
		return new MlxRunner(mlx);
	}

	private MlxRunner(Mlx90640 mlx) {
		this.mlx = mlx;
		frame = MlxFrame.of();
	}

	public void start(RefreshRate refreshRate, Resolution resolution, ReadingPattern mode,
		double emissivity, Double tr) {
		stop();
		future = LogUtil.submit(logger, exec,
			() -> execute(refreshRate, resolution, mode, emissivity, tr));
	}

	public void stop() {
		if (future != null) future.cancel(true);
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

	private void execute(RefreshRate refreshRate, Resolution resolution, ReadingPattern mode,
		double emissivity, Double tr) throws IOException {
		mlx.init();
		mlx.control1(mlx.control1().subPagesMode(true).dataHold(false).subPagesRepeat(true)
			.refreshRate(refreshRate).resolution(resolution).pattern(mode));
		Timer timer = Timer.micros(refreshRate.timeMicros());
		while (true) {
			StatusRegister status = mlx.waitForData(-1);
			timer.start();
			ControlRegister1 control1 = mlx.control1();
			mlx.loadFrame(status.lastSubPageNumber(), control1);
			mlx.status(StatusRegister.dataReset());
			updateFrame(emissivity, tr);
			waitForRefresh(timer);
		}
	}

	private void updateFrame(double emissivity, Double tr) throws IOException {
		ConcurrentUtil.execute(lock, () -> {
			mlx.calculateTo(frame, emissivity, tr);
			sync.signal();
		});
	}

	private static void waitForRefresh(Timer timer) {
		Timer.Snapshot t = timer.snapshot();
		if (t.expired()) logger.warn("Processing overrun: {}/{} \u00b5s", t.elapsed(), t.period());
		else t.applyRemaining(BasicUtil::delayMicros);
	}

}
