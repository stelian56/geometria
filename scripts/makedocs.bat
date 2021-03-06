@echo off & setlocal EnableDelayedExpansion
REM Build Geometria documentation

SET DOC_DIR=%1%
SET DOC_BUILD_DIR=%2%

rmdir /S /Q %DOC_BUILD_DIR%
SET WORK_DIR=%CD%
cd %DOC_DIR%

for /d %%a in (*) do (
    cd %WORK_DIR%
    echo "Building documentation for: %%a"
    mkdir %DOC_BUILD_DIR%\%%a
    xsltproc ^
	--stringparam html.stylesheet ../../css/doc.css ^
	--stringparam generate.toc "book toc" ^
    --stringparam section.autolabel "1" ^
	--stringparam ulink.target "_blank" ^
	-o %DOC_BUILD_DIR%\%%a\UsersGuide.html "%DOCBOOK_HOME%\html\docbook.xsl" %DOC_DIR%\%%a\UsersGuide.xml
)
