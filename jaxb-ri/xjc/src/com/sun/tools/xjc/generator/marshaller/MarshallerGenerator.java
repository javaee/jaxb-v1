/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.marshaller;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.runtime.XMLSerializable;
import com.sun.tools.xjc.runtime.XMLSerializer;
import com.sun.xml.bind.marshaller.IdentifiableObject;
import com.sun.xml.bind.serializer.Util;

// TODO: this should be renamed to "SerializerGenerator"

/**
 * Generates marshallers by using information on BGM.
 * 
 * @author  Kohsuke Kawaguchi
 */
public class MarshallerGenerator
{
    /**
     * Generates marshallers into the code model.
     */
    public static void generate(
        AnnotatedGrammar grammar,
        GeneratorContext context,
        Options opt ) {
        
        new MarshallerGenerator(grammar,context,opt);
    }
    
    
    private final AnnotatedGrammar grammar;
    private final GeneratorContext context;
//    private final Options opt;
    
    private MarshallerGenerator(
        AnnotatedGrammar _grammar,
        GeneratorContext _context,
        Options _opt ) {

        this.grammar = _grammar;
        this.context = _context;
//        this.opt = _opt;
        
        
        ClassItem[] cs = grammar.getClasses();
        for( int i=0; i<cs.length; i++ )
            generate(context.getClassContext(cs[i]));
    }
    
    /**
     * Generates a marshaller for the specified class into the class.
     */
    private void generate( ClassContext cc ) {
        
        // A class has to implement MarshallableObject to be marshallable.
        cc.implClass._implements(
            context.getRuntime(XMLSerializable.class));
        
        // generate three marshaller methods.
        generateMethodSkeleton(cc,"serializeBody").bodyPass.build(cc.target.exp);
        generateMethodSkeleton(cc,"serializeAttributes").attPass.build(cc.target.exp);
        generateMethodSkeleton(cc,"serializeURIs").uriPass.build(cc.target.exp);
        
        processID(cc);
    }
    
    /**
     * Checks if this class has an primitive item of ID.
     * If so, implement {@link IdentifiableObject}.
     */
    private void processID( final ClassContext cc ) {
        cc.target.exp.visit(new BGMWalker(){
            private FieldItem currentField;
            private boolean idProcessed = false;
            
            public Object onField( FieldItem fi ) {
                // assert(currentField==null)
                currentField = fi;
                fi.exp.visit(this);
                currentField = null;
                return null;
            }
            public Object onPrimitive( PrimitiveItem p ) {
                if(p.xducer.isID() && !idProcessed) {
                    // this class has ID type.
                    
                    // a class can have more than one ID value.
                    // if so, only use the first one and ignore the rest.
                    // conceptually, it doesn't make sense to have
                    // more than one ID in one object, and there is
                    // no way to specify which one to you.
                    idProcessed = true;
                    
                    // have it implement IdentifiableObject
                    JCodeModel codeModel = cc.implClass.owner();
                    JClass refString = codeModel.ref(String.class);
                    
                    cc.implClass._implements(IdentifiableObject.class);
                    
                    // [RESULT]
                    // String ____jaxb____getId() {
                    //     <initialize field marshaller>;
                    //     return (String)<fm.next()>;
                    // }
                    JBlock body = cc.implClass.method(JMod.PUBLIC,
                        refString,"____jaxb____getId").body();
                    
                    FieldMarshallerGenerator fmg =
                        context.getField( cc.target.getDeclaredField(currentField.name) )
                            .createMarshaller(body,"t");
                    
                    // this ID can be unioned with some
                    // other types. Hence we are not sure if it evaluates to
                    // String by default. So cast it explicit.y
                    body._return( JExpr.cast( refString, fmg.peek(true) ) );
                }
                return p;
            }
            public Object onInterface( InterfaceItem i )    { return null; }
            public Object onClass( ClassItem c )            { return null; }
            public Object onExternal( ExternalItem i )      { return null; }
            public Object onIgnore( IgnoreItem i )          { return null; }
            public Object onSuper( SuperClassItem s )       { return null; }
        });
    }
    
    /**
     * Generates the method skeleton for a marshaller method
     * and returns the context which then be used to generate
     * the body of that method.
     */
    private Context generateMethodSkeleton( ClassContext cc, String methodName ) {
        // generate a method signature
        // [RESULT] void <methodName>( XMLSerializer target ) throws SAXException;
        JMethod p = cc.implClass.method( JMod.PUBLIC, grammar.codeModel.VOID, methodName);
        JVar $serializer = p.param( context.getRuntime(XMLSerializer.class), "context" );
        p._throws(SAXException.class);
        JBlock body = p.body();
        
        // initialize field marshallers
        FieldUse[] uses = cc.target.getDeclaredFieldUses();
        Map fieldMarshallers = new HashMap();
        for( int i=0; i<uses.length; i++ ) {
            fieldMarshallers.put( uses[i],
                context.getField(uses[i]).createMarshaller( body, Integer.toString(i+1) )
            );
            
            if( uses[i].multiplicity.isUnique() && uses[i].type.isPrimitive() ) {
                // if the field is mandatory but is primitive, then
                // marshaller doesn't (at least usually, if not always) check
                // if the field is really set or not. As a result, even if
                // the value is not set, the marshalling goes on and you
                // see the VM default value. So if the field fits the profile
                // check the existence first and do the error reporting.
                // also see bug 4894443
                
                // [RESULT]
                // if(!(field has a set value)) {
                //    serializer.reportError(Util.createMissingObjectError(this,"fieldName"));
                // } 
                JExpression hasSetValue = context.getField(uses[i]).hasSetValue();
                if( hasSetValue!=null ) {
                    JConditional cond = body._if( hasSetValue.not() );
                    cond._then().invoke( $serializer, "reportError" )
                        .arg( grammar.codeModel.ref(Util.class).staticInvoke("createMissingObjectError")
                            .arg(JExpr._this()).arg(JExpr.lit(uses[i].name)) );
                }
            }
        }
        
        return new Context(context,grammar.getPool(),cc.target,body,$serializer,fieldMarshallers);
    }
}
