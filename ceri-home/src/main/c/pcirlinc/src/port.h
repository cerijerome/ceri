#ifndef __PORT__H
#define __PORT__H

#include <sys/types.h>

int port_open (char *device);
int port_close (int port);
int port_init (int port);
int port_send (int port, char *data, size_t len);
int port_recv (int port, char *buf, unsigned int timeout);
int port_rts_on (int port);
int port_rts_off (int port);

#endif
