@echo off

rem $Id: nxjc.bat,v 1.1 2004-06-25 21:13:35 kohsuke Exp $^M

REM
REM Copyright 2004 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

rem Configure environment
rem =====================
if %JAXB_HOME%x==x goto warnNoJAXB_HOME
if not exist %JAXB_HOME%\lib\jaxb-xjc.jar goto warnJarNotFound
set buildfile=%JAXB_HOME%\etc\build.xml
set ant=%JAXB_HOME%\bin\ant.bat

rem Capture options
rem ===============
set options=
:addOption
if %1x==x goto endOptions
set options=%options% %1
shift
goto addOption
:endOptions

rem Set pwd
rem =======
echo @prompt set pwd$q$p> tmp$.bat
%comspec% /c tmp$.bat> tmp$$.bat
call tmp$$.bat
if exist tmp$.bat del tmp$.bat
if exist tmp$$.bat del tmp$$.bat

rem Call ant
rem ========
call %ant% -buildfile %buildfile% -emacs -Dant.home="%JAXB_HOME%" -Djava.class.path="" -Dbasedir=%pwd% -Dxjc.opts="%options%"
goto end

:warnNoJAXB_HOME
echo.
echo Please set JAXB_HOME.
echo.
goto end

:warnJarNotFound
echo.
echo Can't find %%JAXB_HOME%%\lib\jaxb-xjc.jar.
echo JAXB_HOME may not be set properly.
echo.

:end
exit /b %ERRORLEVEL%
