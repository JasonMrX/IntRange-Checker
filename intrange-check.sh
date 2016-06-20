#!/bin/bash

WORKING_DIR=$(pwd)
ROOT=$(cd $(dirname "$0")/.. && pwd)
JAVAC=$ROOT/checker-framework/checker/bin-devel/javac

INTRANGE_CHECKER=$ROOT/IntRange-Checker

cd $WORKING_DIR

java_files=$1
shift
while [ $# -gt 0 ]
do
    java_files="$java_files $1"
    shift
done

$JAVAC -processor intrange.IntRangeChecker -cp $INTRANGE_CHECKER/bin:$INTRANGE_CHECKER/lib $java_files
