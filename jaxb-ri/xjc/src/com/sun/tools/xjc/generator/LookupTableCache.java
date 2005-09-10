/*
 * @(#)$Id: LookupTableCache.java,v 1.2 2005-09-10 18:20:02 kohsuke Exp $
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
