#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <IOKit/serial/ioss.h>

/*
 * Code to test Mac serial port access.
 *
 * Build:
 * gcc serial-mac.c -o serial-mac; chmod a+x ./serial-mac
 *
 * Run:
 * ./serial
 */

#define STR(x) #x
#define SYM(x) print_symbol(#x,STR(x))
#define SYMI(x) print_symbol_i(#x,x)
#define SIZE(x) print_symbol_i("sizeof("#x")",sizeof(x))
#define VERIFY(x) verify(STR(x),(x))
#define EXEC(x) println(STR(x));x

const char *print_symbol(const char *name, const char *symbol) {
	if (strlen(symbol) == 0) printf("%s = <defined>\n", name);
	else if (strcmp(name, symbol) == 0) printf("%s = <undefined> (probably)\n", name);
	else printf("%s = %s\n", name, symbol);
	return symbol;
}

int print_symbol_i(const char *name, int symbol) {
	if (symbol == 0) printf("%s = 0\n", name);
	else printf("%s = 0x%x %d\n", name, symbol, symbol);
	return symbol;
}

int verify(const char *s, int r) {
	if (r >= 0) print_symbol_i(s, r);
	else printf("%s ==> %d [ERROR]\n", s, r);
	return r;
}

void println(const char *s) {
	printf("%s\n", s);
}

void print_tty(struct termios *ptty) {
	printf("termios@%p {\n", ptty);
	printf("\tc_iflag = 0x%lx %ld\n", ptty->c_iflag, ptty->c_iflag);
	printf("\tc_oflag = 0x%lx %ld\n", ptty->c_oflag, ptty->c_oflag);
	printf("\tc_cflag = 0x%lx %ld\n", ptty->c_cflag, ptty->c_cflag);
	printf("\tc_lflag = 0x%lx %ld\n", ptty->c_lflag, ptty->c_lflag);
	printf("\tc_cc = [ ");
	for (int i = 0; i < NCCS; i++)
		printf("0x%02x ", ptty->c_cc[i]);
	printf("]\n");
	printf("\tc_ispeed = 0x%lx %ld\n", ptty->c_ispeed, ptty->c_ispeed);
	printf("\tc_ospeed = 0x%lx %ld\n", ptty->c_ospeed, ptty->c_ospeed);
	printf("}\n");
}

void clear_nonblock(int fd) {
	int flags = VERIFY(fcntl(fd, F_GETFL));
	flags &= ~O_NONBLOCK;
	VERIFY(fcntl(fd, F_SETFL, flags));
}

#define DC1 0x11
#define DC3 0x13
#define CMSPAR 0

void init_termios(struct termios *ptty) {
	ptty->c_iflag &= ~(IXANY | IXOFF | IXON);
	ptty->c_cflag &= ~(CSIZE | CSTOPB | PARENB | CMSPAR | PARODD | CRTSCTS);
	ptty->c_cflag |= CLOCAL | CREAD | CS8;
	ptty->c_cc[VSTART] = DC1;
	ptty->c_cc[VSTOP] = DC3;
	ptty->c_cc[VMIN] = 0;
	ptty->c_cc[VTIME] = 0;
}

void configure_baud_code(struct termios *ptty, int baudcode) {
	VERIFY(cfsetispeed(ptty, baudcode));
	VERIFY(cfsetospeed(ptty, baudcode));
}

void init_port(int fd) {
	struct termios tty;
	VERIFY(tcgetattr(fd, &tty));
	cfmakeraw(&tty);
	init_termios(&tty);
	configure_baud_code(&tty, B9600);
	print_tty(&tty);
	VERIFY(tcsetattr(fd, TCSANOW, &tty));
}

void set_params(int fd, int baudcode, speed_t speed, int databits, int stopbits, int parity) {
	struct termios tty;
	VERIFY(tcgetattr(fd, &tty));
	tty.c_cflag &= ~(CSIZE | CSTOPB | PARENB | CMSPAR | PARODD);
	tty.c_cflag |= databits | stopbits | parity;
	configure_baud_code(&tty, baudcode);
	print_tty(&tty);
	VERIFY(tcsetattr(fd, TCSANOW, &tty));
	if (speed == 0) return;
	SIZE(speed_t);
	SYMI(IOSSIOSPEED);
	VERIFY(ioctl(fd, IOSSIOSPEED, &speed));
}

int main(int argc, char *argv[]) {
	int fd = VERIFY(open(argv[1], O_RDWR | O_NOCTTY | O_NONBLOCK));
	clear_nonblock(fd);
	init_port(fd);

	set_params(fd, B9600, 250000, CS8, 0, 0);

	EXEC(close(fd));
	return 0;
}
