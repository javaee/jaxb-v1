@echo off

rem $Id: nxjc.bat,v 1.2 2005-09-10 18:19:39 kohsuke Exp $^M

REM
REM The contents of this file are subject to the terms
REM of the Common Development and Distribution License
REM (the "License").  You may not use this file except
REM in compliance with the License.
REM 
REM You can obtain a copy of the license at
REM https://jwsdp.dev.java.net/CDDLv1.0.html
REM See the License for the specific language governing
REM permissions and limitations under the License.
REM 
REM When distributing Covered Code, include this CDDL
REM HEADER in each file and include the License file at
REM https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
REM add the following below this CDDL HEADER, with the
REM fields enclosed by brackets "[]" replaced with your
REM own identifying information: Portions Copyright [yyyy]
REM [name of copyright owner]
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
