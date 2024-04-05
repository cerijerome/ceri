$$CSYMBOLGEN_PRE$$
/*
 * Example code to print symbols and type sizes.
 *
 * Build:
 * gcc symbols.c -o symbols; chmod a+x ./symbols
 * gcc -I<header-include-path> symbols.c -o symbols; chmod a+x ./symbols
 *
 * Run:
 * ./symbols # default arg 013
 * ./symbols 013 # generate with dec, hex and symbol definitions
 * ./symbols 102 # generate java constants with hex values
 */

#define STR(x) #x
#define SYM(x) print_symbol(#x,STR(x))
#define SYMI(x) print_symbol_i(#x,STR(x),x)
#define SIZE(x) print_symbol_i("sizeof("#x")","",sizeof(x))
#define FSIZE(t,f) print_symbol_i("sizeof("#t"."#f")","",(sizeof(((t*)0)->f)))
#define VERIFY(x) verify_int((x),STR(x))

#define FMT_DEC		0x001
#define FMT_HEX		0x002
#define FMT_OCT		0x004
#define FMT_SYM		0x010
#define FMT_JAVA	0x100
#define FMT_DEF		0x013

int format = FMT_DEF;
char uname[200];

void print_symbol(const char *name, const char *symbol) {
	if (format & FMT_JAVA) printf("public static final int ");
	printf("%s = ", name);
	if (strlen(symbol) == 0) printf("<defined>");
	else if (strcmp(name, symbol) == 0) printf("<undefined?>");
	else printf("%s", symbol);
	printf(";\n");
}

void format_sizeof(const char *name, int isymbol) {
    int offset = 7; // sizeof(
    if (strncmp(name + offset, "struct ", 7) == 0) offset += 7;
    int len = strlen(name + offset) - 1;
    strncpy(uname, name + offset, len);
    for (int i = 0; i < len; i++) {
        uname[i] = toupper(uname[i]);
        if (uname[i] == '.') uname[i] = '_';
    }
    uname[len] = 0;
    printf("%s_SIZE = %d; // %s", uname, isymbol, name);
}

void format_symbol_i(const char *name, const char *symbol, int isymbol) {
	printf("%s", name);
	if (format & FMT_DEC) printf(" = %d", isymbol);
	if (format & FMT_HEX) printf(" = 0x%x", isymbol);
	if (format & FMT_OCT) printf(" = 0%o", isymbol);
	if ((format & FMT_SYM) && strlen(symbol) > 0) printf(" = %s", symbol);
}

void print_symbol_i(const char *name, const char *symbol, int isymbol) {
	if (format & FMT_JAVA) printf("public static final int ");
	if ((format & FMT_JAVA) && strncmp(name, "sizeof(", 7) == 0) format_sizeof(name, isymbol);
	else format_symbol_i(name, symbol, isymbol);
	printf(";\n");
}

int verify_int(int r, const char *s) {
    printf("%s => 0x%x 0%o %d", s, r, r, r);
    if (r < 0) printf(" [ERROR]\n");
    else printf("\n");
    return r;
}

void set_format_flags(int argc, char *argv[]) {
	if (argc < 2) return;
	char* p;
	long value = strtol(argv[1], &p, 16);
	if (*p == '\0') format = (int) value; // ok
	else printf("Bad format '%s', using %03x\n", argv[1], format);
}

int main(int argc, char *argv[]) {
	set_format_flags(argc, argv);
	printf("\n");
$$CSYMBOLGEN_MAIN$$
	return 0;
}
