@echo off & setlocal EnableDelayedExpansion
REM Build Geometria help files

SET DOCBOOK_DIR=C:\Program Files\docbook-xsl-1.78.0
SET JHSEARCH=C:\jh2.0\javahelp\bin\jhindexer.bat

SET DOC_DIR=%1%
SET JAVAHELP_DIR=%2%
SET JAVAHELP_BUILD_DIR=%3%

rmdir /S /Q %JAVAHELP_BUILD_DIR%
SET WORK_DIR=%CD%
cd %DOC_DIR%

for /d %%a in (*) do (
    cd %WORK_DIR%
    echo "Building help files for: %%a"
    mkdir %JAVAHELP_BUILD_DIR%\%%a
    xsltproc --stringparam base.dir %JAVAHELP_BUILD_DIR%\%%a\ --stringparam use.id.as.filename 1 --stringparam javahelp.encoding UTF-8 "%DOCBOOK_DIR%\javahelp\javahelp.xsl" %DOC_DIR%\%%a\UsersGuide.xml
    cd %WORK_DIR%\%JAVAHELP_BUILD_DIR%\%%a
    "%JHSEARCH%" .
    cd %WORK_DIR%
    copy %JAVAHELP_DIR%\* %JAVAHELP_BUILD_DIR%\%%a
)
