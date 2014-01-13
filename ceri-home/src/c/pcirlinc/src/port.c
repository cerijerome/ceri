#include <sys/types.h>
#include <sys/time.h>
#include <termios.h>
#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include "port.h"
#ifdef linux
#include <sys/ioctl.h>
#endif

struct termios origstate;
int reset_terminal= 0;
extern int irlinc_errno;

int port_open (char *device)
{
	int fd;

	if ( (fd= open(device, O_RDWR|O_NOCTTY|O_NDELAY)) == -1 ) {
		
		return -1;
	}

	fcntl(fd, F_SETFL, 0);

	return fd;
}

int port_init (int port)
{
	struct termios options;

	if ( tcgetattr(port, &options) == -1 ) return -1;
	memcpy(&options, &origstate, sizeof(options));

	cfsetispeed(&options, B19200);
	cfsetospeed(&options, B19200);

	options.c_cflag |= (CLOCAL|CREAD);
	options.c_cflag &= ~PARENB;		/* No parity */
	options.c_cflag &= ~CSTOPB;		/* 1 stop bit */
	options.c_cflag &= ~CSIZE;
	options.c_cflag |= CS8;			/* 8 data bits */
	options.c_cflag &= ~CRTSCTS;

	options.c_lflag &= ~(ICANON | ECHO | ECHOE | IEXTEN | ISIG);
	options.c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON | IXOFF);

	options.c_oflag &= ~OPOST;

	if ( tcsetattr(port, TCSANOW, &options) == -1 ) return -1;
	reset_terminal= 1;

	return 0;
}

int port_close (int port)
{
	int status= 0;

	if ( reset_terminal )
		if ( tcsetattr(port, TCSANOW, &origstate) == -1 ) status= -1;
	close(port);

	return status;
}

/*
 * POSIX.1 does not specify hardware flow control, and the 1623PC
 * doesn't use RTS/CTS in the standard manner, anyway.  So we need a 
 * hack to set and clear RTS manually.
 */

int port_rts_on (int port)
{
	int status;

	if ( ioctl(port, TIOCMGET, &status) == -1 ) return -1;

	status |= TIOCM_RTS;

	if ( ioctl(port, TIOCMSET, &status) == -1 ) return -1;

	return 0;
}

int port_rts_off (int port)
{
	int status;

	if ( ioctl(port, TIOCMGET, &status) == -1 ) return -1;

	status &= ~TIOCM_RTS;

	if ( ioctl(port, TIOCMSET, &status) == -1 ) return -1;

	return 0;
}

int port_send (int port, char *data, size_t len)
{
	size_t bytes;
	char *ch= data;
	int i;

	tcflush(port, TCIOFLUSH);

	if ( (bytes= write(port, data, len)) == -1 ) return -1;

	return (int) bytes;
}

int port_recv (int port, char *buf, unsigned int timeout)
{
	struct timeval tv;
	fd_set rset;
	char bytes;
	int i;

	mstotv(timeout, &tv);

	FD_ZERO(&rset);
	FD_SET(port, &rset);

	if ( select(port+1, &rset, NULL, NULL, &tv) == -1 ) return -1;

	if ( ! FD_ISSET(port, &rset) ) return -1;

	if ( read(port, buf, 1) == -1 ) return -1;
	bytes= (size_t) buf[0];

	if ( (bytes= (char) Read_timeout(port, buf, bytes, 500)) == -1 )
		return -1;

	return (int) bytes;
}

