#!/usr/bin/python
# Copyright (C) 2004 Rune Linding & Lars Juhl Jensen - EMBL
# The DisEMBL is licensed under the GPL license
# (http://www.opensource.org/licenses/gpl-license.php)
# DisEMBL pipeline


from string import *
import sys,re
from sys import argv
import os
from os import system,popen3

relpath = re.sub("/DisEMBL.py$","",argv[0])
# cwd = re.sub("/$","", os.getcwd())
newpath = relpath + "/biopython-1.50"
sys.path.append(newpath)

from Bio import File
from Bio import Fasta

import fpformat
import tempfile


# change these to the correct paths
NN_bin = relpath + "/" + 'disembl'
SG_bin = relpath + "/" + 'sav_gol'

def JensenNet(sequence):
    outFile = tempfile.mktemp()
    inFile= tempfile.mktemp()
    open(inFile,'w').write(sequence+'\n')
    system(NN_bin + '< ' + inFile +' > ' + outFile)
    REM465 = []
    COILS = []
    HOTLOOPS = []
    resultsFile = open(outFile,'r')
    results = resultsFile.readlines()
    resultsFile.close()
    for result in results:
        coil = float(fpformat.fix(split(result)[0],6))
        COILS.append(coil)
        hotloop = float(fpformat.fix(split(result)[1],6))
        HOTLOOPS.append(hotloop)
        rem465 = float(fpformat.fix(split(result)[2],6))
        REM465.append(rem465)
    os.remove(inFile)
    os.remove(outFile)
    return COILS, HOTLOOPS, REM465


def SavitzkyGolay(window,derivative,datalist):
    if len(datalist) < 2*window:
        window = len(datalist)/2
    elif window == 0:
        window = 1
    stdin, stdout, stderr = popen3(SG_bin + ' -V0 -D' + str(derivative) + ' -n' + str(window)+','+str(window))
    for data in datalist:
        stdin.write(`data`+'\n')
    try:
        stdin.close()
    except:
        print stderr.readlines()
    results = stdout.readlines()
    stdout.close()
    SG_results = []
    for result in results:
        f = float(fpformat.fix(result,6))
        if f < 0:
            SG_results.append(0)
        else:
            SG_results.append(f)
    return SG_results

def getSlices(NNdata, fold, join_frame, peak_frame, expect_val):
    slices = []
    inSlice = 0
    for i in range(len(NNdata)):
        if inSlice:
            if NNdata[i] < expect_val:
                if maxSlice >= fold*expect_val:
                    slices.append([beginSlice, endSlice])
                inSlice = 0
            else:
                endSlice += 1
                if NNdata[i] > maxSlice:
                    maxSlice = NNdata[i]
        elif NNdata[i] >= expect_val:
            beginSlice = i
            endSlice = i
            inSlice = 1
            maxSlice = NNdata[i]
    if inSlice and maxSlice >= fold*expect_val:
        slices.append([beginSlice, endSlice])

    i = 0
    while i < len(slices):
        if i+1 < len(slices) and slices[i+1][0]-slices[i][1] <= join_frame:
            slices[i] = [ slices[i][0], slices[i+1][1] ]
            del slices[i+1]
        elif slices[i][1]-slices[i][0]+1 < peak_frame:
            del slices[i]
        else:
            i += 1
    return slices


def reportSlicesTXT(slices, sequence):
    if slices == []:
        s = lower(sequence)
    else:
        if slices[0][0] > 0:
            s = lower(sequence[0:slices[0][0]])
        else:
            s = ''
        for i in range(len(slices)):
            if i > 0:
                sys.stdout.write(', ')
            sys.stdout.write( str(slices[i][0]+1) + '-' + str(slices[i][1]+1) )
           
    print ''



def runDisEMBLpipeline():
    try:
        smooth_frame = 8
        peak_frame = 8
        join_frame = 4
        fold_coils = 1.2
        fold_hotloops = 1.4
        fold_rem465 = 1.2
        mode = 'scores'
        try:
            file = open(sys.argv[1],'r')
        except:
            mode = 'default'
    except:
        print '\nDisEMBL.py sequence_file \n'
        print 'A default run would be: ./DisEMBL.py fasta_file'
        raise SystemExit
    #db = sys.stdin
    parser = Fasta.RecordParser()
    iterator = Fasta.Iterator(file,parser)
    while 1:
        try:
            cur_record = iterator.next()
            sequence = upper(cur_record.sequence)
            # Run NN
            COILS_raw, HOTLOOPS_raw, REM465_raw = JensenNet(sequence)
            # Run Savitzky-Golay
            REM465_smooth = SavitzkyGolay(smooth_frame,0,REM465_raw)
            COILS_smooth = SavitzkyGolay(smooth_frame,0,COILS_raw)
            HOTLOOPS_smooth = SavitzkyGolay(smooth_frame,0,HOTLOOPS_raw)

            sys.stdout.write('> '+cur_record.title+'\n')
            sys.stdout.write('# COILS ')
            reportSlicesTXT( getSlices(COILS_smooth, fold_coils, join_frame, peak_frame, 0.43), sequence )
            sys.stdout.write('# REM465 ')
            reportSlicesTXT( getSlices(REM465_smooth, fold_rem465, join_frame, peak_frame, 0.50), sequence )
            sys.stdout.write('# HOTLOOPS ')
            reportSlicesTXT( getSlices(HOTLOOPS_smooth, fold_hotloops, join_frame, peak_frame, 0.086), sequence )
            sys.stdout.write('# RESIDUE COILS REM465 HOTLOOPS\n')
            for i in range(len(REM465_smooth)):
                sys.stdout.write(sequence[i]+'\t'+fpformat.fix(COILS_smooth[i],5)+'\t'+fpformat.fix(REM465_smooth[i],5)+'\t'+fpformat.fix(HOTLOOPS_smooth[i],5)+'\n')
        except AttributeError:
            break
    file.close()
    return

runDisEMBLpipeline()
