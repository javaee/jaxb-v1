#!/bin/sh

#
# Copyright 2004 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

find . -name "*.java" | \
xargs grep "3RD PARTY CODE"
