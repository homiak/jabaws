
/****************************************************************************

gjutil.c:  Various utility routines - error checking malloc and
free, string functions etc...

Copyright:  Geoffrey J. Barton  (1992, 1993, 1995, 1997)
email: geoff@ebi.ac.uk

This software is made available for educational and non-commercial research 
purposes.

For commercial use, a commercial licence is required - contact the author
at the above address for details.


******************************************************************************/
#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <time.h>

#include <gjutil.h>

/* define pointers for standard streams to allow redefinition to files */

FILE *std_err;
FILE *std_in;
FILE *std_out;

/* clock externs */
clock_t start_time, end_time,initial_time,final_time;

void *GJmalloc(size_t size)
/* malloc with simple error check */
/* G. J. Barton 1992 */
{
	void *ptr;
	ptr = (void *) malloc(size);
	if(ptr == NULL){
		GJerror("malloc error");
		exit(0);
	}
	return ptr;
}

void *GJrealloc(void *ptr,size_t size)
/* realloc with error check */
/* G. J. Barton 1992 */
{
	ptr = (void *) realloc(ptr,size);
	if(ptr == NULL){
		GJerror("realloc error");
		exit(0);
	}
	return ptr;
}
void *GJmallocNQ(size_t size)
/* as for GJmalloc, but returns NULL on error*/
/* G. J. Barton 1992 */
{
	void *ptr;
	ptr = (void *) malloc(size);
	if(ptr == NULL){
		GJerror("malloc error");
                return NULL;
	}
	return ptr;
}

void *GJreallocNQ(void *ptr,size_t size)
/* as for GJrealloc with error check but returns NULL on error*/
/* G. J. Barton 1992 */
{
	ptr = (void *) realloc(ptr,size);
	if(ptr == NULL){
		GJerror("realloc error");
		return NULL;
	}
	return ptr;
}
void GJfree(void *ptr)
/* free with error check */
/* G. J. Barton 1992 */
{
	if(ptr == NULL){
		GJerror("Attempt to free NULL pointer");
		exit(0);
	}
	free(ptr);
}

void GJerror(const char *prefix)
/* writes error message contained in prefix and contents of errno
   to std_err.
*/
/* G. J. Barton 1992 */
{
	if(prefix != NULL){
		if(*prefix != '\0'){
			fprintf(std_err,"%s: ",prefix);
		}
	}
	fprintf(std_err,"%s\n",strerror(errno));
}

/*
error:   calls GJerror 
*/
void error(const char *str,int flag)
{
    GJerror(str);
    if(flag)exit(0);
}


char *GJstoupper(const char *s)
/* return a copy of s in upper case */
/* G. J. Barton 1992 */
{
	char *temp;
	int i;
	temp = GJstrdup(s);
	i=0;
	while(temp[i] != '\0'){
		temp[i] = toupper(temp[i]);
		++i;
	}
	return temp;
}
char *GJstolower(const char *s)
/* return a copy of s in lower case */
/* G. J. Barton 1992 */
{
	char *temp;
	int i;
	temp = GJstrdup(s);
	i=0;
	while(temp[i] != '\0'){
		temp[i] = tolower(temp[i]);
		++i;
	}
	return temp;
}
char *GJstoup(char *s)
/* return s in upper case */
/* G. J. Barton 1992 */
{
	int i;
	i=0;
	while(s[i] != '\0'){
		s[i] = toupper(s[i]);
		++i;
	}
	return s;
}
char *GJstolo(char *s)
/* return s in lower case */
/* G. J. Barton 1992 */
{
	int i;
	i=0;
	while(s[i] != '\0'){
		s[i] = tolower(s[i]);
		++i;
	}
	return s;
}  

char *GJstrdup(const char *s)
/* returns a pointer to a copy of string s */
/* G. J. Barton 1992 */

{
	char *temp;
	temp = (char *) GJmalloc(sizeof(char) * (strlen(s)+1));
	temp = strcpy(temp,s);
	return temp;
}

char *GJstrrename(char *old,const char *new)
/* takes old which is a pointer to a string, then replaces the contents
   of the string with new, reallocating to the correct size
*/ 
{
  int nlen;
  nlen = strlen(new);
  old = (char *) GJrealloc(old,sizeof(char) * (nlen + 1));
  old = strcpy(old,new);
  return old;
}
  


FILE *GJfopen(const char *fname,const char *type,int action)
/* a file open function with error checking.  The third argument
is set to 0 if we want a failed open to return, or 1 if we
want a failed open to exit the program.
*/
/* G. J. Barton 1992 */
/* modified July 1995 - error message only printed if action is 1 */
{
	FILE *ret_val;
	ret_val = fopen(fname,type);
	if(ret_val == NULL){
	  /*	  GJerror(strcat("Cannot Open File: ",fname));*/
		if(action == 1){
		  GJerror(strcat("Cannot Open File: ",fname));
		  exit(1);
		}
	}
	return ret_val;
}

int GJfclose(FILE *fp,int action)
/* a file close function with error checking.  The second argument
is set to 0 if we want a failed close to return, or 1 if we
want a failed close to exit the program.
*/
/* G. J. Barton 1992 */
{
	int ret_val;
	ret_val = fclose(fp);
	if(ret_val != 0){
		if(action == 1){
		        GJerror("Error closing File");
			exit(1);
		}
	}
	return ret_val;
}


GJFILE *GJfilemake(const char *name,const char *type,int action)
/* If action = 1 then 
Tries to open the file with the given name.  If successful returns 
a pointer to a struct file structure with the name and filehandle.  If
the open fails, or action= 0 then returns a struct file structure 
with the name and a NULL filehandle */
/* G. J. Barton 1995 */
{
	GJFILE *ret_val;
	ret_val = (GJFILE *) GJmalloc(sizeof(GJFILE));
	ret_val->name = GJstrdup(name);
	if(action == 1) {
	  ret_val->handle = GJfopen(ret_val->name,type,0);
	}else if(action == 0){
	  ret_val->handle = NULL;
	}
	return ret_val;
}

GJFILE *GJfilerename(GJFILE *ret_val, const char *name)
/* When passed the fval structure - renames the name part of the
file structure to name, if the handle is non null it tries to close 
the file, then sets the file handle to NULL. */
/* G. J. Barton 1995 */
{
	if(ret_val->name != NULL) {
	  GJfree(ret_val->name);
	  ret_val->name = GJstrdup(name);
	}
	if(ret_val->handle != NULL) {
	  GJfclose(ret_val->handle,0);
	  ret_val->handle = NULL;
	}
	return ret_val;
}

GJFILE *GJfileclose(GJFILE *ret_val,int action)
/* Closes a file named in the  struct file structure returns the struct
 file structure */

/* G. J. Barton July 1995 */
{
        STD_FILES;

	if(GJfclose(ret_val->handle,0) == 0){
	  return ret_val;
	}else {
	  if(action == 1){
	    GJerror("Error closing File");
	    fprintf(std_err,"%s\n",ret_val->name);
	    exit(-1);
	  }
	}
	return ret_val;  
}

GJFILE *GJfileopen(GJFILE *ret_val,const char *type,int action)
/* Opens a file named in the  struct file structure */

/* G. J. Barton October 1995 */
{
	STD_FILES;

	ret_val->handle = GJfopen(ret_val->name,type,0);
	if(ret_val->handle == NULL){
		if(action == 1){
		        GJerror("Error opening File");
			fprintf(std_err,"%s\n",ret_val->name);
			exit(-1);
		}
	}
	return ret_val;  
}

GJFILE *GJfileclean(GJFILE *ret_val,int action)
/*  Closes the file then sets the file pointer to NULL, then 
    frees the filename string */

/* G. J. Barton July 1995 */
{
  if(GJfclose(ret_val->handle,0) == 0) {
    ret_val->handle = NULL;
    GJfree(ret_val->name);
    return ret_val;
  }else {
    if(action == 1){
      GJerror("Error closing File");
      fprintf(std_err,"%s\n",ret_val->name);
      exit(-1);
    }
  }
  return ret_val;
}

void GJinitfile(void)
/* just set the standard streams */
{
	std_err = stderr;
	std_in = stdin;
	std_out = stdout;
}

char *GJfnonnull(char *string)
/* return pointer to first non null character in the string */
/* this could cause problems if the string is not null terminated */
{
	while(*string != '\0'){
		++string;
	}
	return ++string;
}

char *GJstrappend(char *string1, char *string2)
/* appends string2 to the end of string2.  Any newline characters are removed
from string1, then the first character of string2 overwrites the null at the
end of string1.
string1 and string2 must have been allocated with malloc.
*/
/* G. J. Barton July 1992 */
{
	char *ret_val;
	ret_val = GJremovechar(string1,'\n');
	ret_val = (char *) GJrealloc(ret_val,
			   sizeof(char) * (strlen(ret_val) + strlen(string2) + 1));
        ret_val = strcat(ret_val,string2);
        return ret_val;
}

char *GJremovechar(char *string,char c)
/* removes all instances of character c from string
   returns a pointer to the reduced, null terminated string
   11/8/1996:  couple of bugs found in this routine.
   the length of the string returned was too short by 2 bytes.
   This is a dodgy routine since string is freed.
*/
/* G. J. Barton (July 1992) */
{
	char *temp;
	int j,i,nchar;
	nchar = 0;
	i=0;
	while(string[i] != '\0'){
		if(string[i] == c){
			++nchar;
		}
		++i;
	}
	if(nchar == 0){
		 return string;
	}else{
		temp = (char *) GJmalloc(sizeof(char) * (strlen(string)-nchar + 1));
		j=0;
		i=0;
		while(string[i] != '\0'){
			if(string[i] != c){
				temp[j] = string[i];
				++j;
			}
			++i;
		}
		temp[j] = '\0';
		GJfree(string);
		return temp;
	}
}

char *GJremovechar2(char *string,char c)
/* removes all instances of character c from string
   returns a pointer to the reduced, null terminated string
*/
/* G. J. Barton (July 1992) */
{
	char *temp;
	int i,k,len;
	k=0;
	len=strlen(string);
	temp = (char *) GJmalloc(sizeof(char) * (len+1));
	for(i=0;i<(len+1);++i){
	  if(string[i] != c){
	    temp[k] = string[i];
	    ++k;
	  }
	}
	for(i=0;i<(strlen(temp)+1);++i){
	  string[i] = temp[i];
	}
	GJfree(temp);
	return string;
}


char *GJsubchar(char *string,char c2,char c1)
/* substitutes c1 for c2 in string
*/
/* G. J. Barton (July 1992) */
{
	int i;

	i=0;
	while(string[i] != '\0'){
		if(string[i] == c1){
                    string[i] = c2;
		}
		++i;
	}
	return string;
}

/* create a string and if fchar != NULL fill with characters  */
/* always set the len-1 character to '\0' */

char *GJstrcreate(size_t len,char *fchar)
{
	char *ret_val;
	ret_val = (char *) GJmalloc(sizeof(char) * len);
	--len;
	ret_val[len] = '\0';
	if(fchar != NULL){
		while(len > -1){
			ret_val[len] = *fchar;
			--len;
		}
	}
	return ret_val;
}

/* searches for string s2 in string s1 and returns pointer to first instance
of s2 in s1 or NULL if no instance found.  s1 and s2 must be null terminated
*/		
char *GJstrlocate(char *s1, char *s2)
{
    int i=0;
    int j=0;
    int k;
    if(strlen(s1) == 0 || strlen(s2) == 0) return NULL;
    while(i < strlen(s1)){
        j=0;
        k=i;
        while(j < strlen(s2) && s1[k] == s2[j]){
                ++k;
                ++j;
        }
        if(j == strlen(s2)) return &s1[i];
        ++i;
    }
    return NULL;
}
#include <stdlib.h>
#include <string.h>


/* GJstrtok()

This version of strtok places the work pointer at the location of the first 
character in the next token, rather than just after the last character of the 
current token.  This is useful for extracting quoted strings 
*/

char *GJstrtok(char *input_string,const char *token_list)
{
  static char *work;
  char *return_ptr;

  if(input_string != NULL){
    /* first call */
    work =  input_string;
  }

  /* search for next non-token character */
  while(strchr(token_list,*work)!=NULL){
    ++work;
  }

  if(*work == '\0'){
    /* if we've reached the end of string, then return NULL */
    return NULL;
  }else{
    return_ptr = (char *) work;
    while(strchr(token_list,*work) == NULL){
      if(*work == '\0'){
	/* end of the string */
	return return_ptr;
      }else{
	++work;
      }
    }
    *work = '\0';
    ++work;
    /* now increment work until we find the next non-delimiter character */
    while(strchr(token_list,*work) != NULL){
      if(*work == '\0'){
	break;
      }else{
	++work;
      }
    }
    return return_ptr;
  }
}
/**************************************************************************
return a pointer to space for a rectangular unsigned character array
Version 2.0  ANSI and uses GJmallocNQ
--------------------------------------------------------------------------*/

unsigned char **uchararr(int i,int j)
{
    unsigned char **temp;
    int k, rowsiz;

    temp = (unsigned char **) GJmallocNQ(sizeof(unsigned char *) * i);
    if(temp == NULL) return NULL;

    rowsiz = sizeof(unsigned char) * j;

    for (k = 0; k < i; ++k){
	temp[k] =  (unsigned char *) GJmallocNQ(rowsiz);
	if(temp[k] == NULL) return NULL;
    }
    return temp;
}

/**************************************************************************
free up space pointed to by rectangular unsigned character array
-------------------------------------------------------------------------*/
void ucharfree(unsigned char **array,int i)

{
    int k;

    for (k = 0; k < i; ++k){
	GJfree((char *) array[k]);
    }
    GJfree((char *) array);

}
/**************************************************************************
return a pointer to space for a rectangular double array
--------------------------------------------------------------------------*/

double **GJdarr(int i,int j)
{
    double **temp;
    int k, rowsiz;

    temp = (double **) GJmallocNQ(sizeof(double *) * i);
    if(temp == NULL) return NULL;

    rowsiz = sizeof(double) * j;

    for (k = 0; k < i; ++k){
	temp[k] =  (double *) GJmallocNQ(rowsiz);
	if(temp[k] == NULL) return NULL;
    }
    return temp;
}
void GJdarrfree(double **array,int i)

{
    int k;

    for (k = 0; k < i; ++k){
	GJfree((char *) array[k]);
    }
    GJfree((char *) array);

}

/**************************************************************************
return a pointer to space for a rectangular signed character array
Version 2.0  ANSI
--------------------------------------------------------------------------*/
signed char **chararr(int i,int j)

{
    signed char **temp;
    int k, rowsiz;

    temp = (signed char **) GJmallocNQ(sizeof(char *) * i);

    if(temp == NULL) return NULL;

    rowsiz = sizeof(char) * j;

    for (k = 0; k < i; ++k){
	temp[k] =  (signed char *) GJmallocNQ(rowsiz);
	if(temp[k] == NULL) return NULL;
    }
    return temp;
}


/* mcheck - check a call to malloc - if the call has failed, print the
error message and exit the program */
/* ANSI Version - also uses GJerror routine and ptr is declared void*/

void mcheck(void *ptr,char *msg)

{
    if(ptr == NULL){
        GJerror("malloc/realloc error");
	exit(0);
    }
}

/* set a string to blanks and add terminating nul */
char *GJstrblank(char *string,int len)

{
  --len;
  string[len] = '\0';
  --len;
  while(len > -1){
    string[len] = ' ';
    --len;
  }
  return string;
}

/* Initialise an unsigned char array */  
void GJUCinit(unsigned char **array,int i,int j,unsigned char val)
{
  int k,l;

  for(k=0;k<i;++k){
    for(l=0;l<j;++l){
      array[k][l] = val;
    }
  }
}
/*Initialise a signed char array */

void GJCinit(signed char **array,int i,int j,char val)

{
  int k,l;

  for(k=0;k<i;++k){
    for(l=0;l<j;++l){
      array[k][l] = val;
    }
  }
}

/* Initialise an integer vector  */  
void GJIinit(int *array,int i,int val)
{
  int k;

  for(k=0;k<i;++k){
      array[k] = val;
  }
}

/******************************************************************
GJcat:  concatenate N NULL terminated strings into a single string.
The source strings are not altered
Author: G. J. Barton (Feb 1993)
------------------------------------------------------------------*/
char *GJcat(int N,...)
{
	va_list parminfo;
	int i,j,k;
	char **values;	/*input strings */
	int *value_len; /*lengths of input strings */
	int ret_len;    /*length of returned string */
	char *ret_val;  /*returned string */

	ret_len = 0;
	values = (char **) GJmalloc(sizeof(char *) * N);
	value_len = (int *) GJmalloc(sizeof(int *) * N);

	va_start(parminfo,N);

	/* Get pointers and lengths for the N arguments */
	for(i=0;i<N;++i){
		values[i] = va_arg(parminfo,char *);
		value_len[i] = strlen(values[i]);
		ret_len += value_len[i];
	}
	
	ret_val = (char *) GJmalloc(sizeof(char) * (ret_len+1));

	/* Transfer input strings to output string */
	k=0;
	for(i=0;i<N;++i){
		for(j=0;j<value_len[i];++j){
			ret_val[k] = values[i][j];
			++k;
		}
	}
	ret_val[k] = '\0';
	GJfree(values);
	GJfree(value_len);

	va_end(parminfo);

	return ret_val;
}

/************************************************************************

GJGetToken:

The aim of this routine is to emulate the strtok() function, but reading
from a file.  The functionality may differ slightly...

Characters are read from the file until a character that is found in
delim is encountered.  When this occurs, token is returned.  If the
file consists entirely of delimiters, then token is freed
and NULL is returned.  Similarly, if end of file is encountered.

------------------------------------------------------------------------*/


char *GJGetToken(FILE *in, const char *delim)

{
	int i;
	int c;
	char *token;

	i=0;

	token = GJmalloc(sizeof(char));
	
	while((c=fgetc(in)) != EOF){
		if(strchr(delim,c) == NULL){
			/* not a delimiter */
			token[i++] = c;
			token = GJrealloc(token,sizeof(char) * (i+1));
		}else if(i>0){
		        token[i] = '\0';
			return token;
		}
	}
/*	GJerror("End of File Encountered");*/
	GJfree(token);
	return NULL;
}

struct tokens *GJgettokens(const char *delims, char *buff)
/* This splits a buffer into tokens at each position defined by delims.*/
/* The structure returned contains the number of tokens and the */
/* tokens themselves as a char ** array */
{
  char *token;
  struct tokens *tok;

  token = strtok(buff,delims);
  if(token == NULL) return NULL;

  tok = (struct tokens *) GJmalloc(sizeof(struct tokens));
  tok->ntok = 0;
  tok->tok = (char **) GJmalloc(sizeof(char *));
  tok->tok[0] = GJstrdup(token);
  ++tok->ntok;
  while((token = strtok(NULL,delims)) != NULL) {
      tok->tok = (char **) GJrealloc(tok->tok,sizeof(char *) * (tok->ntok+1));
      tok->tok[tok->ntok] = GJstrdup(token);
      ++tok->ntok;
  }
  
  return tok;
}

void GJfreetokens(struct tokens *tok)
/* frees a tokens structure */

{
  int i;
  for(i=0;i<tok->ntok;++i) {
    GJfree(tok->tok[i]);
  }
  GJfree(tok->tok);
  GJfree(tok);
  tok = NULL;   /* add this to avoid odd pointer 27/6/1997*/
}

char * GJtoktostr(struct tokens *tok,char delim,int s, int e)

/* 
   returns a string with the tokens between s and e inclusive written to 
   it separated by delim 
   the tok structure is unmodified.
*/

{
  int n, i, j,k;
  char *ret_val;

  n = 0;


  if(s < 0 || s >= tok->ntok) s = 0;
  if(e < 0 || e >= tok->ntok) e = tok->ntok - 1;

  for(i=s;i<=e;++i){
    n += strlen(tok->tok[i]);
    ++n;
  }

  ret_val = (char *) GJmalloc(sizeof(char) * n);
  j = 0;
  for(i=s;i<=e;++i){
    for(k=0;k<strlen(tok->tok[i]);++k){
      ret_val[j] = tok->tok[i][k];
      ++j;
    }
    ret_val[j++] = delim;
  }
  ret_val[n-1] = '\0';
  return ret_val;
}


void GJindexx(int *arrin,int n,int *indx)
  /* indexed heap sort - adapted from the NR routine indexx.
     inarr is an integer array to sort,
     indx is the returned index array
  */
{
        int l,j,ir,indxt,i;
/* SMJS
        float q;
*/
        int q;

        for (j=1;j<=n;j++) indx[j]=j;
        l=(n >> 1) + 1;
        ir=n;
        for (;;) {
                if (l > 1)
                        q=arrin[(indxt=indx[--l])];
                else {
                        q=arrin[(indxt=indx[ir])];
                        indx[ir]=indx[1];
                        if (--ir == 1) {
                                indx[1]=indxt;
                                return;
                        }
                }
                i=l;
                j=l << 1;
                while (j <= ir) {
                        if (j < ir && arrin[indx[j]] < arrin[indx[j+1]]) j++;
                        if (q < arrin[indx[j]]) {
                                indx[i]=indx[j];
                                j += (i=j);
                        }
                        else j=ir+1;
                }
                indx[i]=indxt;
        }
}

void GJindexxD(double *arrin,int n,int *indx)
  /* indexed heap sort - adapted from the NR routine indexx.
     arrin is a double array to sort,
     indx is the returned index array
  */
{
        int l,j,ir,indxt,i;
/*
        float q;
*/ 
        double q;

        for (j=1;j<=n;j++) indx[j]=j;
        l=(n >> 1) + 1;
        ir=n;
        for (;;) {
                if (l > 1)
                        q=arrin[(indxt=indx[--l])];
                else {
                        q=arrin[(indxt=indx[ir])];
                        indx[ir]=indx[1];
                        if (--ir == 1) {
                                indx[1]=indxt;
                                return;
                        }
                }
                i=l;
                j=l << 1;
                while (j <= ir) {
                        if (j < ir && arrin[indx[j]] < arrin[indx[j+1]]) j++;
                        if (q < arrin[indx[j]]) {
                                indx[i]=indx[j];
                                j += (i=j);
                        }
                        else j=ir+1;
                }
                indx[i]=indxt;
        }
}

void GJindexxS1(char **arrin,int n,int *indx)
     /*indexed sort of a character array - this uses qsort rather than the 
       heapsort in GJindexxS.  indx runs from 0..(n-1) rather than 1..n as in 
       GJindexxS.
     */
{
  int i;
  CWORK  *ret;

  ret = (CWORK *) GJmalloc(sizeof(CWORK) * n);

  for(i=0;i<n;++i){
    ret[i].val = arrin[i];
    ret[i].i = i;
  }
  qsort(ret,  n, sizeof(CWORK), Sworkcomp);

  for(i=0;i<n;++i){
    indx[i] = ret[i].i;
  }
  GJfree(ret);
}

int Sworkcomp(const void *left, const void *right)
{
  CWORK *a = (CWORK *) left;
  CWORK *b = (CWORK *) right;

  return strcmp(a->val,b->val);
}

  
void GJindexxS(char **arrin,int n,int *indx)
  /* indexed heap sort - adapted from the NR routine indexx.
     arrin is a character string array to sort
     indx is the returned index array
  */
{
        int l,j,ir,indxt,i;
/*
        float q;
*/
        char *q;

        for (j=1;j<=n;j++) indx[j]=j;
        l=(n >> 1) + 1;
        ir=n;
        for (;;) {
                if (l > 1)
                        q=arrin[(indxt=indx[--l])];
                else {
                        q=arrin[(indxt=indx[ir])];
                        indx[ir]=indx[1];
                        if (--ir == 1) {
                                indx[1]=indxt;
                                return;
                        }
                }
                i=l;
                j=l << 1;
                while (j <= ir) {
                        if (j < ir && (strcmp(arrin[indx[j]],arrin[indx[j+1]]) < 0)  ) j++;
                        if (strcmp(q,arrin[indx[j]])<0) {
                                indx[i]=indx[j];
                                j += (i=j);
                        }
                        else j=ir+1;
                }
                indx[i]=indxt;
        }
}
 


void GJpline(FILE *fp,char c,int n)
/* print n copies of c to fp terminated by newline */
{
  int i;
  for(i=0;i<n;++i){
    fprintf(fp,"%c",c);
  }
  fprintf(fp,"\n");
}

char *GJlocaltime(void)
{
  time_t t;
  char *ret_val;
  t = time(NULL);
  ret_val=asctime(localtime(&t));
  return ret_val;
}

void GJpstring(FILE *fp,char *s,int n)
/* print a string so that it occupies n characters */
/* strings are left-justified and either truncated or padded on output */
{
  int i,l;


  l = strlen(s);

  if(l > n){
    for(i=0;i<n;++i){
      fprintf(fp,"%c",s[i]);
    }
  }else{
    for(i=0;i<l;++i){
      fprintf(fp,"%c",s[i]);
    }
    for(i=l;i<n;++i){
      fprintf(fp," ");
    }
  }
}

/* return min and max of an integer vector */
IRANGE *irange(int *ivec,int n)
{
  int i;
  int imin,imax;
  IRANGE *ret_val;

  imin = imax = ivec[0];
  
  for(i=1;i<n;++i){
    if(ivec[i] > imax) imax = ivec[i];
    if(ivec[i] < imin) imin = ivec[i];
  }
  
  ret_val = (IRANGE *) GJmalloc(sizeof(IRANGE));
  ret_val->min = imin;
  ret_val->max = imax;

  return ret_val;
}

#define BLIM 5
int GJbsearchINXS(char **cod, int n, char *query)

/* binary search for query  in table cod.   If query is found, return index of query in cod.*/
/* if it is   not found, return -1 */

{
	int r;		/* right limit */
	int l;		/* left limit */
	int cv;		/* centre value */

	r = n-1;
	l = 0;

	for(;;){
		if((r-l) > BLIM){
			cv = (r+l)/2;
			if(strcmp(query,cod[cv]) == 0){
				return cv;
			}else if(strcmp(query,cod[cv]) > 0){
				l = cv;
			}else if(strcmp(query,cod[cv]) < 0){
				r = cv;
			}
		}else{
			for(cv=l;cv<(r+1);++cv){
			        if(strcmp(query,cod[cv]) == 0){			    
					return cv;
				}
			}
			return (int) -1;
		}
	}
}

int GJbsearchINX_IS(char **cod, int *inx, int n, char *query)

/* binary search for query  in table cod.  inx is the index into the table that specifies
   the sorted order of the table cod.
   If query is found, return index of query in inx that can be used to recover the value of 
   cod .*/
/* if it is   not found, return -1 */

{
	int r;		/* right limit */
	int l;		/* left limit */
	int cv;		/* centre value */

	r = n-1;
	l = 0;

	for(;;){
		if((r-l) > BLIM){
			cv = (r+l)/2;
			if(strcmp(query,cod[inx[cv]]) == 0){
				return cv;
			}else if(strcmp(query,cod[inx[cv]]) > 0){
				l = cv;
			}else if(strcmp(query,cod[inx[cv]]) < 0){
				r = cv;
			}
		}else{
			for(cv=l;cv<(r+1);++cv){
			        if(strcmp(query,cod[inx[cv]]) == 0){			    
					return cv;
				}
			}
			return (int) -1;
		}
	}
}

/* 

$Id: gjutil.c,v 1.13 2002/08/09 12:30:31 geoff Exp $ 

$Log: gjutil.c,v $
Revision 1.13  2002/08/09 12:30:31  geoff
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

Revision 1.12  2000/12/21 17:25:44  geoff
Add the option to output the sequence fragments from the multiple alignment
output option in fasta or pir format.  Unlike the block file output, these
fragments contain the complete sequence between the start and end points, including
any parts deleted in the alignment process.
Add appropriate commands to scanps_alias.dat, getpars and getcmd.

Revision 1.11  2000/07/04 11:01:37  searle
Changes for MMX

Revision 1.10  1999/11/17 21:06:47  geoff
Add setup_caleb and other changes to swpp2 and so on.

Revision 1.9  1999/07/09 13:34:10  geoff
modified these as a test

Revision 1.8  1998/08/11 15:38:50  geoff
Modified the copyright notice to reflect the new
ownership of this software.

Revision 1.7  1997/06/29 00:43:57  gjb
Changes to add sysinfo calls and test of license reading routines

Revision 1.6  1997/06/27 16:42:41  gjb
Add trlic.c test.lic and clean up gjutil.c

Revision 1.5  1997/06/27 07:17:31  gjb
Added rlic.c linfo.h and
changes to gjutil.c to give better support for
token manipulation

Revision 1.4  1997/05/12 11:10:53  gjb
Re-added gjutil.c and gjutil.h to repository
after deleting them

Revision 1.2  1997/05/12 10:47:52  gjb
Modified CVS header and log position

*/




