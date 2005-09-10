package com.sun.timer;

import java.math.BigInteger;

/*
 * @(#)$Id: HighResTimer.java,v 1.2 2005-09-10 18:19:40 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/**
 * Uses a Sun internal API.
 * 
 * Works with JDK 1.4.2.
 * See <a href="http://www.javaperformancetuning.com/articles/soundtimer2.shtml">
 * this article</a> for details.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class HighResTimer extends Timer {
    
    private static final sun.misc.Perf p;
    
    /**
     * a positive value if the value of p.highResCounter() can be divided
     * by a long value to the accurate nano-seconds representation.
     * -1 if otherwise.
     */
    private static final long divider;
    
    /** A constant that represents 10^9. */
    private static final BigInteger bi1000_1000_1000 = BigInteger.valueOf(1000*1000*1000);
    /** If {@link #divider} is -1, this value will be non-null  */
    private static final BigInteger biDivider;
    
    
    static {
        p = sun.misc.Perf.getPerf();
        long d = 1000*1000*1000/p.highResFrequency();
        if( 1000L*1000L*1000L==d*p.highResFrequency() ) {
            divider = d;
            biDivider = null;
        } else {
            divider = -1;
            biDivider = BigInteger.valueOf(p.highResFrequency());
        }
    }
    
    public long nanoTime() {
        if( divider!=-1 )
            return p.highResCounter()/divider;
        else
            return BigInteger.valueOf(p.highResCounter()).multiply(bi1000_1000_1000).divide(biDivider).longValue();
    }

    public String name() {
        return "JDK1.4.2 sun.misc.Perf";
    }
}
