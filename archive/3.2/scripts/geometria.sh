#!/bin/bash
# Geometria Startup Script

DOCUMENT=$1

CP=.
CP=$CP:lib/*

java -cp $CP net.geocentral.geometria.view.GFrame $DOCUMENT
