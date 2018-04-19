# RXTX Project

Provides a bridge for RXTX to make it compatible with Java Comms API.  
Not fully supported so far, just enough for x10, dmx, audio, and video projects to work.  

For MacOSX:
- Project contains the RXTX comm library (gnu.io.*) and PL2303 USB to serial drivers.
- Follow instructions under lib/macosx/pl2303 to install the serial usb driver
- Follow instructions under lib/qbang to install gnu rxtx jar and jni library
- Comm port name should be "/dev/tty.usbserial" or similar
- Can ignore the warning "RXTX Version mismatch"

For Ubuntu: (needs update)
- sudo apt-get install librxtx-java
- Copy RXTXcomm.jar to jre/lib/ext, e.g.:
  sudo cp /usr/share/java/RXTXcomm.jar /usr/lib/jvm/java-8-oracle/jre/lib/ext/
- Copy librxtxSerial.so to jre/lib/amd64 and make executable, e.g.:
  sudo cp /usr/lib/jni/librxtxSerial.so /usr/lib/jvm/java-8-oracle/jre/lib/amd64/
  sudo chmod a+x /usr/lib/jvm/java-8-oracle/jre/lib/amd64/librxtxSerial.so
- Change permissions on USB ports, e.g.:
  sudo chmod a+rw /dev/ttyUSB*
  sudo chmod a+rw /dev/usb