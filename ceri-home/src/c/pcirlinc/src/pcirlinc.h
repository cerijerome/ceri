#ifndef __PCIRLINC__H
#define __PCIRLINC__H

#define	TYPE_TV		0
#define	TYPE_CABLE	1
#define	TYPE_VIDEO	2
#define	TYPE_SATELLITE	3
#define	TYPE_VCR	4
#define	TYPE_TAPE	5
#define	TYPE_LD		6
#define	TYPE_DAT	7
#define	TYPE_DVD	8
#define	TYPE_AMPTUNER	9
#define	TYPE_MISCAUDIO	10
#define	TYPE_CD		11
#define	TYPE_PHONO	12
#define	TYPE_HOMEAUTO	13

#define BTN_1			1
#define BTN_2			2
#define BTN_3			3
#define BTN_4			4
#define BTN_5			5
#define BTN_6			6
#define BTN_7			7
#define BTN_8			8
#define BTN_9			9
#define BTN_0			10
#define BTN_VOLUME_UP		11
#define BTN_VOLUME_DOWN		12
#define BTN_MUTE		13
#define BTN_CHANNEL_UP		14
#define BTN_CHANNEL_DOWN	15
#define BTN_POWER		16
#define BTN_ENTER		17
#define BTN_PREV_CHANNEL	18
#define BTN_TV_VIDEO		19
#define BTN_TV_VCR		20
#define BTN_A_B			21
#define BTN_TV_DVD		22
#define BTN_TV_LD		23
#define BTN_INPUT		24
#define BTN_TV_DSS		25
#define BTN_TV_SAT		25
#define BTN_PLAY		26
#define BTN_STOP		27
#define BTN_SEARCH_FORW		28
#define BTN_SEARCH_REV		29
#define BTN_PAUSE		30
#define BTN_RECORD		31
#define BTN_MENU		32
#define BTN_MENU_UP		33
#define BTN_MENU_DOWN		34
#define BTN_MENU_LEFT		35
#define BTN_MENU_RIGHT		36
#define BTN_SELECT		37
#define BTN_EXIT		38
#define BTN_DISPLAY		39
#define BTN_GUIDE		40
#define BTN_PAGE_UP		41
#define BTN_PAGE_DOWN		42
#define BTN_DISK		43
#define BTN_PLUS_10		44
#define BTN_OPEN_CLOSE		45
#define BTN_RANDOM		46
#define BTN_TRACK_FORW		47
#define BTN_TRACK_REV		48
#define BTN_SURROUND		49
#define BTN_SURROUND_MODE	50
#define BTN_SURROUND_UP		51
#define BTN_SURROUND_DOWN	52
#define BTN_PIP			53
#define BTN_PIP_MOVE		54
#define BTN_PIP_SWAP		55
#define BTN_PROGRAM		56
#define BTN_SLEEP		57
#define BTN_ON			58
#define BTN_OFF			59
#define BTN_11			60
#define BTN_12			61
#define BTN_13			62
#define BTN_14			63
#define BTN_15			64
#define BTN_16			65
#define BTN_BRIGHT		66
#define BTN_DIM			67
#define BTN_CLOSE		68
#define BTN_OPEN		69
#define BTN_STOP2		70
#define BTN_FM_AM		71
#define BTN_CUE			72

#define EO_ERRNO	-1

int irlinc_open (char *device);
int irlinc_close (int port);
int irlinc_send_preset (int port, int type, int vendor, int button, 
	unsigned int n);
int irlinc_learn_ir (int port, char *buf, size_t *length);
int irlinc_send_learned_ir (int port, char *code, size_t length, unsigned
	int n);
void irlinc_msleep(long ms);

#endif

