#!/bin/sh

# $Id: nxjc.sh,v 1.2 2005-09-10 18:19:39 kohsuke Exp $

#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the "License").  You may not use this file except
# in compliance with the License.
# 
# You can obtain a copy of the license at
# https://jwsdp.dev.java.net/CDDLv1.0.html
# See the License for the specific language governing
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL
# HEADER in each file and include the License file at
# https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
# add the following below this CDDL HEADER, with the
# fields enclosed by brackets "[]" replaced with your
# own identifying information: Portions Copyright [yyyy]
# [name of copyright owner]
#

[ -z "${JAXB_HOME}" ] &&
{
    echo
    echo "Please set JAXB_HOME."
    exit 1
}

[ -f ${JAXB_HOME}/lib/jaxb-xjc.jar ] ||
{
    echo
    echo "Can't find \${JAXB_HOME}/lib/jaxb-xjc.jar."
    echo "JAXB_HOME may not be set properly."
    exit 1
}

[ `expr \`uname\` : 'CYGWIN'` -eq 6 ] &&
{
    JAXB_HOME=`cygpath -w ${JAXB_HOME}`
    PWD=`cygpath -w ${PWD}`
}

BUILDFILE=${JAXB_HOME}/etc/build.xml

${JAXB_HOME}/bin/ant \
    -buildfile ${BUILDFILE}\
    -emacs \
    -Dant.home="${JAXB_HOME}"\
    -Djava.class.path=\
    -Dbasedir="${PWD}" \
    -Dxjc.opts="${*}" 


