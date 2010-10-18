/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
