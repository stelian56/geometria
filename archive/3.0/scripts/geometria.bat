@echo off

rem Geometria Startup Script

set BASE_DIR=.
set LIB_DIR=%BASE_DIR%\lib

set CP=.
set CP=%CP%;%LIB_DIR%\geometria-3.0.jar
set CP=%CP%;%LIB_DIR%\jhall.jar
set CP=%CP%;%LIB_DIR%\log4j-1.2.14.jar
set CP=%CP%;%LIB_DIR%\vecmath.jar

@echo on

javaw -cp %CP% net.geocentral.geometria.view.GFrame
