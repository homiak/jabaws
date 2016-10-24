#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include "getquery.h"
#include "gjutil.h"

/* get the next sequence entry from a fasta format file and
   return a pointer to a structure containing this information.

   Format expected of the input file is:

   >IDENT Title here 
   ONE LETTER CODE SEQUENCE ON SEVERAL LINES
   LIKE THIS
   >NEXTIDENT  Next title etc.

   The routine reads lines from the file until it finds one that starts
   in '>'.  It then reads this line as an ID, title line before reading
   all alphabetic characters that follow as the amino acid sequence.  
   The sequence is terminated by the next '>' or End of File.

   This means that the must ONLY contain sequences.  PIR format permits 
   more flexibility in the input file.

   NOTE:  This routine assumes that no line will be longer than the 
   p->MAX_BUFFER_LEN.  Any program that calls this routine should
   have p->MAX_BUFFER_LEN set suitably big.  This can be done by 
   pre-checking the database file for line lengths.

   Author: G. J. Barton (October 1995)

*/
   
SEQS *gseq_fasta(FILE *seqfile)
{

  char *buff,*tbuff;
  SEQS *ret_val;
  int MAX_BUFFER_LEN = 10000; 	
  int MAX_SEQ_LEN = 10000; 
  char *ident = NULL;
  char *title = NULL;

  GJ_S_COUNT j;
  int c;
  STD_FILES;

  buff = (char *) GJmalloc(sizeof(char) * MAX_BUFFER_LEN);
  tbuff = buff;

  while((buff = fgets(buff,MAX_BUFFER_LEN,seqfile)) != NULL) {
    if(buff[0] == '>') {
      ret_val = (SEQS *) GJmalloc(sizeof(SEQS));
      ident = strtok(&buff[1]," ");
      if(ident != NULL) {
	ident=GJremovechar2(ident,'\n');
	ret_val->id = GJstrdup(ident);
	ret_val->ilen = strlen(ident);
      }else {
	GJerror("Something strange with sequence identifier in fasta file");
	fprintf(std_err,"Line:%s\n",buff);
	return NULL;
      }
      title = strtok(NULL,"\n");
      if(title != NULL) {
	ret_val->title = GJstrdup(title);
	ret_val->tlen = strlen(title);
      }else {
	/*	GJerror("Something strange with sequence title in fasta file");*/
	/*	fprintf(std_err,"Line:%s\n",buff);*/
	/*if(p->VERBOSE > 10)fprintf(std_err,"Title missing in FASTA file - Inserting dummy: %s\n",buff);*/
	ret_val->title = GJstrdup("-");
	ret_val->tlen = strlen(ret_val->title);
	/*	return NULL;*/
      }
      ret_val->seq = (char *) GJmalloc(sizeof(char) * MAX_SEQ_LEN);
      ret_val->seq[0] = ' ';
      j = 0;
      for(;;) {
	c = fgetc(seqfile);
	if(c == EOF || c == '>') {
	  ungetc(c,seqfile);
	  ret_val->seq[j] = '\0';
	  ret_val->slen = j;
	  ret_val->seq = (char *) GJrealloc(ret_val->seq,sizeof(char) * ret_val->slen);
	  GJfree(buff);
	  return ret_val;
  	}else if(isalpha(c)) {
	  if(j == (MAX_SEQ_LEN - 3)){
	    fprintf(std_err,"Sequence too long: %s (> %d residues):  Increase MAX_SEQ_LEN\n",
		    ret_val->id,j);
	    exit(-1);
	  }
	  ret_val->seq[j++] = toupper(c);
	}
      }
    }
  }
  GJfree(tbuff);
  return(NULL);
}

/*
int main(int argc, char **argv)
{
   FILE *fasta; 
   SEQS *seq; 
   fasta = fopen("/homes/pvtroshin/large.fasta", "r");
	
   do{
     seq = gseq_fasta(fasta); 
     if(seq!=NULL) printf("Seq: %s\n",seq->id );
   } while(seq != NULL);

   fclose(fasta);
  return 0;
}
*/
