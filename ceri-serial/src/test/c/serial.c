#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>

/*
 * Code to test serial port access.
 *
 * Build:
 * gcc serial.c -o serial; chmod a+x ./serial
 *
 * Run:
 * ./serial
 */

#define STR(x) #x
#define SYM(x) print_symbol(#x,STR(x))
#define SYMI(x) print_symbol_i(#x,x)
#define SIZE(x) print_symbol_i("sizeof("#x")",sizeof(x))
#define VERIFY(x) verify((x), STR(x))

void print_symbol(const char *name, const char *symbol) {
	if (strlen(symbol) == 0) printf("%s = <defined>\n", name);
	else if (strcmp(name, symbol) == 0) printf("%s = <undefined> (probably)\n", name);
	else printf("%s = %s\n", name, symbol);
}

void print_symbol_i(const char *name, int symbol) {
	printf("%s = 0x%x 0%o %d\n", name, symbol, symbol, symbol);
}

int verify(int r, const char *s) {
    printf("%s => %d 0x%x\n", s, r, r);
    if (r < 0) printf("*** ERROR ***\n");
    return r;
}

int open_serial(const char *path, int flags) {
	printf("open(\"%s\", 0x%x)", path, flags);
	int fd = open(path, flags);
	printf(" = %d\n", fd);
	return fd;
}

void set_flags(int fd) {
	printf("fcntl(%d, F_GETFL:%d)", fd, F_GETFL);
	int flags = fcntl(fd, F_GETFL);
	printf(" = 0x%x\n", flags);

	flags &= ~O_NONBLOCK;
	printf("fcntl(%d, F_SETFL:%d, 0x%x)", fd, F_SETFL, flags);
	printf(" = %d\n", fcntl(fd, F_SETFL, flags));
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

#define DC1 0x11
#define DC3 0x13

void set_attrs(int fd) {
	struct termios tty;
	printf("tcgetattr(%d, %p)", fd, &tty);
	printf(" = %d\n", tcgetattr(fd, &tty));
	print_tty(&tty);

	printf("cfmakeraw(%p)\n", &tty);
	cfmakeraw(&tty);

	SYMI(CLOCAL | CREAD);
	SYMI(~(ICANON | ECHO | ECHOE | ISIG));
	SYMI(~OPOST);

	tty.c_cflag |= CLOCAL | CREAD;
	tty.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	tty.c_oflag &= ~OPOST;
	tty.c_cc[VSTART] = DC1;
	tty.c_cc[VSTOP] = DC3;
	tty.c_cc[VMIN] = 0;
	tty.c_cc[VTIME] = 0;

	print_tty(&tty);
}

void close_fd(int fd) {
	printf("close(%d)", fd);
	printf(" = %d\n", close(fd));
}

int main(int argc, char *argv[]) {
	SYM(O_RDWR | O_NOCTTY | O_NONBLOCK);
	int fd = open_serial(argv[1], O_RDWR | O_NOCTTY | O_NONBLOCK);
	set_flags(fd);
	set_attrs(fd);
	close_fd(fd);
	return 0;
}
