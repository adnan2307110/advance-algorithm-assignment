@echo off
set "DIR=%~dp0"
if exist "%DIR%maven-dist\apache-maven-3.9.9\bin\mvn.cmd" (
    "%DIR%maven-dist\apache-maven-3.9.9\bin\mvn.cmd" %*
) else (
    mvn %*
)
