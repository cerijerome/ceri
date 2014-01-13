#ifndef __IRDB__H
#define __IRDB__H

#ifdef linux
#include <db1/db.h>
#else
#include <db.h>
#endif

typedef struct {
	DB *db;
	char *file;
} irdb_t;

#define DEFAULT_IRDB_PATH ".irdb"

irdb_t *irdb_open (char *dbfile);
void irdb_close (irdb_t *irdb);
int irdb_get (irdb_t *irdb, const char *func, char *code, size_t *lcode);
int irdb_put (irdb_t *irdb, const char *func, const char *code, size_t lcode);

#endif

