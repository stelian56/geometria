#!/bin/bash
# Build HTML, Javahelp

JHSEARCH=/home/stelian/geometria/jh2.0/javahelp/bin/jhindexer

SOURCE_DIR=$1
DOC_DIR=$2
JAVAHELP_DIR=$3

STARTUP_DIR=`pwd`
DOCBOOK_DIR=/home/stelian/geometria/docbook-xsl-1.75.2
SOURCE_FILE=$SOURCE_DIR/UsersGuide.xml

mkdir -p $DOC_DIR
HTML_FILE=$DOC_DIR/UsersGuide.html
xsltproc \
        -o $HTML_FILE $DOCBOOK_DIR/html/docbook.xsl $SOURCE_FILE

mkdir -p $JAVAHELP_DIR
xsltproc \
    	--stringparam base.dir  $JAVAHELP_DIR/  \
	    --stringparam  use.id.as.filename  1 \
	    --stringparam  javahelp.encoding UTF-8 \
    	 $DOCBOOK_DIR/javahelp/javahelp.xsl $SOURCE_FILE
TEMP_FILE=$JAVAHELP_DIR/jhelpmap.jhm.tmp
#cat $JAVAHELP_DIR/jhelpmap.jhm | awk '{ sub(/target=\"GeometriaUsersGuide\" url=\"index.html\"/, "target=\"top\" url=\"Home.html\"")};1' > $TEMP_FILE
awk 'NR==4{print "  <mapID target=\"top\" url=\"Home.html\"/>"}1' $JAVAHELP_DIR/jhelpmap.jhm > $TEMP_FILE
mv $TEMP_FILE $JAVAHELP_DIR/jhelpmap.jhm
cd $JAVAHELP_DIR && $JHSEARCH .
cd $STARTUP_DIR

