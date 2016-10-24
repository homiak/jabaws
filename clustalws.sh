#!/bin/bash

#start..end..increment
for i in {1..15}
do
   echo "Welcome $i times"
   java -cp /homes/pvtroshin/workspace/clustengine/WEB-INF/lib/*:/homes/pvtroshin/workspace/clustengine/WEB-INF/classes compbio.ws.client.SimpleWSClient http://wsdev.compbio.dundee.ac.uk:8083/jws2 MuscleWS /homes/pvtroshin/workspace/clustengine/testsrc/testdata/TO1381.fasta &
done 



#/fc_gpfs/gjb_lab/www-refine/bin/blast_32bit/blast-2.2.17/bin/blastall -p blastp -i $input -d $dbnam -e $evalue -m 9 -o $outfile
#/fc_gpfs/gjb_lab/www-refine/bin/blast_32bit/blast-2.2.17/bin/rpsblast -i $input -d /db/CDD/Cdd -p T -o $outfile
