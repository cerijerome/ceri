#VeraLite Notes
http://getvera.com/controllers/veralite/

![VeraLite](http://support.mios.com/customer/portal/attachments/72529)

Developer docs:
- http://docs5.mios.com/doc.php?language=1&manual=1&platform=3Lite&page=developers
- http://wiki.micasaverde.com/index.php/Luup_Intro
- http://wiki.micasaverde.com/index.php/Category:Development


##Commands
http://`local-ip`:`port`/data_request?id=`...`

e.g. http://`local-ip`:`port`/data_request?id=user_data

Remote access:

https://fwd2.mios.com/ `user`/`password`/`vera-id`/data_request?id=`...`


##Login
http://wiki.micasaverde.com/index.php/Logon_Vera_SSH

Remote access (24h):
- Tech Support > enable
- Second part of number is password
- ssh remote@`IP`


##Logs
http://wiki.micasaverde.com/index.php/Luup_Debugging
```
cd /var/log/cmh
tail -f LuaUPnP.log
```

##All On/Off
*(Currently unable to make this work)*

Capability 39 = All on/off capable
To enable:
- Device options
- 1 byte dec = 255 (1=off, 2=on, 255=on/off)

Send:
- id=action
- action=SendData
- serviceId=urn:micasaverde-com:serviceId:ZWaveNetwork1
- DeviceNum=1
- Data=0-0x19-0xff-2-0x27-4 (on)
- Data=0-0x19-0xff-2-0x27-5 (off)

/data_request?id=action&action=SendData&serviceId=urn:micasaverde-com:serviceId:ZWaveNetwork1&DeviceNum=1&Data=0-0x19-0xff-2-0x27-4
