#include "../config.h"
#include <stdio.h>
#include <string.h>
#include "error.h"

#define MAX_ERRORS 3
#define MAX_ERRLEN 254

int error_overflow (void);

int irlinc_error= 0;

static struct irlinc_error_stack_struct {
	int idx;
	int count;
	char msg[MAX_ERRORS+1][MAX_ERRLEN+1];
} estack;

int error_overflow (void)
{
	if ( estack.count >= MAX_ERRORS ) {
		strcpy(estack.msg[MAX_ERRORS], "Error stack overflow");
		estack.count= MAX_ERRORS+1;
		return 1;
	}

	return 0;
}

void error_add (int code)
{
	char *ep= estack.msg[estack.count];

	if ( error_overflow() ) return;

	snprintf(ep, MAX_ERRLEN, "%s", error_string(code));

	irlinc_error= code;
	estack.count++;
}

void error_status (unsigned char code)
{
	char *ep= estack.msg[estack.count];

	if ( _error_overflow() ) return;

	snprintf(ep, MAX_ERRLEN, "%s", error_status_string(code));

	irlinc_error= EO_BADSTATUS;
	estack.count++;
}

void error_clear (void)
{
	estack.count= 0;
	estack.idx= 0;
}
