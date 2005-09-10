/*
 * @(#)$Id: ValidationContextAdaptor.java,v 1.2 2005-09-10 18:20:45 kohsuke Exp $
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
