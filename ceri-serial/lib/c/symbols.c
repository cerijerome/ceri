#include <stdio.h>
#include <string.h>
#include <fcntl.h>

#define STR(x) #x

#define SYM(x) print_symbol(#x,STR(x))
#define SYMI(x) print_symbol_i(#x,x)

#define FMT_PRINT 0
#define FMT_JAVA_HEX 1
#define FMT_JAVA_DEC 2

int format = FMT_PRINT;

void print_symbol_i(const char *name, int symbol) {
	if (format == FMT_PRINT) printf("%-12s = 0x%x %d\n", name, symbol, symbol);
	if (format == FMT_JAVA_HEX) printf("public static final int %s = 0x%x;\n", name, symbol);
	if (format == FMT_JAVA_DEC) printf("public static final int %s = %d;\n", name, symbol);
}

void print_symbol(const char *name, const char *symbol) {
	if (strlen(symbol) == 0) printf("%s = <defined>\n", name);
	else if (strcmp(name, symbol) == 0) printf("%s = <undefined> (probably)\n", name);
	else if (format > 0) printf("public static final String %s = %s;\n", name, symbol);
	else printf("%s = %s\n", name, symbol);
}

#define HELLO2
#define HELLO3 12345678
#define HELLO4 "hello"

int main(int argc, char *argv[]) {
	format = argc < 2 ? FMT_PRINT : *argv[1] - '0';
	
	SYM(HELLO1);
	SYM(HELLO2);
	SYM(HELLO3);
	SYM(HELLO4);
	SYM(O_CREAT);
	
	SYMI(HELLO3);
	SYMI(O_CREAT);

	return 0;
}

// gcc symbols.c -o symbols; chmod a+x ./symbols; ./symbols 1

