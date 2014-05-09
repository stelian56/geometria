REM Sign Geometria binaries

SET SOURCE_DIR=%1%
SET TARGET_DIR=%2%

SET KEYSTORE=keyStore
SET ALIAS=signedApplet
SET NAME=GeoCentral
SET PASSWORD=screwswine
SET CERTIFICATE=GeoCentral.cer
SET PREFIX=signed-

keytool -genkey -alias %ALIAS% -keystore %KEYSTORE% -keypass %PASSWORD% -dname "cn=%NAME%" -storepass %PASSWORD%

rmdir /S /Q %TARGET_DIR%
mkdir %TARGET_DIR%
SET WORK_DIR=%CD%
cd %SOURCE_DIR%
for %%a in (*) do (
    cd %WORK_DIR%
    jarsigner -keystore %KEYSTORE% -storepass %PASSWORD% -keypass %PASSWORD% -signedjar %TARGET_DIR%\%PREFIX%%%a %SOURCE_DIR%\%%a %ALIAS%
)

keytool -export -keystore %KEYSTORE% -storepass %PASSWORD% -alias %ALIAS% -file %TARGET_DIR%/%CERTIFICATE%


