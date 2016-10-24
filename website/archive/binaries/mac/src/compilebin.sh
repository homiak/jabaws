#!/bin/bash

echo ""
echo "Compiling Clustalw..."
cd clustalw
chmod +x ./configure 
./configure
make clean 
make
chmod +x src/clustalw2 
cd ..

echo "Compiling Clustal Omega..."
cd clustalo
make clean
chmod +x ./configure 
./configure
make
cd ..

echo "Compiling Mafft..."
cd mafft/core
make clean
make
cd ../..

echo "Compiling Mafft dependency fasta34..."
cd fasta34
rm *.o
make 
chmod +x fasta34
cd ..


echo "Compiling Muscle..."
cd muscle
rm -f *.o muscle
make
cd ..

echo "Compiling Probcons..."
cd probcons
make clean 
make
chmod +x probcons
cd ..

echo "Compiling T-Coffee..."
cd tcoffee
chmod +x install
./install clean
./install t_coffee -force
chmod +x t_coffee_source/t_coffee
cd ..

echo "Compiling DisEMBL..."
cd disembl
gcc -O3 disembl.c -o disembl
echo "DONE"
chmod +x disembl DisEMBL.py

echo "Compiling DisEMBL dependancy Tisean... "
cd Tisean_3.0.1
chmod +x ./configure
./configure
make
cp source_c/sav_gol ../
cd ..
echo "DONE"
chmod +x sav_gol
cd ..

echo "Setting up GlobPlot ..."
cp disembl/sav_gol globplot/sav_gol
cd globplot
chmod +x GlobPlot.py
echo "DONE"
cd ..

echo "Compiling IUPred..."
cd iupred
make clean
make 
echo "DONE"
cd ..

