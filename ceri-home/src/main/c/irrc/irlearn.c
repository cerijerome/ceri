#include <sys/types.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <pcirlinc.h> 
#include <fcntl.h>
#include <sys/stat.h>
#include <pwd.h>
#include "irdb.h"

void usage (void);

int main (int argc, char **argv)
{
	extern int optind;
	extern char *optarg;
	char *sdevice= NULL;
	char *irdbfile= NULL;
	char *button;
	char buf[256];
	int ch;
	irdb_t *irdb;
	int port;
	size_t lcode;

	while ( (ch= getopt(argc, argv, "d:f:h")) != EOF ) {
		switch (ch) {
		case 'd':
			sdevice= optarg;
			break;
		case 'f':
			irdbfile= optarg;
			break;
		case 'h':
		default:
			usage();
			return 1;
		}
	}
	argc-= optind;
	argv+= optind;

	if ( argc != 1 ) {
		usage();
		return 1;
	}

	button= argv[0];

	if ( strcspn(button, "/@") != strlen(button) ) {
		fprintf(stderr, "illegal character in command label\n");
		return 1;
	}

	if ( (irdb= irdb_open(irdbfile)) == NULL ) {
		perror("irdb_open");
		return 1;
	}

	if ( (port= irlinc_open(NULL)) == -1 ) {
		perror("irlinc_open");
		return 1;
	}

	if ( irlinc_learn_ir(port, buf, &lcode) == -1 ) {
		fprintf(stderr, "learn failure\n");
		irlinc_close(port);
		return 1;
	}

	if ( irdb_put(irdb, button, buf, lcode) == -1 ) {
		perror("irdb_put");
	}

	irlinc_close(port);
	irdb_close(irdb);

	return 0;
}

void usage () 
{
	fprintf(stderr, "usage: irlearn [ -d device ] [ -f dbfile ] cmd_label\n");
}
