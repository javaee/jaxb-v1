/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.ct;

/**
 * Three-state flag for a complex type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ComplexTypeBindingMode {
    private ComplexTypeBindingMode( String name ) { this.name=name; }
    
    private final String name;
    public String toString() { return name; }
    
    /**
     * Neither FALLBACK nor NOMOREEXTENSION.
     */
    static final ComplexTypeBindingMode NORMAL = new ComplexTypeBindingMode("normal");
    
    /**
     * If a complex type has falled back to the general list content and
     * it is not NOMOREEXTENSION.
     */
    static final ComplexTypeBindingMode FALLBACK_CONTENT = new ComplexTypeBindingMode("fallback(content)");
    
    /**
     * If a complex type has falled back to the rest content and
     * it is not NOMOREEXTENSION.
     */
    static final ComplexTypeBindingMode FALLBACK_REST = new ComplexTypeBindingMode("fallback(rest)");
//    
//    /**
//     * If a complex type is derived by restriction from a complex type
//     * other than the ur-type. Once this flag is turned on, no more
//     * derivation by extension is allowed.
//     */
//    static final ComplexTypeBindingMode NOMOREEXTENSION = new ComplexTypeBindingMode("noMoreExtension");
}