/*
 * @(#)$Id: LookupTableInterner.java,v 1.1 2004-06-25 21:14:14 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import java.util.ArrayList;
import java.util.List;

import com.sun.msv.grammar.ChoiceExp;

/**
 * Wraps another {@link LookupTableBuilder} and 
 * reduces the total number of tables by
 * merging tables as much as possible.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class LookupTableInterner implements LookupTableBuilder {
    
    /**
     * List of active {@link LookupTable}s. 
     */
    private final List liveTable = new ArrayList();
    
    /**
     * Wrapped builder.
     */
    private final LookupTableBuilder core;
    
    
    public LookupTableInterner(LookupTableBuilder _core) {
        this.core = _core;
    }
    
    public LookupTableUse buildTable( ChoiceExp exp ) {
        LookupTableUse t = core.buildTable(exp);
        if(t==null) return null;
        
        return new LookupTableUse( intern(t.table), t.anomaly, t.switchAttName );
    }
    
    private LookupTable intern( LookupTable t ) {
        // Checks if the specified table can be merged with any of the
        // existing tables. If so, do a merge and return the merged table.  
        for( int i=0; i<liveTable.size(); i++ ) {
            LookupTable a = (LookupTable)liveTable.get(i);
            if(a.isConsistentWith(t)) {
                a.absorb(t);
                return a;
            }
        }
        // no table is consistent with this new table. start using this new table
        liveTable.add(t);
        return t;
    }
    
    /**
     * List all the memoized tables.
     */
    public LookupTable[] listTables() {
        return (LookupTable[]) liveTable.toArray(new LookupTable[liveTable.size()]);
    }
}
