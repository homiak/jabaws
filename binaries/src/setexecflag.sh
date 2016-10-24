#!/bin/sh

echo ""
echo "Setting executable flag for Clustalw..."
chmod +x clustalw/src/clustalw2 

echo "Setting executable flag for Clustal Omega..."
chmod +x clustalo/src/clustalo 

echo "Setting executable flag for Mafft..."
chmod +x mafft/binaries/* 
chmod +x mafft/scripts/*

echo "Setting executable flag for Mafft dependency fasta34 ..."
chmod +x fasta34/fasta34 

echo "Setting executable flag for Muscle..."
chmod +x muscle/muscle

echo "Setting executable flag for Probcons..."
chmod +x probcons/probcons

echo "Setting executable flag for T-Coffee..."
chmod +x tcoffee/t_coffee_source/t_coffee

echo "Setting executable flag for DisEMBL..."
chmod +x disembl/disembl disembl/sav_gol disembl/DisEMBL.py 

echo "Setting executable flag for GlobPlot..."
chmod +x globplot/GlobPlot.py globplot/sav_gol

echo "Setting executable flag for IUPred..."
chmod +x iupred/iupred
