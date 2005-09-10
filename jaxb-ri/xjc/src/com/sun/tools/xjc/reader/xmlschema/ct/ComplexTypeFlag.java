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
package com.sun.tools.xjc.reader.xmlschema.ct;

/**
 * Three-state flag for a complex type.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
final class ComplexTypeFlag {
    private ComplexTypeFlag( String name ) { this.name=name; }
    
    private final String name;
    public String toString() { return name; }
    
    /**
     * Neither FALLBACK nor NOMOREEXTENSION.
     */
    static final ComplexTypeFlag NORMAL = new ComplexTypeFlag("normal");
    
    /**
     * If a complex type has falled back to the general list content and
     * it is not NOMOREEXTENSION.
     */
    static final ComplexTypeFlag FALLBACK_CONTENT = new ComplexTypeFlag("fallback(content)");
    
    /**
     * If a complex type has falled back to the rest content and
     * it is not NOMOREEXTENSION.
     */
    static final ComplexTypeFlag FALLBACK_REST = new ComplexTypeFlag("fallback(rest)");
//    
//    /**
//     * If a complex type is derived by restriction from a complex type
//     * other than the ur-type. Once this flag is turned on, no more
//     * derivation by extension is allowed.
//     */
//    static final ComplexTypeFlag NOMOREEXTENSION = new ComplexTypeFlag("noMoreExtension");
}
