package ceri.common.function;

import ceri.common.util.BasicUtil;

public class WrapperException extends RuntimeException {
	private static final long serialVersionUID = -7884771697875904804L;
	final Agent<?> agent;

	WrapperException(Agent<?> agent, Exception e) {
		super(e);
		this.agent = agent;
	}

	public static <E extends Exception> Agent<E> agent() {
		return new Agent<>();
	}
	
	public static class Agent<E extends Exception> {

		Agent() {}

		public <T, R> R wrap(ExceptionFunction<E, T, R> function, T t) {
			try {
				return function.apply(t);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new WrapperException(this, BasicUtil.uncheckedCast(e));
			}
		}
		
		public <T, U, R> R wrap(ExceptionBiFunction<E, T, U, R> function, T t, U u) {
			try {
				return function.apply(t, u);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new WrapperException(this, BasicUtil.uncheckedCast(e));
			}
		}
		
		public <T, R> R handle(ExceptionFunction<E, T, R> function, T t) throws E {
			try {
				return function.apply(t);
			} catch (WrapperException e) {
				if (this != e.agent)
					throw new IllegalStateException("Mis-matched agent: " + e.agent);
				throw BasicUtil.<E>uncheckedCast(e.getCause());
			}
		}
		
		public <T, U, R> R handle(ExceptionBiFunction<E, T, U, R> function, T t, U u) throws E {
			try {
				return function.apply(t, u);
			} catch (WrapperException e) {
				if (this != e.agent)
					throw new IllegalStateException("Mis-matched agent: " + e.agent);
				throw BasicUtil.<E>uncheckedCast(e.getCause());
			}
		}
	}

}
