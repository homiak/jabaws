#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include "gjutil.h"

/* Standard structure for storing protein sequence data */

typedef struct seqdat {	/* all lengths include char terminator and [0] */
    char *id;	/* identifier */
    int ilen;
    char *title;/* title */
    int tlen;
    int slen;	/* length of sequence*/
    char *seq;	/* sequence */
}SEQS;


SEQS *gseq_fasta(FILE *seqfile);
