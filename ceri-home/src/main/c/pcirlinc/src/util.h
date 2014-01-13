#ifndef __UTIL__H
#define __UTIL__H

#include <sys/types.h>
#include <sys/time.h>
#include "pcirlinc.h"

void mstotv (long ms, struct timeval *tv);
ssize_t Read_timeout (int fd, void *buf, size_t nbytes, unsigned 
	long timeo);

#endif
