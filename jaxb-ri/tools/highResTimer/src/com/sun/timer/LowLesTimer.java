package com.sun.timer;
/*
 * @(#)$Id: LowLesTimer.java,v 1.1 2004-06-25 21:13:38 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * Uses {@link System#currentTimeMillis()}. Low precision.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class LowLesTimer extends Timer {

    public long nanoTime() {
        return System.currentTimeMillis()*1000*1000;
    }

    public String name() {
        return "System.currentTimeMillis";
    }
}
