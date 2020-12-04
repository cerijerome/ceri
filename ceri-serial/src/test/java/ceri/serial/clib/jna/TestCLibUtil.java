package ceri.serial.clib.jna;

import java.util.function.Function;
import java.util.function.Predicate;
import com.sun.jna.LastErrorException;
import ceri.common.test.CallSync;
import ceri.common.text.StringUtil;

public class TestCLibUtil {

	private TestCLibUtil() {}
	
	public static <T, R> void autoError(CallSync.Apply<T, R> sync,
		R response, Predicate<T> predicate, String errorMessage, Object...args) {
		autoError(sync, response, predicate, t -> StringUtil.format(errorMessage, args));
	}	
	
	public static <T, R> void autoError(CallSync.Apply<T, R> sync,
		R response, Predicate<T> predicate, Function<T, String> errorMessageFn) {
		sync.autoResponse(t -> {
			if (predicate.test(t)) return response;
			throw new LastErrorException(errorMessageFn.apply(t));
		});
	}	
	
}
