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

		public <T> T wrap(E exception) {
			throw new WrapperException(this, exception);
		}

		public <T> T handle(WrapperException exception) throws E {
			if (this != exception.agent)
				throw new IllegalStateException("Mis-matched agent: " + exception.agent);
			throw BasicUtil.<E>uncheckedCast(exception.getCause());
		}

	}

}
