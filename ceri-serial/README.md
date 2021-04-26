# Serial Projects

## Java Serial

Provides a bridge for serial libs to Java Comms API.  
Not fully supported so far, just enough for x10, dmx, audio, and video projects to work.  

`purejavacomm` is now used instead of rxtx. This uses `jna` to call OS libraries directly,
no need to install a java extension lib.
 
For MacOSX:
- OSX now comes with USB to serial FTDI driver for PL2303
- Otherwise follow instructions under `lib/pl2303/mac` to install the serial usb driver
- Comm port name should be `/dev/tty.usbserial...` or similar

For Ubuntu:
- (Need to install FTDI PL2303 driver?)
- Need to change permissions on USB ports regularly, e.g.:  
  `sudo chmod a+rw /dev/ttyUSB*`  
  `sudo chmod a+rw /dev/usb`

## LibUsb
This is a JNA implementation of `libusb-1.0.x`, a user-level USB driver available for most platforms.
Not all features are tested, so beware!

## LibFtdi
This is an implementation of `libftdi`, used to communicate with USB devices using FTDI chips. It is built on top of `LibUsb`.

---
## RXTX (old)
  
For Ubuntu: (old)
- `sudo apt-get install librxtx-java`
- Copy `RXTXcomm.jar` to `jre/lib/ext`, e.g.:  
  `sudo cp /usr/share/java/RXTXcomm.jar /usr/lib/jvm/java-8-oracle/jre/lib/ext/`
- Copy `librxtxSerial.so` to `jre/lib/amd64` and make executable, e.g.:  
  `sudo cp /usr/lib/jni/librxtxSerial.so /usr/lib/jvm/java-8-oracle/jre/lib/amd64/`  
  `sudo chmod a+x /usr/lib/jvm/java-8-oracle/jre/lib/amd64/librxtxSerial.so`
- Change permissions on USB ports, e.g.:  
  `sudo chmod a+rw /dev/ttyUSB*`  
  `sudo chmod a+rw /dev/usb`

For MacOSX (old):
- Follow instructions under `lib/qbang` to install gnu rxtx jar and jni library
  - Update `ceri-pom/pom.xml` to reference the rxtx version
- Comm port name should be `/dev/tty.usbserial` or similar
- Can ignore the warning "RXTX Version mismatch"

Issues:

- Mac OSX releasing FTDI serial port  
  https://superuser.com/questions/1135730/how-to-release-reset-serial-port-ftdi-devices-mac-osx
<pre>
java.io.IOException: Device not configured in nativeavailable
    at gnu.io.RXTXPort.nativeavailable(Native Method) ~[nrjavaserial-3.13.0.jar:3.13.0]
    at gnu.io.RXTXPort$SerialInputStream.available(RXTXPort.java:1568) ~[nrjavaserial-3.13.0.jar:3.13.0]
    at ceri.common.io.ReplaceableInputStream.available(ReplaceableInputStream.java:79) ~[classes/:?]
    at java.io.BufferedInputStream.available(BufferedInputStream.java:416) ~[?:?]
    at ceri.common.io.PollingInputStream.waitForData(PollingInputStream.java:63) ~[classes/:?]
    at ceri.common.io.PollingInputStream.read(PollingInputStream.java:40) ~[classes/:?]
    at java.io.DataInputStream.readByte(DataInputStream.java:270) ~[?:?]
    at ceri.dmx.enttec.reply.ReplyProcessor.readSom(ReplyProcessor.java:80) ~[classes/:?]
    at ceri.dmx.enttec.reply.ReplyProcessor.readReply(ReplyProcessor.java:71) ~[classes/:?]
    at ceri.dmx.enttec.reply.ReplyProcessor.loop(ReplyProcessor.java:57) [classes/:?]
    at ceri.log.concurrent.LoopingExecutor.loops(LoopingExecutor.java:81) [classes/:?]
    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1135) [?:?]
    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635) [?:?]
    at java.lang.Thread.run(Thread.java:844) [?:?]
    
  </pre>