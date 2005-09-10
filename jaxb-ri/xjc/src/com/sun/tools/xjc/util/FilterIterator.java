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

package com.sun.tools.xjc.util;

import java.util.Iterator;

public abstract class FilterIterator implements Iterator
{
    /**
     * @return
     *      return false to hide this object from the user.
     */
    protected abstract boolean test( Object o );
    
    private final Iterator core;
    
    public FilterIterator( Iterator _core ) { this.core=_core; }
    
    private boolean ready = false;
    private boolean noMore = false;
    private Object obj = null;
    
    public final Object next() {
        if(!hasNext())  throw new IllegalStateException("no more object");
        ready=false;
        return obj;
    }
    
    public final boolean hasNext() {
        if(noMore)      return false;
        if(ready)       return true;
        
        while(core.hasNext()) {
            Object o = core.next();
            if(test(o)) {
                obj=o;
                ready=true;
                return true;
            }
        }
        noMore=true;
        return false;
    }
    
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
