/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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