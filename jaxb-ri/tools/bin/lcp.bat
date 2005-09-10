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

REM   Copyright (c) 2001 The Apache Software Foundation.  All rights
REM   reserved.

set _CLASSPATHCOMPONENT=%1
:argCheck
if %2a==a goto gotAllArgs
shift
set _CLASSPATHCOMPONENT=%_CLASSPATHCOMPONENT% %1
goto argCheck
:gotAllArgs
set LOCALCLASSPATH=%_CLASSPATHCOMPONENT%;%LOCALCLASSPATH%

