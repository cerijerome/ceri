$$CSYMBOLGEN_PRE$$
/*
 * Example code to print symbols and type sizes.
 *
 * Build:
 * gcc <filename>.c -o <filename>; chmod a+x ./<filename>
 * gcc -I<header-include-path> <filename>.c -o <filename>; chmod a+x ./<filename>
 *
 * Run:
 * ./<filename>
 */

#define CERI_STR(x) #x
#define CERI_SYM(x) ceri_print_symbol(#x,CERI_STR(x))
#define CERI_SYMI(x) ceri_print_symbol_i(#x,CERI_STR(x),x)
#define CERI_VSYMI(x,y) ceri_verify_symbol_i(#x,CERI_STR(x),x,y)
#define CERI_SIZE(x) ceri_print_symbol_i("sizeof("#x")","",sizeof(x))
#define CERI_FSIZE(t,f) ceri_print_symbol_i("sizeof("#t"."#f")","",(sizeof(((t*)0)->f)))

int ceri_errors = 0;
int ceri_undefineds = 0;

void ceri_print_symbol(const char *name, const char *sym) {
	printf("%s = ", name);
	if (strlen(sym) == 0) printf("<defined>");
	else if (strcmp(name, sym) == 0) {
		printf("<undefined?>");
		ceri_undefineds++;
	} else printf("\"%s\"", sym);
	printf("\n");
}

void ceri_print_symbol_i(const char *name, const char *sym, long int isym) {
	printf("%s = ", name);
	if (isym > -10 && isym <  10) printf("%ld", isym);
	else printf("%1$ld|0x%1$lx", isym);
	if (strlen(sym) > 0) printf(" \"%s\"", sym);
	printf("\n");
}

void ceri_verify_symbol_i(const char *name, const char *sym, long int isym, long int iexpect) {
	if (isym < -0xffffffff) isym &= 0xffffffff;
	if (isym != iexpect) {
		printf("[ERROR] 0x%lx != ", iexpect);
		ceri_errors++;
	}
	ceri_print_symbol_i(name, sym, isym);
}

int main(int argc, char *argv[]) {
	printf("\n");

$$CSYMBOLGEN_MAIN$$
	printf("\n");
	if (ceri_errors > 0) printf("[ERRORS: %d]\n", ceri_errors);
	if (ceri_undefineds > 0) printf("[undefined: %d]\n", ceri_undefineds);
	return 0;
}
