/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.grammar.xducer.Transducer;

/**
 * conversion declaration (&lt;conversion> and &lt;enumeration>).
 */
public interface BIConversion
{
    /** Gets the conversion name. */
    String name();
    
    /** Gets a transducer for this conversion. */
    Transducer getTransducer();
}
