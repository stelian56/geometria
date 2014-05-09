@echo off & setlocal EnableDelayedExpansion
REM Build Geometria documentation

SET DOCBOOK_DIR=C:\Program Files\docbook-xsl-1.78.0
SET JHSEARCH=C:\jh2.0\javahelp\bin\jhindexer.bat

SET DOC_DIR=%1%
SET DOC_BUILD_DIR=%2%

rmdir /S /Q %DOC_BUILD_DIR%
SET WORK_DIR=%CD%
cd %DOC_DIR%

for /d %%a in (*) do (
    cd %WORK_DIR%
    echo "Building documentation for: %%a"
    mkdir %DOC_BUILD_DIR%\%%a
    xsltproc --stringparam html.stylesheet ../../css/geometria.css -o %DOC_BUILD_DIR%\%%a\UsersGuide.html "%DOCBOOK_DIR%\html\docbook.xsl" %DOC_DIR%\%%a\UsersGuide.xml
)
