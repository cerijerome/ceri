#include "../config.h"
#include <stdio.h>
#include "pcirlinc.h"
#include "port.h"
#include "util.h"

#ifndef DEFAULT_DEVICE
# ifdef linux
#  define DEFAULT_DEVICE "/dev/ttyS0"
# else
#  define DEFAULT_DEVICE "/dev/cuaa0"
# endif
#endif

char response[255];

int irlinc_open (char *device)
{
	int port;

	char *dev= DEFAULT_DEVICE;
	if ( device != NULL ) dev= device;

	if ( (port= port_open(dev)) == -1 ) return -1;

	if ( port_init(port) == -1 ) {
		port_close(port);
		return -1;
	}

	return port;
}

int irlinc_close (int port)
{
	return port_close(port);
}

int irlinc_send_preset (int port, int type, int vendor, int button,
	unsigned int n)
{
	unsigned int i;
	char cmd[6];

	cmd[0]= 5;
	cmd[1]= 1;
	cmd[2]= (type<<4)|(vendor>>8);
	cmd[3]= vendor&0xff;
	cmd[4]= button&0xff;
	cmd[5]= 0;

	if ( n == 0 ) return 0;

	if ( port_rts_on(port) == -1 ) return -1;
	for (i= 0; i< n; i++) {
		irlinc_msleep(20);
		if ( port_send(port, cmd, 6) == -1 ) return -1;
	}
	if ( port_rts_off(port) == -1 ) return -1;
	irlinc_msleep(20);

	if ( port_recv(port, response, 500) == -1 ) return -1;

	return (int) response[0];
}

int irlinc_learn_ir (int port, char *buf, size_t *length)
{
	static char cmd[4]= { 0x03, 0x12, 0x00, 0x00 };

	*length= 0;

	if ( port_rts_on(port) == -1 ) return -1;
	irlinc_msleep(20);
	if ( port_send(port, cmd, 4) == -1 ) return -1;
	if ( port_rts_off(port) == -1 ) return -1;
	irlinc_msleep(20);

	if ( port_recv(port, response, 6000) == -1 ) return -1;

	memcpy(buf, &response[4], response[3]);

	*length= (size_t) response[3];

	return 0;
}

int irlinc_send_learned_ir (int port, char *code, size_t length, unsigned
	int n)
{
	char *cmd;
	int i;

	if ( n == 0 ) return 0;

	if ( (cmd= (char *) malloc(length+9)) == NULL ) return -1;
	cmd[0]= length+8;
	cmd[1]= 0x10;
	cmd[2]= 0;
	cmd[3]= 0;
	cmd[4]= 0x10;
	cmd[5]= 0;
	cmd[6]= 0;
	cmd[7]= length;
	memcpy(&cmd[8], code, length);
	cmd[length+9]= 0;

	if ( port_rts_on(port) == -1 ) {
		free(cmd);
		return -1;
	}
	for (i= 0; i< n; i++) {
		irlinc_msleep(20);
		if ( port_send(port, cmd, length+9) == -1 ) {
			free(cmd);
			return -1;
		}
	}

	free(cmd);

	if ( port_rts_off(port) == -1 ) return -1;
	irlinc_msleep(20);

	if ( port_recv(port, response, 500) == -1 ) return -1;

	return (int) response[0];
}

