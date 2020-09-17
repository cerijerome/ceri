package ceri.x10.cm11a.protocol;

import java.time.LocalDateTime;
import org.junit.Test;
import ceri.common.data.ByteArray.Encoder;
import ceri.common.data.ByteProvider;

public class DataBehavior {

	@Test
	public void shouldWriteDateTime() {
		ByteProvider bytes =
			Encoder.of().apply(enc -> Data.writeDateTo(LocalDateTime.now(), enc)).immutable();
		var dt = Data.readDateFrom(bytes.reader(0));
		// BinaryPrinter.DEFAULT.print(bytes);
		// System.out.println(Data.readDateFrom(bytes.reader(0)));
	}

}
