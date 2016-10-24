/* -*- mode: c; tab-width: 4; c-basic-offset: 4;  indent-tabs-mode: nil -*- */

/*********************************************************************
 * Clustal Omega - Multiple sequence alignment
 *
 * Copyright (C) 2010 University College Dublin
 *
 * Clustal-Omega is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This file is part of Clustal-Omega.
 *
 ********************************************************************/

/*
 *  RCS $Id: clustalo-api-test.c 213 2011-03-11 16:10:15Z andreas $
 */


#include <stdio.h>

/* Include clustal-omega's header. That's all you need 
 *
 * If you developing in C++, use the following instead:
 * extern "C" {
 * #include "clustal-omega.h"
 * }
 */
#include "clustal-omega.h"


int
main(int argc, char **argv)
{
    /* the multiple sequence structure */
    mseq_t *prMSeq = NULL;
    /* for openmp: number of threads to use */
    int iThreads = 1;
    /* alignment options to use */
    opts_t rAlnOpts;
    /* an input file */
    char *pcSeqInfile;
    int iAux;

    /* use LOGLEVEL_QUIET to make Clustal shut up */
    iVerbosityLevel = LOGLEVEL_INFO;

    SetDefaultAlnOpts(&rAlnOpts);

    InitClustalOmega(iThreads);

    /* Get sequence input file name from command line
     */
    if (argc!=2) {
        Fatal("Need sequence file as argument");
    }
    pcSeqInfile = argv[1];

    /* Read sequence file
     */
    NewMSeq(&prMSeq);
    if (ReadSequences(prMSeq, pcSeqInfile,
                      SEQTYPE_UNKNOWN,
                      INT_MAX, INT_MAX)) {
        Fatal("Reading sequence file '%s' failed", pcSeqInfile);
    }

    /* Dump some info about the sequences
     */
    for (iAux=0; iAux<prMSeq->nseqs; iAux++) {
        Info(LOGLEVEL_INFO, 
             "Sequence no %d has the following name: %s",
             iAux, prMSeq->sqinfo[iAux].name);
        Info(LOGLEVEL_INFO, 
             "Sequence no %d has the following residues: %s",
             iAux, prMSeq->seq[iAux]);
        /* more info can be found in prMSeq->sqinfo[iAux] */
    }


    /* Align the sequences without a profile (NULL)
     */
    if (Align(prMSeq, NULL, & rAlnOpts)) {
        Fatal("A fatal error happended during the alignment process");
    }


    /* Output of final alignment to stdout (NULL) as aligned fasta/a2m
     */
    if (WriteAlignment(prMSeq, NULL, MSAFILE_A2M)) {
        Fatal("Could not save alignment");
    } 

    FreeMSeq(&prMSeq);

    Info(LOGLEVEL_INFO, "Successfull program exit");

    return EXIT_SUCCESS;
}
/***   end of main()   ***/
