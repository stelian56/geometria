@echo off

rem Geometria Startup Script

set DOCUMENT=%1%

set CP=.
set CP=%CP%;lib\*

@echo on

java -Dlog4j.configuration=log4jfile.properties -cp %CP% net.geocentral.geometria.view.GFrame %DOCUMENT%
