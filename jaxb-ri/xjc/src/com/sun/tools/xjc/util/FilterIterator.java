/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
