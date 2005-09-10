/*
 * @(#)$Id: LookupTableUse.java,v 1.2 2005-09-10 18:20:02 kohsuke Exp $
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
