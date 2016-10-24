#!/bin/bash

#JAVA=/sw/java/latest/bin/java; export JAVA;
JAVA_HOME=/sw/java/latest; export JAVA_HOME;
ANT_HOME=/sw/java/ant; export ANT_HOME;
ANT=/sw/java/ant/bin/ant; export ANT;

# Re-Run tests run which was last run in IDE 
#$ANT -f build.xml CustomTest

# Excludes cluster dependent and non-windows machine tests, 
# so can be safely run from the development environment 
#$ANT -f build.xml All_cluster_independent_windows_only_tests

# For re-running failed tests
#$ANT -f build.xml Rerun_failed_tests

# For running cluster dependent tests
#$ANT -f build.xml Run_cluster_dependent_test

# For running ALL tests
$ANT -f build.xml Test