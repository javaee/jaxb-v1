#!/bin/sh

# $Id: nxjc.sh,v 1.1 2004-06-25 21:13:35 kohsuke Exp $

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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


