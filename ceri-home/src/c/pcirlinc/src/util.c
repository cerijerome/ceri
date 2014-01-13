#include <sys/time.h>
#include <stdio.h>
#include "util.h"

void irlinc_msleep (long ms)
{
	struct timeval timeout;

	mstotv(ms, &timeout);

	select(0, NULL, NULL, NULL, &timeout);

	return;
}

void mstotv (long ms, struct timeval *tv)
{
	if ( ! ms ) return;

	if ( ms >= 1000 ) {
		tv->tv_sec= ms/1000;
		tv->tv_usec= (ms - tv->tv_sec*1000)*1000;
	} else {
		tv->tv_sec= 0;
		tv->tv_usec= ms*1000;
	}
}

/* Call read(2) until we get nbytes, a timeout, or an error. */

ssize_t Read_timeout (int fd, void *buf, size_t nbytes, unsigned long ms)
{
	struct timeval timeout;
	ssize_t bread, bremain;
	char *bp= (char *) buf;
	fd_set rset;

	if ( ms ) {
		mstotv(ms, &timeout);
		FD_ZERO(&rset);
	}

	bremain= nbytes;
	while ( bremain ) {
		if ( ms ) {
			FD_SET(fd, &rset);

			if ( (select(fd+1, &rset, NULL, NULL, &timeout))
				== -1 ) return -1;

			if ( ! FD_ISSET(fd, &rset) ) return -1;
		}
		if ( (bread= read(fd, bp, nbytes)) == -1 ) return -1;
		bremain-= bread;
		bp+= bread;
	}

	return nbytes;
}

