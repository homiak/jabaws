#ifndef GJ_UTIL_H
#define GJ_UTIL_H

/*
$Id: gjutil.h,v 1.8 2002/08/09 12:30:31 geoff Exp $
*/

#include <stdio.h>


typedef int GJ_LIM;              /* type for limits - e.g. max buffer length */
typedef int GJ_PEN;              /* type for gap penalties */
typedef int GJ_FLG;              /* Flag type - usually just 1 or 0 */
typedef float GJ_FLOAT;          /* Single precision floating point type */
typedef double GJ_DBL;           /* a double precision floating point type */
typedef int GJ_S_COUNT;          /* small counter */
typedef long GJ_L_COUNT;         /* long counter */

typedef struct {                    /* structure to hold a filename and */
  char *name;                    /* associated handle */
  FILE *handle;
} GJFILE;

struct tokens {                  /* structure to hold tokens parsed from */
  int ntok;                      /* string with strtok */
  char **tok;
};

typedef struct {
  int min;
  int max;
} IRANGE;

typedef struct {
  char *val;
  int i;
} CWORK;

int Sworkcomp(const void *left, const void *right);
void GJindexxS(char **arrin,int n,int *indx);

#define STD_FILES extern FILE *std_in,*std_out,*std_err

/* utility.h function definitions */

void *GJmalloc(size_t);
void *GJrealloc(void *,size_t);
void *GJmallocNQ(size_t);
void *GJreallocNQ(void *,size_t);
void GJfree(void *);
void GJerror(const char *);
char *GJstrdup(const char *);
char *GJstoupper(const char *);
char *GJstolower(const char *);
char *GJstoup(char *);
char *GJstolo(char *);

FILE *GJfopen(const char *, const char *,int);
int  GJfclose(FILE *,int);
GJFILE *GJfilemake(const char *name,const char *type,int action);
GJFILE *GJfilerename(GJFILE *ret_val, const char *name);
GJFILE *GJfileclose(GJFILE *ret_val,int action);
GJFILE *GJfileopen(GJFILE *ret_val,const char *type,int action);
GJFILE *GJfileclean(GJFILE *fval,int action);
void GJinitfile(void);

char *GJfnonnull(char *);
char *GJstrappend(char *,char *);
char *GJremovechar(char *,char);
char *GJremovechar2(char *string,char c);
char *GJstrcreate(size_t, char *);
char *GJstrlocate(char *,char *);
char *GJsubchar(char *,char,char);
char *GJstrtok(char *,const char *);
void error(const char *, int);
unsigned char **uchararr(int,int);
void ucharfree(unsigned char **array,int i);
double **GJdarr(int i,int j);
void GJdarrfree(double **array,int i);
signed   char **chararr(int,int);
void GJCinit(signed char **,int ,int ,char );
void mcheck(void *, char *);
char *GJstrblank(char *, int);
void GJUCinit(unsigned char **,int ,int ,unsigned char );
char *GJcat(int N,...);
struct tokens *GJgettokens(const char *delims, char *buff);
void GJfreetokens(struct tokens *tok);
char * GJtoktostr(struct tokens *tok, char delim, int s, int e);
void GJ_start_clock(void);
void GJ_stop_clock(FILE *fp);
char *GJstrrename(char *old,const char *new);
void GJindexx(int *arrin,int n,int *indx);
void GJindexxD(double *arrin,int n,int *indx);
void GJindexxS(char **arrin,int n,int *indx);
int GJbsearchINXS(char **cod, int n, char *query);
int GJbsearchINX_IS(char **cod, int *inx, int n, char *query);

void GJpline(FILE *fp,char c,int n);
char *GJlocaltime(void);
void GJpstring(FILE *fp,char *s,int n);

IRANGE *irange(int *ivec, int n);

/*
$Log: gjutil.h,v $
Revision 1.8  2002/08/09 12:30:31  geoff
Experiment with different weighting schemes.
Changes to build_profile to accommodate new schemes
pwf 3  Complex clustering to get sequence weights at each position
pwf 4  Dirichlet mixture alone
pwf 5  Dirichlet mixture + psiblast windowing + blosum weights
pwf 6  local blosum matrix calculation + HH sequence weights + blosum

Also add wander_check opton
Also add option to turn off sw7 bug work around.
Add option to suppress multiple alignment output
Add gjnoc2 to distribution

Revision 1.7  2000/12/21 17:25:44  geoff
Add the option to output the sequence fragments from the multiple alignment
output option in fasta or pir format.  Unlike the block file output, these
fragments contain the complete sequence between the start and end points, including
any parts deleted in the alignment process.
Add appropriate commands to scanps_alias.dat, getpars and getcmd.

Revision 1.6  1999/11/17 21:06:47  geoff
Add setup_caleb and other changes to swpp2 and so on.

Revision 1.5  1997/06/27 07:17:32  gjb
Added rlic.c linfo.h and
changes to gjutil.c to give better support for
token manipulation

Revision 1.4  1997/05/12 11:10:54  gjb
Re-added gjutil.c and gjutil.h to repository
after deleting them

Revision 1.2  1997/05/12 10:47:52  gjb
Modified CVS header and log position

*/
#endif  /* GJ_UTIL_H */

