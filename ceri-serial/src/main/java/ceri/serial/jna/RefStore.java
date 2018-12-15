package ceri.serial.jna;

import static ceri.common.validation.ValidationUtil.*;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.SafeReadWrite;
import ceri.log.util.LogUtil;

/**
 * Stores objects using identity hash code. Since the references exist in the map, the hash code
 * should be unique. Keeps track of expiration from the map, and reference count. Useful for JNA
 * callbacks to retrieve Java object references.
 */
public class RefStore<T> implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final long EXPIRATION_MS_DEF = TimeUnit.MINUTES.toMillis(30);
	public static final int NULL_TOKEN = 0;
	private static final long NO_EXPIRATION = 0;
	private final ScheduledExecutorService executor;
	private final SafeReadWrite safe = SafeReadWrite.create();
	private final long defaultExpirationMs;
	private final Map<Integer, Ref<T>> refs = new HashMap<>();

	private static class Ref<T> {
		public final T obj;
		private long expirationOffsetMs;
		private long expirationMs;
		private int references;

		Ref(T obj, long expirationOffsetMs) {
			this.obj = obj;
			this.expirationOffsetMs = expirationOffsetMs;
			expirationMs = expirationOffsetMs == NO_EXPIRATION ? NO_EXPIRATION :
				System.currentTimeMillis() + expirationOffsetMs;
			references = 1;
		}

		public Ref<T> updateExpiration() {
			return updateExpiration(System.currentTimeMillis());
		}

		public Ref<T> updateExpiration(long currentTimeMs) {
			if (expirationMs != NO_EXPIRATION) expirationMs = currentTimeMs + expirationOffsetMs;
			return this;
		}

		public boolean expired(long currentTimeMs) {
			return (expirationMs != NO_EXPIRATION) && expirationMs <= currentTimeMs;
		}

		public Ref<T> reference() {
			references++;
			return this;
		}

		public Ref<T> unreference() {
			references--;
			return this;
		}

		public int references() {
			return references;
		}
	}

	public static <T> RefStore<T> of() {
		return of(EXPIRATION_MS_DEF);
	}

	public static <T> RefStore<T> of(long defaultExpirationMs) {
		return of(defaultExpirationMs, defaultExpirationMs / 2);
	}

	public static <T> RefStore<T> of(long defaultExpirationMs, long expirationCycleMs) {
		return new RefStore<>(defaultExpirationMs, expirationCycleMs);
	}

	private RefStore(long defaultExpirationMs, long expirationCycleMs) {
		this.defaultExpirationMs = defaultExpirationMs;
		executor = executor(expirationCycleMs);
	}

	/**
	 * Add a reference to the given object, and return a token to identify it. If the object had
	 * already been added, this will increase the reference count. The object will expire from this
	 * storage after the default expiration time.
	 */
	public int reference(T t) {
		return reference(t, defaultExpirationMs);
	}

	/**
	 * Add a reference to the given object, and return a token to identify it. If the object had
	 * already been added, this will increase the reference count. The object will expire from this
	 * storage after the given expiration time.
	 */
	public int reference(T t, long expirationOffsetMs) {
		if (t == null) return NULL_TOKEN;
		validateMin(expirationOffsetMs, 0);
		Integer id = System.identityHashCode(t);
		safe.write(() -> {
			Ref<T> ref = refs.get(id);
			if (ref == null) {
				ref = new Ref<>(t, expirationOffsetMs);
				refs.put(id, ref);
			} else ref.updateExpiration().reference();
		});
		return id;
	}

	/**
	 * Add a reference to an object using its token. The object is returned, or null if the object
	 * has not been referenced before.
	 */
	public T referenceByToken(int token) {
		if (token == NULL_TOKEN) return null;
		Integer id = token;
		return safe.writeWithReturn(() -> {
			Ref<T> ref = refs.get(id);
			if (ref == null) return null;
			return ref.updateExpiration().reference().obj;
		});
	}

	/**
	 * Decrease the reference count of the object, and remove it from storage if the count reaches
	 * zero. Returns the token of the object, or 0 if the object is not stored.
	 */
	public int unreference(T t) {
		if (t == null) return NULL_TOKEN;
		Integer id = System.identityHashCode(t);
		return safe.writeWithReturn(() -> {
			Ref<T> ref = refs.get(id);
			if (ref == null) return NULL_TOKEN;
			if (ref.unreference().references() <= 0) refs.remove(id);
			return id;
		});
	}

	/**
	 * Decrease the reference count of an object using its token, removing it from storage if the
	 * count reaches zero. Returns the object, or null if the object is not stored.
	 */
	public T unreferenceByToken(int token) {
		if (token == NULL_TOKEN) return null;
		Integer id = token;
		return safe.writeWithReturn(() -> {
			Ref<T> ref = refs.get(id);
			if (ref == null) return null;
			if (ref.unreference().references() <= 0) refs.remove(id);
			return ref.obj;
		});
	}

	/**
	 * Attempt to retrieve an object by its token. Reference count and expiration are unchanged.
	 */
	public T peek(int token) {
		if (token == NULL_TOKEN) return null;
		Integer id = token;
		return safe.read(() -> {
			Ref<T> ref = refs.get(id);
			return ref == null ? null : ref.obj;
		});
	}

	/**
	 * Attempt to retrieve an object by its token. Reference count is unchanged, but expiration is
	 * reset.
	 */
	public T get(int token) {
		if (token == NULL_TOKEN) return null;
		Integer id = token;
		return safe.writeWithReturn(() -> {
			Ref<T> ref = refs.get(id);
			return ref == null ? null : ref.updateExpiration().obj;
		});
	}

	/**
	 * Removes an object from storage, returning its token. Returns 0 if the object was not stored.
	 */
	public int remove(T t) {
		if (t == null) return NULL_TOKEN;
		Integer id = System.identityHashCode(t);
		return safe.writeWithReturn(() -> {
			Ref<T> ref = refs.remove(id);
			return ref == null ? NULL_TOKEN : id;
		});
	}

	/**
	 * Removes an object from storage using its token. The object is returned, or null if the object
	 * was not stored.
	 */
	public T removeByToken(int token) {
		Integer id = token;
		return safe.writeWithReturn(() -> {
			Ref<T> ref = refs.remove(id);
			return ref == null ? null : ref.obj;
		});
	}

	@Override
	public void close() {
		LogUtil.close(logger, executor);
	}

	private ScheduledExecutorService executor(long expirationCycleMs) {
		if (expirationCycleMs == NO_EXPIRATION) return null;
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleWithFixedDelay(this::expireRefs, expirationCycleMs, expirationCycleMs,
			TimeUnit.MILLISECONDS);
		return executor;
	}

	void expireRefs() {
		// First check if any have expired using the read-lock.
		if (!hasExpirations()) return;
		long t0 = System.currentTimeMillis();
		safe.write(() -> {
			Iterator<Map.Entry<Integer, Ref<T>>> i = refs.entrySet().iterator();
			while (i.hasNext()) {
				Ref<T> ref = i.next().getValue();
				if (ref.expired(t0) || ref.references() <= 0) i.remove();
			}
		});
	}

	private boolean hasExpirations() {
		long t = System.currentTimeMillis();
		return safe.read(() -> {
			for (Ref<T> ref : refs.values())
				if (ref.expired(t)) return true;
			return false;
		});
	}

}
