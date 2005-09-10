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
package com.sun.tools.xjc.reader.xmlschema;

import java.util.Stack;

import com.sun.msv.datatype.xsd.PositiveIntegerType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.WhitespaceTransducer;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXIdSymbolSpace;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;

/**
 * Builds fragments for simple types.
 * 
 * <p>
 * This class is just a coordinator and all the actual works
 * is done in classes like ConversionFinder/DatatypeBuilder.
 * 
 * <p>
 * There is at least one ugly code that you need to aware of
 * when you are modifying the code. See the documentation
 * about <a href="package.html#stref_cust">
 * "simple type customization at the point of reference."</a>
 * 
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SimpleTypeBuilder {
    SimpleTypeBuilder( BGMBuilder builder ) {
        this.builder = builder;
        this.datatypeBuilder = new DatatypeBuilder(builder,builder.schemas);
        this.conversionFinder = new ConversionFinder(builder);
        this.pool = builder.pool;
    }
    
    protected final BGMBuilder builder;
    
    /** Code that builds XSDatatype. */
    public final DatatypeBuilder datatypeBuilder;
    
    protected final ConversionFinder conversionFinder;
    
    private final ExpressionPool pool;
    
    /**
     * The component that is refering to the simple type
     * which we are building. This is ugly but necessary
     * to support the customization of simple types at
     * its point of reference. See my comment at the header 
     * of this class for details.
     * 
     * UGLY: Implemented as a Stack of XSComponent to fix a bug
     */
    public final Stack refererStack = new Stack();
    
    /**
     * Entry point from outside. Builds a BGM type expression
     * from a simple type schema component.
     * 
     * @param type
     *      the simple type to be bound.
     */
    public Expression build( XSSimpleType type ) {
        // check for the 
        Expression e = checkRefererCustomization(type);
        if(e==null)
            e = (Expression)type.apply(new Functor(type));
        
        return e;
    }
    
    /**
     * Returns a javaType customization specified to the referer, if present.
     * @return can be null.
     */
    private BIConversion getRefererCustomization() {
        BindInfo info = builder.getBindInfo((XSComponent)refererStack.peek());
        BIProperty prop = (BIProperty)info.get(BIProperty.NAME);
        if(prop==null)  return null;
        return prop.conv;
    }
    
    /**
     * Checks if the referer has a conversion customization or not.
     * If it does, use it to bind this simple type. Otherwise
     * return null;
     */
    private Expression checkRefererCustomization( XSSimpleType type ) {
        
        // assertion check. referer must be set properly
        // before the build method is called.
        // since the handling of the simple type point-of-reference
        // customization is very error prone, it deserves a strict
        // assertion check.
        // UGLY CODE WARNING
        XSComponent top = (XSComponent)refererStack.peek();
        
        if( top instanceof XSElementDecl ) {
            // if the parent is element type, its content type must be us.
            XSElementDecl eref = (XSElementDecl)top;
            _assert( eref.getType()==type );
            detectJavaTypeCustomization();
        } else
        if( top instanceof XSAttributeDecl ) {
            XSAttributeDecl aref = (XSAttributeDecl)top;
            _assert( aref.getType()==type );
            detectJavaTypeCustomization();
        } else
        if( top instanceof XSComplexType ) {
            XSComplexType tref = (XSComplexType)top;
            _assert( tref.getBaseType()==type );
            detectJavaTypeCustomization();
        } else
        if( top == type ) {
            // this means the simple type is built by itself and
            // not because it's referenced by something.
            ;
        } else
            // unexpected referer type.
            _assert(false);
        
        // now we are certain that the referer is OK.
        // see if it has a conversion customization.
        BIConversion conv = getRefererCustomization();
        if(conv!=null) {
            conv.markAsAcknowledged();
            // the conversion is given.
            return buildPrimitiveType( type, conv.getTransducer() );            
        } else
            // not found
            return null;
    }
    
    /**
     * Detect "javaType" customizations placed directly on simple types, rather
     * than being enclosed by "property" and "baseType" customizations (see
     * sec 6.8.1 of the spec).
     * 
     * Report an error if any exist.
     */
    private void detectJavaTypeCustomization() {
        BindInfo info = builder.getBindInfo((XSComponent)refererStack.peek());
        BIConversion conv = (BIConversion)info.get(BIConversion.NAME);

        if( conv != null ) {
            // ack this conversion to prevent further error messages
            conv.markAsAcknowledged();

            // report the error
            builder.errorReporter.error( conv.getLocation(), 
                    Messages.ERR_UNNESTED_JAVATYPE_CUSTOMIZATION_ON_SIMPLETYPE );
        }
    }
    
    /**
     * Builds a primitive type expression from a conversion.
     */
    private PrimitiveItem buildPrimitiveType( XSSimpleType type, Transducer xducer ) {
        XSDatatype dt = datatypeBuilder.build(type);
        return builder.grammar.createPrimitiveItem(
            WhitespaceTransducer.create(xducer,builder.grammar.codeModel,type),
            dt,
            pool.createData(dt),
            type.getLocator());
    }
    
    
    private class Functor implements XSSimpleTypeFunction {
        private Functor( XSSimpleType _type ) {
            this.originalType = _type;
        }


    
        /**
         * Checks the conversion specification for a given type.
         * 
         * If there is one, then this method builds an expression for
         * it and return. Otherwise null.
         */
        private Expression checkConversion( XSSimpleType type ) {
            Transducer t = conversionFinder.find(type);
            
            if(t!=null) {
                // if the conversion is found, follow it.
                
                // check ID symbol space customization
                if( t.getIDSymbolSpace()!=null ) {
                    BIXIdSymbolSpace ssc = (BIXIdSymbolSpace)builder.getBindInfo((XSComponent)refererStack.peek())
                        .get(BIXIdSymbolSpace.NAME);
                    if(ssc==null)
                        ssc = (BIXIdSymbolSpace)builder.getBindInfo(type)
                            .get(BIXIdSymbolSpace.NAME);
                
                    if(ssc!=null)
                        t = ssc.makeTransducer(t);
                }
            
                return buildPrimitiveType( type, t );
            } else
                return null;
        }
    
    
        
        /**
         * The type object to which we are building an expression.
         * 
         * When we build an expression from a list datatype, we'd like
         * to know the facets that are applied to that datatype,
         * (so that we can use the right occurence constraint.)
         * 
         * This would be only possible by keeping a reference to the
         * original datatype, which is why we have this field.
         */
        private final XSSimpleType originalType;
        
        public Object listSimpleType(XSListSimpleType type) {
            Expression e = checkConversion(type);
            if(e!=null)     return e;
            
            
            // check the length-related facets to decide if it should
            // be zeroOrMore or oneOrMore
            int min=0;
            int max=-1; // unbounded
            XSFacet length = originalType.getFacet("length");
            if(length!=null) {
                int v = PositiveIntegerType.load(length.getValue()).intValue();
                min = max = v;
            } else {
                XSFacet minLength = originalType.getFacet("minLength");
                if(minLength!=null) {
                    min = PositiveIntegerType.load(minLength.getValue()).intValue();
                }
                XSFacet maxLength = originalType.getFacet("maxLength");
                if(maxLength!=null) {
                    String v = maxLength.getValue().trim();
                    if(v.equals("unbounded"))   max=-1;
                    else
                        max = PositiveIntegerType.load(v).intValue();
                }
            }
            
            Expression item = (Expression)type.getItemType().apply(this);
            Expression body = min>0? pool.createOneOrMore(item) : pool.createZeroOrMore(item);
            
            // insert occurrence hint for this list.
            return pool.createList( new OccurrenceExp( body, max, min, item ) );
        }
        
        public Object unionSimpleType(XSUnionSimpleType type) {
            Expression e = checkConversion(type);
            if(e!=null)     return e;
            
            
            // union will be treated as choice of member types.
            int sz = type.getMemberSize();
            Expression exp = Expression.nullSet;
            
            for( int i=0; i<sz; i++ )
                exp = pool.createChoice(
                    exp, (Expression)type.getMember(i).apply(this) );
            
            return exp;
        }
    
        public Object restrictionSimpleType(XSRestrictionSimpleType type) {
            // I had an assertion check to make sure type is not anySimpleType,
            // but a test revealed that an attribute decl can legally have anySimpleTyp
            // as its type. Thus that assertion was incorrect.
            
            // if there is any conversion, follow it.
            Expression e = checkConversion(type);
            if(e!=null)     return e;
            
            
            // if not just process the base type.
            return type.getSimpleBaseType().apply(this);
        }
    }
    
    
    private static final void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
}
