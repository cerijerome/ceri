#Qbang RXTX

Sources:
- `http://rxtx.qbang.org/wiki/index.php/Download`  
- `https://github.com/CMU-CREATE-Lab/finch/tree/master/java`  

## Mac install script
Run script:
- `mac-install <ver>`  

Copies the version of `RXTXComm.jar` to local maven repository:
- Coordinates: `org.qbang.rxtx:rxtx:<ver>`

Copies `librxtxSerial.jnilib` to Java extensions dir:  
- `sudo cp <ver>/mac/librxtxSerial.jnilib /Library/Java/Extensions` for all users
- (Use `cp <ver>/mac/librxtxSerial.jnilib ~/Library/Java/Extensions` for current user only)

## Notes
`2.2pre2` contains rxtx `2.2pre1`, and a warning will be printed.  
`2.2pre1` has no warning, but not sure what was fixed in `2.2pre2`.  
`2.2cmu` was built by CMU-CREATE-lab (doesn't seem to work)  
`2.x` is the original from jamierf with only mac lib (doesn't seem to work)  
`2.1-7` is the old version (doesn't seem to work)  