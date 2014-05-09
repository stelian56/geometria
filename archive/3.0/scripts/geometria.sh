#!/bin/bash
# Geometria Startup Script

BASE_DIR=.
LIB_DIR=$BASE_DIR/lib

CP=.
for jarfile in `find $LIB_DIR -name '*.jar'`
	do
		CP=$CP:$jarfile
	done 

java -cp $CP net.geocentral.geometria.view.GFrame
