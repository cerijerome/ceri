package ceri.serial.libusb.jna;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static ceri.serial.libusb.jna.LibUsbAudio.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.util.function.Function;
import org.junit.Test;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.test.CallSync;
import ceri.serial.jna.Struct;

public class LibUsbAudioTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(LibUsbAudio.class);
	}

	@Test
	public void testEnumTypes() {
		exerciseEnum(audio_interface_subclass_code.class);
		exerciseEnum(audio_descriptor_type.class);
		exerciseEnum(audio_control_interface_desc_subtype.class);
		exerciseEnum(audio_streaming_interface_desc_subtype.class);
		exerciseEnum(audio_proc_unit_process_type.class);
		exerciseEnum(audio_endpoint_desc_subtype.class);
		exerciseEnum(audio_request_code.class);
		exerciseEnum(audio_term_control_selector.class);
		exerciseEnum(audio_feat_unit_control_selector.class);
		exerciseEnum(audio_updownmix_proc_unit_control_selector.class);
		exerciseEnum(audio_dolbypl_proc_unit_control_selector.class);
		exerciseEnum(audio_stereo3d_proc_unit_control_selector.class);
		exerciseEnum(audio_reverb_proc_unit_control_selector.class);
		exerciseEnum(audio_chorus_proc_unit_control_selector.class);
		exerciseEnum(audio_drcomp_proc_unit_control_selector.class);
		exerciseEnum(audio_ext_unit_control_selector.class);
		exerciseEnum(audio_endpoint_control_selector.class);
		// exerciseEnum(audio_originator.class);
		exerciseEnum(audio_spatial_location.class);
	}

	@Test
	public void testInterruptData() {
		var t = new audio_interrupt_data(null);
		assertEquals(t.interruptPending(), false);
		assertEquals(t.memoryContentsChanged(), false);
		assertEquals(t.originatorType(), audio_originator.AUDIOCONTROL_INTERFACE);
		t.bStatusType = (byte) 0xc2;
		assertEquals(t.interruptPending(), true);
		assertEquals(t.memoryContentsChanged(), true);
		assertEquals(t.originatorType(), audio_originator.AUDIOSTREAMING_ENDPOINT);
		writeRead(t, audio_interrupt_data::new);
	}

	@Test
	public void testControlHeaderDescriptor() {
		var t = new audio_control_header_descriptor(null);
		t.baInterfaceNr = bytes(1, 2, 3);
		t.bInCollection = (byte) t.baInterfaceNr.length;
		Struct.write(t);
		t = writeRead(t, audio_control_header_descriptor::new);
		assertEquals(t.bInCollection, (byte) 3);
		assertArray(t.baInterfaceNr, 1, 2, 3);
	}

	@Test
	public void testMixerUnitDescriptor() {
		var t = new audio_mixer_unit_descriptor(null);
		t.baSourceID = bytes(1, 2, 3);
		t.bNrInPins = (byte) t.baSourceID.length;
		t.bmControls = bytes(4, 5, 6);
		t.bLength = (byte) (audio_mixer_unit_descriptor.BASE_LENGTH + t.baSourceID.length +
			t.bmControls.length);
		assertEquals(t.bLength, (byte) 16);
		assertEquals(t.p(), 3);
		assertEquals(t.n(), 3);
	}

	@Test
	public void testSimpleTypes() {
		writeRead(audio_input_term_descriptor::new);
		writeRead(audio_output_term_descriptor::new);
	}

	private static <T extends Struct> T writeRead(Function<Pointer, T> constructor) {
		return writeRead(constructor.apply(null), constructor);
	}

	private static <T extends Struct> T writeRead(T t, Function<Pointer, T> constructor) {
		return Struct.read(constructor.apply(Struct.write(t).getPointer()));
	}
}
