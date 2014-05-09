#!/bin/bash

KEYSTORE=keyStore
ALIAS=signedApplet
NAME=GeoCentral
PASSWORD=screwswine
CERTIFICATE=GeoCentral.cer
SOURCEDIR=dist/lib
TARGETDIR=signed
PREFIX=signed-

keytool -genkey -alias $ALIAS -keystore $KEYSTORE -keypass $PASSWORD -dname "cn=$NAME" -storepass $PASSWORD

rm -rf $TARGETDIR
mkdir $TARGETDIR
for file in `dir $SOURCEDIR`
do
    jarsigner -keystore $KEYSTORE -storepass $PASSWORD -keypass $PASSWORD -signedjar $TARGETDIR/$PREFIX$file $SOURCEDIR/$file $ALIAS
done

keytool -export -keystore $KEYSTORE -storepass $PASSWORD -alias $ALIAS -file $TARGETDIR/$CERTIFICATE


