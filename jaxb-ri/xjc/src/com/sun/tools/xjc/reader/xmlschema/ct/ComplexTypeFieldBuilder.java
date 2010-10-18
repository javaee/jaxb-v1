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

import java.util.HashMap;
import java.util.Map;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComplexType;

/**
 * single entry point of building a field expression from a complex type.
 * 
 * One object is created for one {@link BGMBuilder}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ComplexTypeFieldBuilder {
    
    protected final BGMBuilder builder;
    
    /** All installed available complex type builders. */
    private final CTBuilder[] complexTypeBuilders;
    
    /** A map from XSComplexType to ComplexTypeBindingMode. */
    private final Map complexTypeBindingModes = new HashMap();
    
    
    
    public ComplexTypeFieldBuilder( BGMBuilder _builder ) {
        this.builder = _builder;
        
        // set up all the available complex type builders
        // put specific ones first.
        complexTypeBuilders = new CTBuilder[]{
            new ChoiceComplexTypeBuilder(this),
            new MixedComplexTypeBuilder(this),
            new FreshComplexTypeBuilder(this),
            new ExtendedComplexTypeBuilder(this),
            new RestrictedComplexTypeBuilder(this),
            new STDerivedComplexTypeBuilder(this)
        };
    }
    
    /**
     * Binds a complex type to a field expression.
     */
    public Expression build( XSComplexType type ) {
        for( int i=0; i<complexTypeBuilders.length; i++ )
            if( complexTypeBuilders[i].isApplicable(type) )
                return complexTypeBuilders[i].build(type);
        
        _assert(false);
        return null;
    }
    
    /**
     * Records the binding mode of the given complex type.
     * 
     * <p>
     * Binding of a derived complex type often depends on that of the
     * base complex type. For example, when a base type is bound to
     * the getRest() method, all the derived complex types will be bound
     * in the same way.
     * 
     * <p>
     * For this reason, we have to record how each complex type is being
     * bound. 
     */
    protected void recordBindingMode( XSComplexType type, ComplexTypeBindingMode flag ) {
        // it is an error to override the flag.
        Object o = complexTypeBindingModes.put(type,flag); 
        _assert( o==null );
    }
    
    /**
     * Obtains the binding mode recorded through
     * {@link #recordBindingMode(XSComplexType, ComplexTypeBindingMode)}.
     */
    protected ComplexTypeBindingMode getBindingMode( XSComplexType type ) {
        Object r = complexTypeBindingModes.get(type);
        _assert(r!=null);
        return (ComplexTypeBindingMode)r;
    }

    protected static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
}
