#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <poll.h>
#include <termios.h>
#include <signal.h>
#include <sys/ioctl.h>
#include <IOKit/serial/ioss.h>

/*
 * Example code to print symbols and type sizes.
 *
 * Build:
 * gcc symbols.c -o symbols; chmod a+x ./symbols
 * gcc -I<header-include-path> symbols.c -o symbols; chmod a+x ./symbols
 *
 * Run:
 * ./symbols
 * ./symbols 1 # generate java with hex ints
 * ./symbols 2 # generate java with decimal ints
 */

#define STR(x) #x
#define SYM(x) print_symbol(#x,STR(x))
#define SYMI(x) print_symbol_i(#x,STR(x),x)
#define SIZE(x) print_symbol_i("sizeof("#x")","",sizeof(x))
#define VERIFY(x) verify_int((x),STR(x))

#define FMT_PRINT 0
#define FMT_JAVA_HEX 1
#define FMT_JAVA_DEC 2

int format = FMT_PRINT;

void print_symbol(const char *name, const char *symbol) {
	if (strlen(symbol) == 0) printf("%s = <defined>\n", name);
	else if (strcmp(name, symbol) == 0) printf("%s = <undefined> (probably)\n", name);
	else if (format > 0) printf("public static final String %s = %s;\n", name, symbol);
	else printf("%s = %s\n", name, symbol);
}

void print_symbol_i(const char *name, const char *symbol, int isymbol) {
	if (format == FMT_PRINT) {
		printf("%s = 0x%x 0%o %d", name, isymbol, isymbol, isymbol);
		if (strlen(symbol) > 0)	printf(" = %s", symbol);
		printf("\n");
	}
	if (format == FMT_JAVA_HEX) printf("public static final int %s = 0x%x;\n", name, isymbol);
	if (format == FMT_JAVA_DEC) printf("public static final int %s = %d;\n", name, isymbol);
}

int verify_int(int r, const char *s) {
    printf("%s => 0x%x 0%o %d", s, r, r, r);
    if (r < 0) printf(" [ERROR]\n");
    else printf("\n");
    return r;
}

#define EMPTY
#define STRING "string"

int main(int argc, char *argv[]) {
	format = argc < 2 ? FMT_PRINT : *argv[1] - '0';

	//SYM(UNDEFINED);
	//SYM(EMPTY);
	//SYM(STRING);
	//SYM(_POSIX_C_SOURCE);
	//SYM(_DARWIN_C_SOURCE);
	//SIZE(long);
	//SIZE(void*);
	SIZE(mode_t);
	SIZE(nfds_t);

	//SYMI(TIOCINQ);
	SYMI(TIOCOUTQ);
 
	return 0;
}

