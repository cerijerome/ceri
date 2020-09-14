package ceri.x10.cm11a.protocol;

import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.Test;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.data.ByteProvider;
import ceri.common.test.BinaryPrinter;
import ceri.x10.cm11a.protocol.Data;

public class DataBehavior {

	@Test
	public void shouldWriteDateTime() {
		ByteProvider bytes = Encoder.of().apply(enc -> Data.writeDateTo(LocalDateTime.now(), enc)).immutable();
		BinaryPrinter.DEFAULT.print(bytes);
		System.out.println(Data.readDateFrom(bytes.reader(0)));
	}
	
}
