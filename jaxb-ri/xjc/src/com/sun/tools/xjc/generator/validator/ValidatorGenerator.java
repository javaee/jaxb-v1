/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.validator;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.msv.grammar.trex.TREXGrammar;
import com.sun.msv.grammar.util.ExpressionPrinter;
import com.sun.msv.verifier.DocumentDeclaration;
import com.sun.msv.verifier.regexp.REDocumentDeclaration;
import com.sun.msv.writer.relaxng.RELAXNGWriter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.runtime.ValidatableObject;
import com.sun.tools.xjc.util.Util;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.validator.SchemaDeserializer;

/**
 * Generates code (by augmenting code model) for the runtime validation.
 */
public class ValidatorGenerator
{
    /** Assign System.out or something else to see trace messages. */
    private final static java.io.PrintStream debug =
        Util.getSystemProperty(ValidatorGenerator.class,"debug")!=null?System.out:null;
    
    /**
     * Generates validators (the "validate" method) into the code model.
     */
    public static void generate( AnnotatedGrammar grammar, GeneratorContext context, Options opt ) {
        final JCodeModel codeModel = grammar.codeModel;
        
        // for each class ...
        ClassItem[] cis = grammar.getClasses();
        for( int i=0; i<cis.length; i++ ) {
            final ClassItem ci = cis[i];
            final JDefinedClass cls = context.getClassContext(ci).implClass;
            
            // make it implement ValidatableObject
            cls._implements(context.getRuntime(ValidatableObject.class));
            
            {// implement the getPrimaryInterface method.
                JMethod method = cls.method(
                    JMod.PUBLIC, Class.class, "getPrimaryInterface");
                // [RESULT] return <intfClass>.class;
                method.body()._return(((JClass)ci.getType()).dotclass());
            }
            
            //  create a schema fragment for the specified class
            ExpressionPool pool = new ExpressionPool();
            Expression fragment = createSchemaFragment(ci,pool);
            if(opt.debugMode && opt.verbose) {
                System.out.println(ci.getType().fullName());
                System.out.println(ExpressionPrinter.printFragment(fragment));
                System.out.println();
            }
            
            if(debug!=null) {
                debug.println("---- schema fragment for "+ci.name+" ----");
                try {
                    TREXGrammar g = new TREXGrammar(pool);
                    g.exp = fragment;
                    RELAXNGWriter w = new RELAXNGWriter();
                    com.sun.org.apache.xml.internal.serialize.OutputFormat format = 
                        new com.sun.org.apache.xml.internal.serialize.OutputFormat("xml",null,true);
                    format.setIndent(1);
                    w.setDocumentHandler(new com.sun.org.apache.xml.internal.serialize.XMLSerializer(debug,format));
                    w.write(g);
                } catch( org.xml.sax.SAXException e ) {
                    e.printStackTrace();
                    throw new JAXBAssertionError();
                }
            }
            
            // serialize them into a string
            StringWriter sw = new StringWriter();
            saveFragmentTo( fragment, pool, new StringOutputStream(sw) );
            
            String deserializeMethodName = "deserialize";
            
            if( sw.getBuffer().length()>32*1024 ) {
                // if the encoded string is too big, try to compress it.
                sw = new StringWriter();
                try {
                    saveFragmentTo( fragment, pool,
                        new GZIPOutputStream(new StringOutputStream(sw)) );
                    deserializeMethodName = "deserializeCompressed";
                } catch( IOException e ) {
                    // can't happen
                    throw new InternalError(e.getMessage());
                }
            }
            
            
            // [RESULT] private static Grammar schemaFragment;
            //
            // this variable will be set once the grammar is built

            JFieldVar $schemaFragment = cls.field(JMod.PRIVATE|JMod.STATIC,
                Grammar.class,"schemaFragment" );
            
            
            // turn the fragment into JExpression
            JExpression encodedFragment;
            
            if( Util.getSystemProperty(ValidatorGenerator.class,"noSplit")!=null )
                encodedFragment = JExpr.lit(sw.toString());
            else {
                int len = sw.getBuffer().length();
                StringBuffer buf = new StringBuffer(len);
                for( int j=0; j<len; j+=60 ) {
                    buf.append('\n');
                    if(j!=0)    buf.append('+');
                    else        buf.append(' ');
                    buf.append(JExpr.quotify('"',sw.getBuffer().substring(j,Math.min(j+60,len))));
                }
                encodedFragment = JExpr.direct(buf.toString());
            }
            
            {// generate the createRawVerifier method.
                // [RESULT]
                //    public DocumentDeclaration createRawValidator() {
                //        if(schemaFragment==null)
                //            schemaFragment = SchemaDeserializer.deserialize("....");
                //        return new REDocumentDeclaration(schemaFragment);
                //    }
                
                JMethod m = cls.method(JMod.PUBLIC,
                    DocumentDeclaration.class,
                    "createRawValidator");
                m.body()._if($schemaFragment.eq(JExpr._null()))._then()
                    .assign( $schemaFragment,
                        codeModel.ref(SchemaDeserializer.class).staticInvoke(deserializeMethodName)
                            .arg( encodedFragment ));
                m.body()._return(
                    JExpr._new(codeModel.ref(REDocumentDeclaration.class))
                        .arg($schemaFragment));
            }
        }
    }
    
    private static void saveFragmentTo( Expression fragment, ExpressionPool pool, OutputStream os ) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(fragment);
            oos.writeObject(pool);
            oos.close();
        } catch( IOException e ) {
            // we know that the schema is always serializable.
            // so this is impossible
            throw new JAXBAssertionError(e);
        }
    }
    
    private static Expression createSchemaFragment( ClassItem ci, ExpressionPool pool ) {
        // build a schema fragment
        Expression exp;
        if( ci.agm.exp==null )  exp = ci.exp;
        else                    exp = ci.agm.exp;   // UGLY
        exp = exp.visit(new SchemaFragmentBuilder(new ExpressionPool()));
        
        // intern it
        return exp.visit(new ExpressionCloner(pool) {
            // reference exp intern map
            private final Map m = new HashMap();
            
            public Expression onAttribute( AttributeExp exp ) {
                return new AttributeExp( exp.nameClass, exp.exp.visit(this) );
            }
    
            public Expression onElement( ElementExp exp ) {
                return new ElementPattern(
                    exp.getNameClass(),
                    exp.contentModel.visit(this) );
            }
            
            public Expression onRef( ReferenceExp exp ) {
                if( m.containsKey(exp) )    return (Expression)m.get(exp);
                
                ReferenceExp i = new ReferenceExp(null);
                m.put(exp,i);
                i.exp = exp.exp.visit(this);
                return i;
            }
            
            public Expression onOther( OtherExp exp ) {
                // this expression has to be removed completely by the
                // schema fragment builder
                throw new JAXBAssertionError();
            }
        });
    }
}
