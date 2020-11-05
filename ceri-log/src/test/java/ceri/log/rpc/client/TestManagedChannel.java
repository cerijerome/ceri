package ceri.log.rpc.client;

import java.util.concurrent.TimeUnit;
import ceri.common.test.CallSync;
import ceri.common.time.Timeout;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;

public class TestManagedChannel extends ManagedChannel {
	public final CallSync.Get<String> authority = CallSync.supplier("");
	public final CallSync.Accept<Boolean> shutdown = CallSync.consumer(false, true);
	public final CallSync.Apply<Timeout, Boolean> awaitTermination =
		CallSync.function(null, true);

	public static TestManagedChannel of() {
		return new TestManagedChannel();
	}

	private TestManagedChannel() {}

	@Override
	public String authority() {
		return authority.get();
	}

	@Override
	public <T, R> ClientCall<T, R> newCall(MethodDescriptor<T, R> methodDescriptor,
		CallOptions callOptions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		return shutdown.value();
	}

	@Override
	public TestManagedChannel shutdown() {
		return shutdownNow();
	}

	@Override
	public TestManagedChannel shutdownNow() {
		shutdown.accept(true);
		return this;
	}

	@Override
	public boolean isTerminated() {
		return isShutdown();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return awaitTermination.applyWithInterrupt(Timeout.of(timeout, unit));
	}

}