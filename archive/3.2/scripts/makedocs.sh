#!/bin/bash
# Build HTML, Javahelp

JHSEARCH=/opt/jh2.0/javahelp/bin/jhindexer

DOC_DIR=$1
DOC_BUILD_DIR=$2
JAVAHELP_DIR=$3
JAVAHELP_BUILD_DIR=$4

STARTUP_DIR=`pwd`
DOCBOOK_DIR=/opt/docbook-xsl-1.77.0

cd $DOC_DIR
langs=`ls -d1 *`
cd $STARTUP_DIR

echo "Building documentation for:" $langs

rm -rf $DOC_BUILD_DIR $JAVAHELP_BUILD_DIR

for lang in $langs; do

    SOURCE_FILE=$DOC_DIR/$lang/UsersGuide.xml
    mkdir -p $DOC_BUILD_DIR/$lang
    mkdir -p $JAVAHELP_BUILD_DIR/$lang
    HTML_FILE=$DOC_BUILD_DIR/$lang/UsersGuide.html
    xsltproc \
        -o $HTML_FILE $DOCBOOK_DIR/html/docbook.xsl $SOURCE_FILE
    xsltproc \
    	--stringparam base.dir  $JAVAHELP_BUILD_DIR/$lang/  \
	    --stringparam  use.id.as.filename  1 \
	    --stringparam  javahelp.encoding UTF-8 \
    	 $DOCBOOK_DIR/javahelp/javahelp.xsl $SOURCE_FILE
    TEMP_FILE=$JAVAHELP_BUILD_DIR/$lang/jhelpmap.jhm.tmp
    awk \
        'NR==4{print "  <mapID target=\"top\" url=\"Home.html\"/>"}1' \
        $JAVAHELP_BUILD_DIR/$lang/jhelpmap.jhm > $TEMP_FILE
    mv $TEMP_FILE $JAVAHELP_BUILD_DIR/$lang/jhelpmap.jhm
    cp $JAVAHELP_DIR/* $JAVAHELP_BUILD_DIR/$lang
    cd $JAVAHELP_BUILD_DIR/$lang
    $JHSEARCH .
    cd $STARTUP_DIR
    
done



