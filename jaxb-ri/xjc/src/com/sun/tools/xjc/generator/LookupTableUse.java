/*
 * @(#)$Id: LookupTableUse.java,v 1.1 2004-06-25 21:14:14 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.SimpleNameClass;

/**
 * Information about use of a {@link LookupTable}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class LookupTableUse {
    /**
     * Table information that governs the mapping from
     * the switch attribute vlaue to {@link com.sun.tools.xjc.grammar.ClassItem}s.
     */
    public final LookupTable table;
    
    /**
     * A branch of a choice. If the look up fails, this branch should be taken.
     * Null if the switch attribute look up is always expected to work.
     */
    public final Expression anomaly;
    
    /**
     * Name of the switch attribute.
     */
    public final SimpleNameClass switchAttName;
    
    LookupTableUse(LookupTable _table, Expression _anomaly, SimpleNameClass _switchAttName) {
        this.table = _table;
        this.anomaly = _anomaly;
        this.switchAttName = _switchAttName;
    }
}
