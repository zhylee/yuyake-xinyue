:: local variables

set protoVersion=3
set filePath=".\file"

:: server proto path

::set serverPackage=cn.yuyake
::set targetPath=null

::@echo start copy files
::java -jar proto-generate.jar %protoVersion% %filePath% %serverPackage%

@echo off

:: java generate
set targetPath=".."

cd %targetPath%

@echo off
@echo start generate files

del .\src\main\java\cn\yuyake\game\message\body\*.java

for /r "protobuf/file" %%i in (*.proto) do (
echo %%~nxi
protobuf\protoc.exe  -I=./protobuf/file/ --java_out=./src/main/java/ %%~nxi
)


IF ERRORLEVEL 1 goto exeFail
IF ERRORLEVEL 0 goto exeSuccess

:exeFail
echo generate files failed
:exeSuccess
echo generate files succeeded



