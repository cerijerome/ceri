#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <fcntl.h>
#include <limits.h>
#include <pwd.h>
#ifdef linux
#include <db1/db.h>
#else
#include <db.h>
#endif
#include "irdb.h"

irdb_t *irdb_open (char *dbfile)
{
	irdb_t *irdb;
	BTREEINFO dbinfo;

	irdb= (irdb_t *) malloc(sizeof(irdb_t));
	if ( irdb == NULL ) return NULL;

	if ( dbfile == NULL ) {
		struct passwd *pw;

		if ( (pw= getpwuid(getuid())) == NULL ) return NULL;
		irdb->file= (char *) malloc(strlen(pw->pw_dir) +
			strlen(DEFAULT_IRDB_PATH) +2);
		if ( irdb->file == NULL ) return NULL;

		sprintf(irdb->file, "%s/%s", pw->pw_dir, DEFAULT_IRDB_PATH);
	} else {
		irdb->file= strdup(dbfile);
		if ( irdb->file == NULL ) return NULL;
	}

	memset(&dbinfo, 0, sizeof(dbinfo));
	dbinfo.lorder= 1234;

	if ( (irdb->db= dbopen(irdb->file, O_CREAT|O_RDWR,
		S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP, DB_BTREE, (void *) &dbinfo))
		== NULL ) {

		return NULL;
	}

	return irdb;
}

void irdb_close (irdb_t *irdb)
{
	(irdb->db->close)(irdb->db);
	free(irdb->file);
	free(irdb);
}

int irdb_get (irdb_t *irdb, const char *func, char *code, size_t *lcode)
{
	int status;
	DBT key;
	DBT data;

	memset(&key, 0, sizeof(key));
	memset(&data, 0, sizeof(data));

	key.data= (void *) func;
	key.size= strlen(func);

	status= (irdb->db->get)(irdb->db, &key, &data, 0);
	if ( status ) {
		*lcode= 0;
		return (status == -1) ? -1 : 0;
	}

	memcpy(code, data.data, data.size);
	*lcode= data.size;

	return 0;
}

int irdb_put (irdb_t *irdb, const char *func, const char *code, size_t lcode)
{
	DBT key;
	DBT data;

	key.data= (void *) func;
	key.size= strlen(func);

	data.data= (void *) code;
	data.size= lcode;

	if ( (irdb->db->put)(irdb->db, &key, &data, 0) ) {
		return -1;
	}

	return 0;
}

