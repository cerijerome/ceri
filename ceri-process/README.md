#Process - running executables from java

##MacOSX
When running via eclipse, `PATH` may not include `/usr/local/bin`.  
One solution is to run 
`sudo launchctl config user path <new path>`, such as:  
`sudo launchctl config user path /usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin` 
