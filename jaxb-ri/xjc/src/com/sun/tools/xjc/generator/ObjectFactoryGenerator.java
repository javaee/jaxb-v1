/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;
import com.sun.codemodel.fmt.JPropertyFile;
import com.sun.codemodel.fmt.JSerializedObject;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.writer.relaxng.RELAXNGWriter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.Constructor;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.runtime.DefaultJAXBContextImpl;
import com.sun.tools.xjc.runtime.GrammarInfo;
import com.sun.tools.xjc.runtime.GrammarInfoImpl;
import com.sun.tools.xjc.util.Util;
import com.sun.xml.bind.ContextFactory_1_0_1;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Generates <code>ObjectFactory</code> then wraps it and provides
 * access to it.
 * 
 * The ObjectFactory contains:
 *   o the embedded GrammarInfo necessary for unmarshalling
 *   o static factory methods for each schema derived content class
 *   o shadow method for DefaultJaxbContextImpl.newInstance for 
 *     developer convenience
 *
 * ObjectFactory used to be named JAXBContextImpl but was changed at
 * the request of the EG.
 * 
 * @author
 *      Ryan Shoemaker
 */
final class ObjectFactoryGenerator {

    /** Assign System.out or something alike to see trace messages. */
    private final static PrintStream debug =
        Util.getSystemProperty(ObjectFactoryGenerator.class,"debug")!=null?System.out:null;
    
    private final GeneratorContext context;
    private final AnnotatedGrammar grammar;
    private final JCodeModel codeModel;
    private final Options opt;
    
    /** The package that this ObjectFactory governs. */
    private final JPackage targetPackage;
    
    /** The static GrammarInfo instance inside the ObjectFactory class. */
    private final JVar $grammarInfo;
    
    /** The static rootTagMap instance inside the ObjectFactory class. */
    private final JVar $rootTagMap;
    
    private final DefaultImplementationMapGenerator defImplMapGenerator;
    
    /** reference to the generated ObjectFactory class. */
    private final JDefinedClass objectFactory;
    
    /**
     * Returns a reference to the generated GrammarInfoImpl class
     * inside the ObjectFactory.
     */
    public JVar getGrammarInfo() {
        return $grammarInfo;
    }
    
    /**
     * Returns a reference to the generated ObjectFactory
     */
    public JDefinedClass getObjectFactory() {
        return objectFactory;
    }
    
    public JVar getRootTagMap() {
        return $rootTagMap;
    }


    
    
    ObjectFactoryGenerator( 
        GeneratorContext _context,
        AnnotatedGrammar _grammar, Options _opt, JPackage _pkg ) {
        
        this.context = _context;
        this.grammar = _grammar;
        this.opt = _opt;
        this.codeModel = grammar.codeModel;
        this.targetPackage = _pkg;
        
        // create the ObjectFactory class skeleton
        objectFactory = context.getClassFactory().createClass(
                targetPackage, "ObjectFactory", null );
        
        this.defImplMapGenerator =
            new DefaultImplementationMapGenerator(
                Util.calculateInitialHashMapCapacity(countClassItems(), 0.75F));        
        
        this.$rootTagMap = objectFactory.field(JMod.PRIVATE|JMod.STATIC,
                HashMap.class,
                "rootTagMap",
                JExpr._new(objectFactory.owner().ref(HashMap.class)));
        
        // generate an ObjectFactory class for a given package.
        
        objectFactory._extends(context.getRuntime(DefaultJAXBContextImpl.class));
    
        // also generate jaxb.properties file
        // TODO: this is a sort of quick hack.
        // we need to generate ObjectFactory and jaxb.properties into many directories.
        JPropertyFile jaxbProperties = new JPropertyFile("jaxb.properties");
        targetPackage.addResourceFile(jaxbProperties);
        jaxbProperties.add(
            JAXBContext.JAXB_CONTEXT_FACTORY,
            ContextFactory_1_0_1.class.getName());
        jaxbProperties.add(
            ContextFactory_1_0_1.RUNTIME_KEY,
            context.getRuntime(DefaultJAXBContextImpl.class).fullName() );
        
        if(opt.debugMode) {
            // also creates ObjectFactory in the root package
            // so that it can be easily found by the batch test program
            if(!targetPackage.isUnnamed() ) {
                try {
                    codeModel._package("")._class("ObjectFactory")
                        ._extends(objectFactory);
                } catch( JClassAlreadyExistsException e ) {
                    // we don't mind if it already exists.
                    // this is the debug mode, after all.
                }
            }
        }

        // generate GrammarInfo object 
        $grammarInfo = objectFactory.field(
            JMod.PUBLIC|JMod.STATIC|JMod.FINAL,
            context.getRuntime(GrammarInfo.class),
            "grammarInfo",
            JExpr._new(context.getRuntime(GrammarInfoImpl.class))
                .arg($rootTagMap).arg(defImplMapGenerator.$map).arg(objectFactory.dotclass()) );
        
        
        // generate the default constructor
        //
        // m1 result:
        //        public ObjectFactory() {
        //            super( new ObjectFactory.GrammarInfoImpl() );
        //        }
        JMethod m1 = objectFactory.constructor(JMod.PUBLIC);
        m1.body().invoke("super").arg($grammarInfo);
        m1.javadoc()
            .setComment( "Create a new ObjectFactory that can be used to " +
                         "create new instances of schema derived classes " +
                         "for package: " + targetPackage.name() );
        
        // unneccessarily shadow DefaultJAXBContextImpl methods so that when
        // developers look at the generated code, they will see the api signatures
        // for the newInstance method.  These are purely clues for the developer.
        //
        // m2 result:
        // public Object newInstance( Class javaContentInterface )
        //     throws JAXBException {
        //
        //     return DefaultJAXBContextImpl.newInstance( javaContentInterface );
        // }
        JMethod m2 = objectFactory.method( JMod.PUBLIC,
                                         codeModel.ref( Object.class ),
                                         "newInstance" )
                                ._throws( javax.xml.bind.JAXBException.class );
        m2.param( Class.class, "javaContentInterface" );
        m2.body()._return( JExpr.invoke( JExpr._super(),
                                         "newInstance" )
                                .arg( JExpr.ref( "javaContentInterface" ) ) );
        m2.javadoc()
            .setComment(
                "Create an instance of the specified Java content interface." )  
            .addParam( "javaContentInterface", "the Class object of the java" +
                       "content interface to instantiate" )
            .addReturn( "a new instance" )
            .addThrows( "JAXBException",  "if an error occurs" );
        
        // unneccessarily shadow DefaultJAXBContextImpl methods so that when
        // developers look at the generated code, they will see the api signatures
        // for the get/setProperty methods.  These are purely clues for the developer.
        //
        // m3 result:
        // public Object getProperty( String name )
        //     throws PropertyException {
        //
        //     return super.getProperty( name );
        // }
        //
        // m4 result:
        // public void setProperty( String name, Object value )
        //     throws PropertyException {
        //
        //     super.setProperty( name, value );
        // }
        {
            JMethod m3 = objectFactory.method( JMod.PUBLIC,
                                               codeModel.ref( Object.class ),
                                               "getProperty" )
                ._throws( javax.xml.bind.PropertyException.class );
            JVar $name = m3.param( String.class, "name" );
            m3.body()._return( JExpr._super().invoke("getProperty").arg( $name ) );
            m3.javadoc()
                .setComment( "Get the specified property. This method can only be\n" +
                             "used to get provider specific properties.\n" +
                             "Attempting to get an undefined property will result\n" +
                             "in a PropertyException being thrown." )
                .addParam( "name", "the name of the property to retrieve" )
                .addReturn( "the value of the requested property" )
                .addThrows(  "PropertyException", "when there is an error " +
                             "retrieving the given property or value" );
        }
        
        {
            JMethod m4 = objectFactory.method( JMod.PUBLIC,
                                               codeModel.VOID,
                                               "setProperty" )
                ._throws( javax.xml.bind.PropertyException.class );
            JVar $name = m4.param( String.class, "name" );
            JVar $value =m4.param( Object.class, "value" );
            m4.body().invoke( JExpr._super(), "setProperty" )
                .arg($name).arg($value);
            m4.javadoc()
                .setComment( "Set the specified property. This method can only be\n" +
                             "used to set provider specific properties.\n" +
                             "Attempting to set an undefined property will result\n" +
                             "in a PropertyException being thrown." )
                .addParam( "name", "the name of the property to retrieve" )
                .addParam( "value", "the value of the property to be set" )
                .addThrows(  "PropertyException", "when there is an error " +
                             "processing the given property or value" );
        }

        // serialize the bgm for use in the runtime validation system
        Grammar purifiedGrammar = AGMBuilder.remove( grammar );
        try {
            targetPackage.addResourceFile( new JSerializedObject(
                "bgm.ser",purifiedGrammar) );
        } catch( IOException e ) {
            // we know that BGM can be always serialized. So this shouldn't be possible
            throw new JAXBAssertionError(e);
        }

        if(debug!=null) {
            debug.println("---- schema ----");
            try {
                RELAXNGWriter w = new RELAXNGWriter();
                com.sun.org.apache.xml.internal.serialize.OutputFormat format = 
                    new com.sun.org.apache.xml.internal.serialize.OutputFormat("xml",null,true);
                format.setIndent(1);
                w.setDocumentHandler(new com.sun.org.apache.xml.internal.serialize.XMLSerializer(debug,format));
                w.write(purifiedGrammar);
            } catch( org.xml.sax.SAXException e ) {
                throw new JAXBAssertionError(e);
            }
        }

        
        // add some class javadoc
        objectFactory.javadoc().appendComment( 
            "This object contains factory methods for each \n" +
            "Java content interface and Java element interface \n" +
            "generated in the " + targetPackage.name() + " package. \n" +
            "<p>An ObjectFactory allows you to programatically \n" +
            "construct new instances of the Java representation \n" + 
            "for XML content. The Java representation of XML \n" +
            "content can consist of schema derived interfaces \n" +
            "and classes representing the binding of schema \n" +
            "type definitions, element declarations and model \n" +
            "groups.  Factory methods for each of these are \n" +
            "provided in this class." );
            
    }
    
    



    /**
     * Adds code that is relevant to a given ClassItem to
     * ObjectFactory.
     */
    void populate( ClassContext cc ) {
        // add static factory method for this class to JAXBContext.
        //
        // generate methods like:
        //     public static final Foo createFoo() throws JAXBException {
        //         return new FooImpl();
        //     }
        {
        
            JMethod m = objectFactory
                            .method( JMod.PUBLIC, 
                                     cc.ref,
                                     "create" + getPartlyQualifiedName(cc.ref) )
                            ._throws( JAXBException.class );
            m.body()._return( JExpr._new(cc.implRef) );

            // add some jdoc to avoid javadoc warnings in jdk1.4
            m.javadoc()
                .appendComment( "Create an instance of " + 
                                 getPartlyQualifiedName(cc.ref) )
                .addThrows( "JAXBException", "if an error occurs" );
        }        
        

        // add static factory methods for all the other constructors.
        Iterator itr = cc.target.iterateConstructors();
        if(itr.hasNext()) {
            // if we are going to add constructors with parameters,
            // first we need to have a default constructor.
            cc.implClass.constructor(JMod.PUBLIC);
        }
        
        while(itr.hasNext()) {
            Constructor cons = (Constructor)itr.next();
            
            // method on ObjectFactory
            // [RESULT]
            // Foo createFoo( T1 a, T2 b, T3 c, ... ) throws JAXBException {
            //    return new FooImpl(a,b,c,...);
            // }
            JMethod m = objectFactory.method( JMod.PUBLIC,
                cc.ref, "create" + getPartlyQualifiedName(cc.ref) );
            JInvocation inv = JExpr._new(cc.implRef);
            m.body()._return(inv);
            
            m._throws(codeModel.ref(JAXBException.class));
            
            // add some jdoc to avoid javadoc warnings in jdk1.4
            m.javadoc()
                .appendComment( "Create an instance of " + 
                                 getPartlyQualifiedName(cc.ref) )
                .addThrows( "JAXBException", "if an error occurs" );
            
            // constructor
            // [RESULT]
            // FooImpl( T1 a, T2 b, T3 c, ... ) {
            // }
            JMethod c = cc.implClass.constructor(JMod.PUBLIC);
            
            for( int i=0; i<cons.fields.length; i++ ) {
                String fieldName = cons.fields[i];
                FieldUse field = cc.target.getField(fieldName);
                if(field==null) {
                    // TODO: error rpoert
                    throw new UnsupportedOperationException("illegal constructor param name: "+fieldName);
                }

                fieldName = camelize(fieldName);

                FieldRenderer renderer = context.getField(field);
                // assert(renderer!=null); since this has already been populated
                
                JVar $fvar; // variable on the factory method.
                
                // declare a parameter on this factory method and set
                // it to the field
                if(field.multiplicity.isAtMostOnce()) {
                    
                    $fvar     = m.param( field.type, fieldName );
                    JVar $var = c.param( field.type, fieldName );
                    
                    renderer.setter(c.body(),$var);
                } else {
                    // for collection fields
                    $fvar     = m.param( field.type.array(), fieldName );
                    JVar $var = c.param( field.type.array(), fieldName );
                    // [RESULT] for( int i=0; i<array.length; i++ ) <setter>(val[i]);
                    JForLoop forLoop = c.body()._for();
                    JVar $i = forLoop.init( codeModel.INT, "___i", JExpr.lit(0) );
                    forLoop.test( $i.lt($var.ref("length")));
                    forLoop.update( $i.incr() );
                    
                    renderer.setter( forLoop.body(), $var.component($i) );
                }
                
                inv.arg($fvar);
            }
        }
        

        defImplMapGenerator.add( cc.ref, cc.implRef );
    }


    /** Prepends all the outer class names. */
    private String getPartlyQualifiedName( JDefinedClass cls ) {
        if(cls.parentContainer() instanceof JPackage)
            return cls.name();
        else
            return getPartlyQualifiedName(
                (JDefinedClass)cls.parentContainer())+cls.name();
    }
    

    /** Change the first character to the lower case. */
    private static String camelize( String s ) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
    
    /** count the number of ClassItems in the current package context */
    private int countClassItems() {
        ClassItem[] classItems = grammar.getClasses();
        int count = 0;
        
        // the class item array contains all ClassItems for the entire schema.
        // we only need to know how many are in our package.
        for( int i = 0; i < classItems.length; i++ ) {
            if( ((JClass)classItems[i].getTypeAsDefined())._package() == targetPackage ) {
                count++;
            }
        }
        return count;
    }

    /**
     * Governs the generation of a hash map that keeps track of
     * public content interface -> our implementation class mapping.
     * 
     * <p>
     * This class hides a logic of supporting a huge schema.
     */
    private class DefaultImplementationMapGenerator extends StaticMapGenerator
    {
        DefaultImplementationMapGenerator(int initialCapacity) {
            // generate a static dictionary that keeps track of content interface
            // -> implementation mapping
            super(
                objectFactory.field(JMod.PRIVATE|JMod.STATIC,
                    HashMap.class,
                    "defaultImplementations",
                    JExpr._new(
                            codeModel.ref(HashMap.class))
                                .arg(JExpr.lit(initialCapacity))
                                .arg(JExpr.lit(0.75F))),
                objectFactory.init());
        }
        
        /**
         * Generates an association.
         */
        public void add( JDefinedClass _interface, JClass _implementation ) {
            super.add( _interface.dotclass(), JExpr.lit(_implementation.fullName()) );
        }

        protected JMethod createNewMethod( int uniqueId ) {
            return objectFactory.method(
                JMod.PRIVATE|JMod.STATIC,codeModel.VOID,"__$$init$$"+uniqueId);
        }
    }
}
