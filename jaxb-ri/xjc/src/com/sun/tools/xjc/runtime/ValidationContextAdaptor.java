/*
 * @(#)$Id: ValidationContextAdaptor.java,v 1.1 2004-06-25 21:15:26 kohsuke Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.runtime;

import org.relaxng.datatype.ValidationContext;

/**
 * Wraps {@link UnmarshallingContext} and makes it look like a
 * {@link ValidationContext}.
 * 
 * <p>
 * This indirection allows the core of the runtime to be independent
 * from the RELAX NG datatype interface. 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ValidationContextAdaptor implements ValidationContext {
    
    private final UnmarshallingContext core;
    
    public ValidationContextAdaptor( UnmarshallingContext _context ) {
        this.core = _context;
    }
    
    public String getBaseUri() {
        return core.getBaseUri();
    }
    
    public boolean isNotation(String notationName) {
        return core.isNotation(notationName);
    }

    public boolean isUnparsedEntity(String entityName) {
        return core.isUnparsedEntity(entityName);
    }

    public String resolveNamespacePrefix(String prefix) {
        return core.resolveNamespacePrefix(prefix);
    }

}
