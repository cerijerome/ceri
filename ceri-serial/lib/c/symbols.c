#include <stdio.h>
#include <fcntl.h>

#define SYMBOL(x) print_symbol(#x,x)

#define FMT_PRINT 0
#define FMT_JAVA_HEX 1
#define FMT_JAVA_DEC 2

int format = FMT_PRINT;

void print_symbol(char *name, int symbol) {
	if (format == FMT_PRINT) printf("%-12s = 0x%x %d\n", name, symbol, symbol);
	if (format == FMT_JAVA_HEX) printf("public static final int %s = 0x%x;\n", name, symbol);
	if (format == FMT_JAVA_DEC) printf("public static final int %s = %d;\n", name, symbol);
}

int main(int argc, char *argv[]) {
	format = argc < 2 ? FMT_PRINT : *argv[1] - '0';

#if defined(__MACRO__)
	printf("__MACRO__ defined");
#endif
	
	SYMBOL(SEEK_END);
	SYMBOL(O_CREAT);
	
	return 0;
}

// gcc symbols.c -o symbols; chmod a+x ./symbols; ./symbols 1

