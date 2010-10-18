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
