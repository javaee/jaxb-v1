/*
 * @(#)$Id: LookupTableCache.java,v 1.1 2004-06-25 21:14:14 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import java.util.HashMap;
import java.util.Map;

import com.sun.msv.grammar.ChoiceExp;

/**
 * Wraps another {@link LookupTableBuilder} and caches
 * the return value, so that the multiple invocations of
 * the buildTable method runs faster.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class LookupTableCache implements LookupTableBuilder {
    
    private final Map cache = new HashMap();
    private final LookupTableBuilder core;
    
    public LookupTableCache(LookupTableBuilder _core) {
        this.core = _core;
    }
    
    public LookupTableUse buildTable(ChoiceExp exp) {
        if( cache.containsKey(exp) ) {
            return (LookupTableUse)cache.get(exp);
        } else {
            LookupTableUse t = core.buildTable(exp);
            cache.put(exp,t);
            return t;
        }
    }
}
