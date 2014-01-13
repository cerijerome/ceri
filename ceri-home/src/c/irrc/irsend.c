#include <sys/types.h>
#include <sys/time.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <pcirlinc.h> 
#include <fcntl.h>
#include <sys/stat.h>
#include <pwd.h>
#include <ctype.h>
#include "irdb.h"

#define CODE_INVALID	0
#define CODE_PRESET	1
#define CODE_LEARNED	2

void usage (void);
int parse_arg(char *arg, int *type, int *vendor, int *button);
void parse_count(char *arg, int *cycle, int *repeat);
void uppercase (char *str);

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
	int dbisopen= 0;

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

	if ( ! argc ) {
		usage();
		return 1;
	}

	if ( strchr(button, '/') != NULL ) {
		fprintf(stderr, "illegal character in command label\n");
		return 1;
	}

	if ( (port= irlinc_open(NULL)) == -1 ) {
		perror("irlinc_open");
		return 1;
	}

	while (argc) {
		int which, type, vendor, button, lcode;
		int cycle, repeat, i;
		char code[255], *func;

		cycle= repeat= 1;

		which= parse_arg(*argv, &type, &vendor, &button);
		if ( which == CODE_LEARNED ) func= *argv;
		argc--;
		argv++;

		if ( argc ) {
			parse_count(*argv, &cycle, &repeat);
			argc--;
			argv++;
		}

		if ( which == CODE_PRESET ) {
			for (i= 0; i< repeat; i++) {
				if ( irlinc_send_preset(port, type, vendor,
					button, cycle) == -1 ) {

					fprintf(stderr, "send failed\n");
					break;
				} 

				irlinc_msleep(50);
			}
		} else if ( which == CODE_LEARNED ) {
			if ( ! dbisopen ) {
				if ( (irdb= irdb_open(irdbfile)) == NULL ) {
					perror("irdb_open");
					return 1;
				}
				dbisopen++;
			}

			if ( irdb_get(irdb, func, code, &lcode) == -1 ) {
				fprintf(stderr, "function %s not found\n",
					func);
			} else {
				for (i= 0; i< repeat; i++) {
					if (irlinc_send_learned_ir(port, code,
						lcode, cycle) == -1 ) {

						fprintf(stderr,
							"send failed\n");
						break;
					}

					irlinc_msleep(50);
				}
			}
		} else {
		}
	}

	irlinc_close(port);
	if ( dbisopen ) irdb_close(irdb);

	return 0;
}

void usage () 
{
	fprintf(stderr, "usage: irlearn [ -d device ] [ -f dbfile ] cmd n ...\n");
}

int parse_arg(char *arg, int *type, int *vendor, int *button)
{
	int scount= 0;
	char *cp= arg;

	if ( *cp == '@' ) {
		arg++;
		uppercase(arg);
	} else {
		while ( *cp && ((cp= strchr(cp, '/')) != NULL) ) {
			scount++;
			cp++;
		}

		if ( scount ) {
			if (scount != 2)  return CODE_INVALID;
		} else {
			return CODE_LEARNED;
		}

		uppercase(arg);

		/* Parse the device type */

		cp= strsep(&arg, "/");

		if ( ! strcmp(cp, "TV") ) *type= TYPE_TV;
		else if ( ! strcmp(cp, "CABLE") )	*type= TYPE_CABLE;
		else if ( ! strcmp(cp, "VIDEO") )	*type= TYPE_VIDEO;
		else if ( ! strcmp(cp, "SATELLITE") )	*type= TYPE_SATELLITE;
		else if ( ! strcmp(cp, "VCR") )		*type= TYPE_VCR;
		else if ( ! strcmp(cp, "TAPE") )	*type= TYPE_TAPE;
		else if ( ! strcmp(cp, "LD") )		*type= TYPE_LD;
		else if ( ! strcmp(cp, "DAT") )		*type= TYPE_DAT;
		else if ( ! strcmp(cp, "DVD") )		*type= TYPE_DVD;
		else if ( ! strcmp(cp, "AMPTUNER") )	*type= TYPE_AMPTUNER;
		else if ( ! strcmp(cp, "MISCAUDIO") )	*type= TYPE_MISCAUDIO;
		else if ( ! strcmp(cp, "CD") )		*type= TYPE_CD;
		else if ( ! strcmp(cp, "PHONO") )	*type= TYPE_PHONO;
		else if ( ! strcmp(cp, "HOMEAUTO") )	*type= TYPE_HOMEAUTO;
		else CODE_INVALID;

		/* Parse the vendor code */

		cp= strsep(&arg, "/");
		*vendor= atoi(cp);
	} 

	/* Parse the button code */

	if ( ! strcmp(arg, "1") )			*button= BTN_1;
	else if ( ! strcmp(arg, "2") )			*button= BTN_2;
	else if ( ! strcmp(arg, "3") )			*button= BTN_3;
	else if ( ! strcmp(arg, "4") )			*button= BTN_4;
	else if ( ! strcmp(arg, "5") )			*button= BTN_5;
	else if ( ! strcmp(arg, "6") )			*button= BTN_6;
	else if ( ! strcmp(arg, "7") )			*button= BTN_7;
	else if ( ! strcmp(arg, "8") )			*button= BTN_8;
	else if ( ! strcmp(arg, "9") )			*button= BTN_9;
	else if ( ! strcmp(arg, "0") )			*button= BTN_0;
	else if ( ! strcmp(arg, "VOLUME_UP") )		*button= BTN_VOLUME_UP;
	else if ( ! strcmp(arg, "VOLUME_DOWN") )	*button= BTN_VOLUME_DOWN;
	else if ( ! strcmp(arg, "MUTE") )		*button= BTN_MUTE;
	else if ( ! strcmp(arg, "CHANNEL_UP") )		*button= BTN_CHANNEL_UP;
	else if ( ! strcmp(arg, "CHANNEL_DOWN") )	*button= BTN_CHANNEL_DOWN;
	else if ( ! strcmp(arg, "POWER") )		*button= BTN_POWER;
	else if ( ! strcmp(arg, "ENTER") )		*button= BTN_ENTER;
	else if ( ! strcmp(arg, "PREV_CHANNEL") )	*button= BTN_PREV_CHANNEL;
	else if ( ! strcmp(arg, "TV_VIDEO") )		*button= BTN_TV_VIDEO;
	else if ( ! strcmp(arg, "TV_VCR") )		*button= BTN_TV_VCR;
	else if ( ! strcmp(arg, "A_B") )		*button= BTN_A_B;
	else if ( ! strcmp(arg, "TV_DVD") )		*button= BTN_TV_DVD;
	else if ( ! strcmp(arg, "TV_LD") )		*button= BTN_TV_LD;
	else if ( ! strcmp(arg, "INPUT") )		*button= BTN_INPUT;
	else if ( ! strcmp(arg, "TV_DSS") )		*button= BTN_TV_DSS;
	else if ( ! strcmp(arg, "TV_SAT") )		*button= BTN_TV_SAT;
	else if ( ! strcmp(arg, "PLAY") )		*button= BTN_PLAY;
	else if ( ! strcmp(arg, "STOP") )		*button= BTN_STOP;
	else if ( ! strcmp(arg, "SEARCH_FORW") )	*button= BTN_SEARCH_FORW;
	else if ( ! strcmp(arg, "SEARCH_REV") )		*button= BTN_SEARCH_REV;
	else if ( ! strcmp(arg, "PAUSE") )		*button= BTN_PAUSE;
	else if ( ! strcmp(arg, "RECORD") )		*button= BTN_RECORD;
	else if ( ! strcmp(arg, "MENU") )		*button= BTN_MENU;
	else if ( ! strcmp(arg, "MENU_UP") )		*button= BTN_MENU_UP;
	else if ( ! strcmp(arg, "MENU_DOWN") )		*button= BTN_MENU_DOWN;
	else if ( ! strcmp(arg, "MENU_LEFT") )		*button= BTN_MENU_LEFT;
	else if ( ! strcmp(arg, "MENU_RIGHT") )		*button= BTN_MENU_RIGHT;
	else if ( ! strcmp(arg, "SELECT") )		*button= BTN_SELECT;
	else if ( ! strcmp(arg, "EXIT") )		*button= BTN_EXIT;
	else if ( ! strcmp(arg, "DISPLAY") )		*button= BTN_DISPLAY;
	else if ( ! strcmp(arg, "GUIDE") )		*button= BTN_GUIDE;
	else if ( ! strcmp(arg, "PAGE_UP") )		*button= BTN_PAGE_UP;
	else if ( ! strcmp(arg, "PAGE_DOWN") )		*button= BTN_PAGE_DOWN;
	else if ( ! strcmp(arg, "DISK") )		*button= BTN_DISK;
	else if ( ! strcmp(arg, "PLUS_10") )		*button= BTN_PLUS_10;
	else if ( ! strcmp(arg, "OPEN_CLOSE") )		*button= BTN_OPEN_CLOSE;
	else if ( ! strcmp(arg, "RANDOM") )		*button= BTN_RANDOM;
	else if ( ! strcmp(arg, "TRACK_FORW") )		*button= BTN_TRACK_FORW;
	else if ( ! strcmp(arg, "TRACK_REV") )		*button= BTN_TRACK_REV;
	else if ( ! strcmp(arg, "SURROUND") )		*button= BTN_SURROUND;
	else if ( ! strcmp(arg, "SURROUND_MODE") )	*button= BTN_SURROUND_MODE;
	else if ( ! strcmp(arg, "SURROUND_UP") )	*button= BTN_SURROUND_UP;
	else if ( ! strcmp(arg, "SURROUND_DOWN") )	*button= BTN_SURROUND_DOWN;
	else if ( ! strcmp(arg, "PIP") )		*button= BTN_PIP;
	else if ( ! strcmp(arg, "PIP_MOVE") )		*button= BTN_PIP_MOVE;
	else if ( ! strcmp(arg, "PIP_SWAP") )		*button= BTN_PIP_SWAP;
	else if ( ! strcmp(arg, "PROGRAM") )		*button= BTN_PROGRAM;
	else if ( ! strcmp(arg, "SLEEP") )		*button= BTN_SLEEP;
	else if ( ! strcmp(arg, "ON") )			*button= BTN_ON;
	else if ( ! strcmp(arg, "OFF") )		*button= BTN_OFF;
	else if ( ! strcmp(arg, "11") )			*button= BTN_11;
	else if ( ! strcmp(arg, "12") )			*button= BTN_12;
	else if ( ! strcmp(arg, "13") )			*button= BTN_13;
	else if ( ! strcmp(arg, "14") )			*button= BTN_14;
	else if ( ! strcmp(arg, "15") )			*button= BTN_15;
	else if ( ! strcmp(arg, "16") )			*button= BTN_16;
	else if ( ! strcmp(arg, "BRIGHT") )		*button= BTN_BRIGHT;
	else if ( ! strcmp(arg, "DIM") )		*button= BTN_DIM;
	else if ( ! strcmp(arg, "CLOSE") )		*button= BTN_CLOSE;
	else if ( ! strcmp(arg, "OPEN") )		*button= BTN_OPEN;
	else if ( ! strcmp(arg, "STOP2") )		*button= BTN_STOP2;
	else if ( ! strcmp(arg, "FM_AM") )		*button= BTN_FM_AM;
	else if ( ! strcmp(arg, "CUE") )		*button= BTN_CUE;
	else return CODE_INVALID;

	return CODE_PRESET;
}

void parse_count(char *arg, int *cycle, int *repeat)
{
	char *cp= arg;


	*repeat= atoi(arg);
	if ( ! *repeat ) *repeat= 1;

	cp= strchr(cp, '/');
	if ( cp != NULL ) {
		cp++;
		*cycle= atoi(cp);
		if ( ! *cycle ) *cycle= 1;
	}

	return;
}

void uppercase (char *str)
{
	int i;

	for(i= 0; i< strlen(str); i++) str[i]= (char) toupper(str[i]);
}

