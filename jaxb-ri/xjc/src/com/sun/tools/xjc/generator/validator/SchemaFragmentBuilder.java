/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.validator;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.JavaItemVisitor;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.runtime.MSVValidator;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Builds a schema fragment from AGM fragment of a {@link ClassItem}.
 * 
 * <p>
 * The obtained schema fragment will be used at run-time to
 * validate content tree objects.
 * 
 * To create a schema fragment rooted at <code>exp</code>,
 * call <code>exp.visit(schemaFragmentBuilder)</code>.
 */
class SchemaFragmentBuilder extends ExpressionCloner implements JavaItemVisitor
{
    /** set to true when we are visiting inside an AttributeExp. */
    private boolean inAttribute = false;
    
    /** set to true when we are visiting inside a SuperClassItem. */
    private boolean inSuperClass = false;
    
    public SchemaFragmentBuilder( ExpressionPool pool ) {
        super(pool);
    }
    
    public Expression onRef( ReferenceExp exp ) {
        return exp.exp.visit(this);
    }
    
    public Expression onOther( OtherExp exp ) {
        if(exp instanceof JavaItem )
            return (Expression)((JavaItem)exp).visitJI(this);
        else
            return exp.exp.visit(this);
    }
    
    public Expression onAttribute( AttributeExp exp ) {
        if(inAttribute)
            // AttributeExp cannot contain AttributeExp.
            throw new JAXBAssertionError();
        
        inAttribute = true;
        
        try {
            return new AttributeExp( exp.nameClass, exp.exp.visit(this) );
        } finally {
            inAttribute = false;
        }
    }
    
    public Expression onElement( ElementExp exp ) {
        return createElement(exp);
    }

    public ElementPattern createElement(NameClassAndExpression exp) {
        return new ElementPattern(
            exp.getNameClass(),
            exp.getContentModel().visit(this) );
    }
    
    public Object onPrimitive( PrimitiveItem pi ) {
        return pi.exp.visit(this);
    }
    
    public Object onField( FieldItem fi ) {
        return fi.exp.visit(this);
    }
    
    public Object onIgnore( IgnoreItem ii ) {
        // TODO
        return ii.exp.visit(this);
    }
    
    public Object onInterface( InterfaceItem ii ) {
        return ii.exp.visit(this);
    }
    
    public Object onSuper( SuperClassItem si ) {
        inSuperClass = true;
        try {
            return si.exp.visit(this);
        } finally {
            inSuperClass = false;
        }
    }
    
    public Object onExternal( ExternalItem ei ) {
        return ei.createValidationFragment();
    }
    
    
    private Expression anyAttributes = 
        pool.createZeroOrMore(pool.createAttribute(NameClass.ALL));
    
    public Object onClass( ClassItem ii ) {
        if(inSuperClass) {
            inSuperClass = false;
            try {
                return ii.exp.visit(this);
            } finally {
                inSuperClass = true;
            }
        }
        
        if(inAttribute) {
            // in attribute body, put a marker text (\u0000<classname>)
            return pool.createValue(
                StringType.theInstance,
                ("\u0000"+ii.getType().fullName()).intern());
        } else {
            // in all other places, put a dummy element in place of a child object.
            
            // allw any attributes on this dummy element.
            return new ElementPattern(
                new SimpleNameClass(
                    MSVValidator.DUMMY_ELEMENT_NS,
                    ii.getType().fullName().intern() ),
                anyAttributes );
        }
    }
}
