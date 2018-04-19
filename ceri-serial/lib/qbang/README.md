#Qbang RXTX

Source: `http://rxtx.qbang.org/wiki/index.php/Download`  

## Install RXTXComm.jar
Copy the versions of `RXTXComm.jar` to local maven repository:
- Run `mvn-install-rxtx`
- Coordinates: `org.qbang.rxtx:rxtx:2.2pre2`

## Install native lib - Mac
Copy `librxtxSerial.jnilib` to Java extensions dir:  
- `sudo cp <ver>/mac/librxtxSerial.jnilib /Library/Java/Extensions` for all users
- `cp <ver>/mac/librxtxSerial.jnilib ~/Library/Java/Extensions` for current user
- Script `mac-install-jni`

## Notes
`2.2pre2` contains rxtx `2.2pre1`, and a warning will be printed.  
`2.2pre1` has no warning, but not sure what was fixed in `2.2pre2`.  
