package ceri.home.teatree;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import ceri.common.event.EventListener;
import ceri.common.property.PropertyUtil;
import ceri.home.audio.Audio;
import ceri.home.audio.SoundLibrary;
import ceri.home.audio.SoundPlayer;
import ceri.home.device.common.IrProperties;
import ceri.home.device.dvd.DvdGrammar;
import ceri.home.device.dvd.DvdProperties;
import ceri.home.device.dvd.DvdState;
import ceri.home.device.dvd.sony.SonyDvd;
import ceri.home.device.receiver.ReceiverGrammar;
import ceri.home.device.receiver.ReceiverIrProperties;
import ceri.home.device.receiver.ReceiverProperties;
import ceri.home.device.receiver.ReceiverState;
import ceri.home.device.receiver.denon.DenonReceiver;
import ceri.home.device.tv.TvGrammar;
import ceri.home.device.tv.TvIrProperties;
import ceri.home.device.tv.TvProperties;
import ceri.home.device.tv.TvState;
import ceri.home.device.tv.sony.SonyTv;
import ceri.home.io.pcirlinc.PcIrLinc;
import ceri.home.io.pcirlinc.PcIrLincProperties;
import ceri.speech.grammar.ActionGrammar;
import ceri.speech.grammar.ContextGrammar;
import ceri.speech.recognition.SpeechParser;
import ceri.speech.recognition.SpeechRecognizer;
import ceri.speech.recognition.SpeechRecognizerProperties;

public class Teatree implements Runnable {
	private static final File ROOT = new File(".");
	private static final String TV_STATE_KEY = "tv.state";
	private static final String RECEIVER_STATE_KEY = "receiver.state";
	private static final String DVD_STATE_KEY = "dvd.state";
	private final PcIrLinc pcIrLinc;
	private final TvProperties tvProperties;
	private final SonyTv tv;
	private final ReceiverProperties receiverProperties;
	private final DenonReceiver receiver;
	private final DvdProperties dvdProperties;
	private final SonyDvd dvd;
	private final SpeechParser speechParser;
	private final Properties state;
	private final File stateFile;
	private final SoundLibrary<SpeechParser.ParseState> soundLibrary;
	private final CountDownLatch countDownLatch = new CountDownLatch(1);

	public Teatree() throws InterruptedException, IOException {
		stateFile = new File(ROOT, "data/state/state.properties");
		state = PropertyUtil.load(stateFile);
		Properties properties = PropertyUtil.load(getClass(), "teatree.properties");
		pcIrLinc = createPcIrLinc(properties);

		tvProperties = new TvProperties(properties, "tv");
		tv = createTv(properties, pcIrLinc, state);
		receiverProperties = new ReceiverProperties(properties, "receiver");
		receiver = createReceiver(properties, pcIrLinc, state);
		dvdProperties = new DvdProperties(properties, "dvd");
		dvd = createDvd(properties, pcIrLinc, state);
		speechParser = createSpeechParser(properties);
		addGrammar();
		soundLibrary = createSoundLibrary();
		speechParser.startListening("headset-ceri");
	}

	public static void main(String[] args) throws Exception {
		Teatree tt = new Teatree();
		Thread thread = new Thread(tt);
		thread.start();
		thread.join();
		System.exit(0);
	}

	@Override
	public void run() {
		try {
			doRun();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void doRun() throws Exception {
		//		SoundPlayer soundPlayer = new SoundPlayer();
		//		SoundLibrary<String> soundLibrary = SoundLibrary.create(soundPlayer);
		//	    List<String> words = Numbers.INSTANCE.getLongAsWords(new Random().nextInt());
		//	    for (String word : words) {
		//	    	File f = new File(ROOT, "data/speech/numbers/" + word + ".wav");
		//	    	Audio audio = Audio.createFromStream(AudioSystem.getAudioInputStream(f));
		//	    	audio = audio.clip(new ClipInfo(8, 40));
		//	    	soundLibrary.add(word, audio);
		//	    }

		try {
			//for (String word : words) soundLibrary.play(word);
			countDownLatch.await();
		} finally {
			try {
				PropertyUtil.store(state, stateFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			speechParser.close();
			pcIrLinc.close();
		}
	}

	private PcIrLinc createPcIrLinc(Properties properties) throws IOException {
		PcIrLincProperties pcIrLincProperties = new PcIrLincProperties(properties, "pcIrLinc");
		return new PcIrLinc(pcIrLincProperties);
	}

	private SonyTv createTv(Properties properties, PcIrLinc pcIrLinc, Properties stateStorage) {
		TvIrProperties tvIrProperties = new TvIrProperties(properties, "tv.ir");
		String stateStr = stateStorage.getProperty(TV_STATE_KEY);
		TvState tvState = stateStr == null ? new TvState() : TvState.createFromString(stateStr);
		return new SonyTv(tvState, pcIrLinc, tvIrProperties, tvProperties);
	}

	private DenonReceiver createReceiver(Properties properties, PcIrLinc pcIrLinc,
		Properties stateStorage) {
		ReceiverIrProperties receiverIrProperties =
			new ReceiverIrProperties(properties, "receiver.ir");
		String stateStr = stateStorage.getProperty(RECEIVER_STATE_KEY);
		ReceiverState receiverState =
			stateStr == null ? new ReceiverState() : ReceiverState.createFromString(stateStr);
		return new DenonReceiver(receiverState, pcIrLinc, receiverIrProperties, receiverProperties);
	}

	private SonyDvd createDvd(Properties properties, PcIrLinc pcIrLinc, Properties stateStorage) {
		IrProperties dvdIrProperties = new IrProperties(properties, "dvd.ir");
		String stateStr = stateStorage.getProperty(DVD_STATE_KEY);
		DvdState dvdState = stateStr == null ? new DvdState() : DvdState.createFromString(stateStr);
		return new SonyDvd(dvdState, pcIrLinc, dvdIrProperties, dvdProperties);
	}

	private void addGrammar() {
		ActionGrammar tvContextGrammar =
			TvGrammar.createContextual(tvProperties.channelMap().values(), tvProperties.inputs(),
				tv);
		ActionGrammar receiverContextGrammar =
			ReceiverGrammar.createContextual(receiverProperties.inputs(), receiver);
		ActionGrammar dvdContextGrammar = DvdGrammar.createContextual(dvd);
		ContextGrammar contextGrammar =
			new ContextGrammar(speechParser, tvContextGrammar, receiverContextGrammar,
				dvdContextGrammar);

		ActionGrammar tvGrammar =
			TvGrammar.create(tvProperties.channelMap().values(), tvProperties.inputs(), tv,
				contextGrammar);
		ActionGrammar receiverGrammar =
			ReceiverGrammar.create(receiverProperties.inputs(), receiver, contextGrammar);
		ActionGrammar dvdGrammar = DvdGrammar.create(dvd, contextGrammar);

		MacroGrammar macroGrammar =
			new MacroGrammar.Builder(contextGrammar).tv(tv, tvProperties).receiver(receiver,
				receiverProperties).dvd(dvd).build();

		speechParser.addGrammar(macroGrammar);
		speechParser.addGrammar(tvGrammar);
		speechParser.addGrammar(receiverGrammar);
		speechParser.addGrammar(dvdGrammar);
		speechParser.addGrammar(new ControlGrammar(countDownLatch, state, stateFile));
	}

	private SpeechParser createSpeechParser(Properties properties) throws InterruptedException {
		SpeechRecognizerProperties recognizerProperties =
			new SpeechRecognizerProperties(properties, "speech.recognizer");
		SpeechRecognizer recognizer = new SpeechRecognizer(recognizerProperties);
		EventListener<SpeechParser.ParseState> listener =
			new EventListener<SpeechParser.ParseState>() {
				@Override
				public void event(SpeechParser.ParseState parseState) {
					playSound(parseState);
				}
			};
		SpeechParser speechParser = new SpeechParser(recognizer, null, listener);
		return speechParser;
	}

	@SuppressWarnings("resource")
	private SoundLibrary<SpeechParser.ParseState> createSoundLibrary() throws IOException {
		SoundLibrary<SpeechParser.ParseState> soundLibrary = SoundLibrary.create(new SoundPlayer());
		for (SpeechParser.ParseState parseState : SpeechParser.ParseState.values()) {
			try {
				File file =
					new File(ROOT, "data/sounds/parser/" + parseState.name().toLowerCase() + ".wav");
				Audio audio = Audio.createFromStream(AudioSystem.getAudioInputStream(file));
				soundLibrary.add(parseState, audio);
			} catch (UnsupportedAudioFileException e) {
				throw new IOException(e);
			}
		}
		return soundLibrary;
	}

	void playSound(SpeechParser.ParseState parseState) {
		try {
			soundLibrary.play(parseState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//	private void setState(Properties stateStorage) {
	//		stateStorage.setProperty(TV_STATE_KEY, tv.state.toString());
	//		stateStorage.setProperty(RECEIVER_STATE_KEY, receiver.state.toString());
	//		stateStorage.setProperty(DVD_STATE_KEY, dvd.state.toString());
	//	}

}
