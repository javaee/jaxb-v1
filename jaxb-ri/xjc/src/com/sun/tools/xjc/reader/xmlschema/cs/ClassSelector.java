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
package com.sun.tools.xjc.reader.xmlschema.cs;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Locator;

import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.util.JavadocEscapeWriter;
import com.sun.msv.grammar.Expression;
import com.sun.msv.util.LightStack;
import com.sun.tools.xjc.generator.field.IsSetFieldRenderer;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.reader.Util;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.JClassFactory;
import com.sun.tools.xjc.reader.xmlschema.PrefixedJClassFactoryImpl;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.util.SchemaWriter;

/**
 * Manages association between XSComponents and generated
 * content interfaces.
 * 
 * <p>
 * All the content interfaces are created, registered, and
 * maintained in this class.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ClassSelector {
    /**
     * If the package name is not specified in the schema,
     * this package will be used. Can be null if none is
     * specified.
     */
    private final String defaultPackageName;
    
    /** Center of builder classes. */
    public final BGMBuilder builder;

    /** Satellite builder. */
    protected final AGMFragmentBuilder agmFragmentBuilder;

    public final CodeModelClassFactory codeModelClassFactory;
    
    /**
     * Map from XSComponents to {@link Binding}s. Keeps track of all
     * content interfaces that are already built or being built.
     */
    private final Map bindMap = new HashMap();
    
    /**
     * A list of {@link Binding}s object that needs to be built.
     */
    private final LightStack bindQueue = new LightStack();
    
    /**
     * Object that determines components that are mapped
     * to classes.
     */
    private final ClassBinder classBinder;
    
    /**
     * Object that maps components to DOMs.
     */
    private final DOMBinder domBinder;
    
    /**
     * {@link JClassFactory}s that determines where a new class
     * should be created.
     */
    private final Stack classFactories = new Stack();
    
    private class Binding {
        private final XSComponent sc;
        private final ClassItem ci;
        private boolean built;
        
        Binding(XSComponent _sc, ClassItem _ci) {
            this.sc = _sc;
            this.ci = _ci;
        }
        
        void build() {
            if( built )     return; // already built
            built = true;
            
            ClassSelector.this.build( sc, ci );
            
            // if this schema component is an element declaration
            // and it satisfies a set of conditions specified in the spec,
            // this class will receive a constructor.
            if(needValueConstructor(sc)) {
                // TODO: fragile. There is no guarantee that the property name
                // is in fact "value".
                ci.addConstructor(new String[]{"Value"});
            }
        }
    }


    // should be instanciated only from BGMBuilder.
    public ClassSelector( BGMBuilder _builder, String defaultPackage ) {
        this.builder = _builder;
        this.agmFragmentBuilder = new AGMFragmentBuilder(_builder);
        this.codeModelClassFactory = new CodeModelClassFactory(
            builder.getErrorReceiver());
        this.domBinder = new DOMBinder(this);
        this.defaultPackageName = defaultPackage;
        
        ClassBinder c = new DefaultClassBinder(this);
        if( builder.getGlobalBinding().isModelGroupBinding() )
            c = new ModelGroupBindingClassBinder(this, c);
        classBinder = c;
        
        classFactories.push(null);  // so that the getClassFactory method returns null
    }
    
    /** Gets the current class factory. */
    public final JClassFactory getClassFactory() {
        return (JClassFactory)classFactories.peek();
    }
    
    public final void pushClassFactory( JClassFactory clsFctry ) {
        classFactories.push(clsFctry);
    }
    
    public final void popClassFactory() {
        classFactories.pop();
    }

    
    /**
     * A variation of the bindType method. A complex type is guaranteed
     * to be bound to a ClassItem if it is ever mapped to a type.
     * This method captures that invariant.
     */
    public ClassItem bindToType( XSComplexType ct ) {
        // binding the body of the complex type defines the characteristic
        // of binding  its derived types, so that has to be done eagerly.
        return _bindToClass(ct,true);
    }
    public TypeItem bindToType( XSElementDecl e ) {
        TypeItem t = domBinder.bind(e);
        if(t!=null)     return t;
        else            return _bindToClass(e,false);
    }
    
    /**
     * Checks if the given component is being mapped to a {@link TypeItem}
     * (such as {@link ClassItem}). If so, build that {@link TypeItem}
     * and return to the object.
     * If it is not being mapped to a type item, return null.
     */
    public Expression bindToType( XSComponent sc ) {
        Expression t = domBinder.bind(sc);
        if(t!=null)     return t;
        else            return _bindToClass(sc,false);
        // since the return type from thsi method is Expression, not ClassItem,
        // the caller shouldn't assume that the returned object is ClassItem.
        // thus it should mean that the caller is not looking into the body
        // of ClassItem. Thus we can defer the binding of the body.
    }
    
    /**
     * @param cannotBeDelayed
     *      if the binding of the body of the class cannot be defered
     *      and needs to be done immediately. If the flag is false,
     *      the binding of the body will be done later, to avoid
     *      cyclic binding problem. 
     */
    private ClassItem _bindToClass( XSComponent sc, boolean cannotBeDelayed ) {
        // check if this class is already built.
        if(!bindMap.containsKey(sc)) {
            // craete a bind task

            // use this opportunity to check abstract complex type check.
            // TODO: UGLY. move this code to the "right" place.
            if (sc instanceof XSElementDecl)
                checkAbstractComplexType((XSElementDecl) sc);
            
            // if this is a global declaration, make sure they will be generated
            // under a package.
            boolean isGlobal = false;
            if( sc instanceof XSDeclaration ) {
                isGlobal = ((XSDeclaration)sc).isGlobal();
                if( isGlobal )
                    pushClassFactory( new JClassFactoryImpl(this,
                        getPackage(((XSDeclaration)sc).getTargetNamespace())) );
            }
            
            // otherwise check if this component should become a class.
            ClassItem ci = (ClassItem)sc.apply(classBinder);
            
            if( isGlobal )
                popClassFactory();
            
            if(ci==null)
                return null;
                
            queueBuild( sc, ci );
        }
        
        Binding bind = (Binding)bindMap.get(sc);
        if( cannotBeDelayed )
            bind.build();
            
        return bind.ci;
    }
    
    /**
     * Runs all the pending build tasks.
     */
    public void executeTasks() {
        while( bindQueue.size()!=0 )
            ((Binding)bindQueue.pop()).build();
    }


    // should be used only from the checkAbstractComplexType method.
    // store complex types for which errors have already been reported
    // to avoid duplicate error messages.
    private Set reportedAbstractComplexTypes = null;

    /**
     * Checks if the given element declaration is refering to
     * an abstract complex type. If so, issue an error.
     * <p>
     * This is a part of the prohibited feature check, but the
     * nature of this check requires the schema component object
     * graph, so it cannot be done in the
     * {@link com.sun.tools.xjc.reader.xmlschema.parser.ProhibitedFeaturesFilter}
     * class.
     * <p>
     * This method needs to be called for all XSElementDecls in
     * the grammar. Currently, we do this as a part of the
     * AGMFragmentBuilder processing.
     */
    private void checkAbstractComplexType( XSElementDecl decl ) {
        if( builder.inExtensionMode )
            // in the extension mode, we are allowing abstract complex types.
            return;
        
        XSType t = decl.getType();
        if( t.isComplexType() && t.asComplexType().isAbstract() ) {
            // an abstract complex type referenced from an element declaration.
            // this is an error.
            
            if( reportedAbstractComplexTypes==null )
                reportedAbstractComplexTypes = new HashSet();
            
            if( reportedAbstractComplexTypes.add(t) ) {
                // report one error per one complex type.
                builder.errorReceiver.error( t.getLocator(),
                    Messages.format( Messages.ERR_ABSTRACT_COMPLEX_TYPE, t.getName() ));
                builder.errorReceiver.error( decl.getLocator(),
                    Messages.format( Messages.ERR_ABSTRACT_COMPLEX_TYPE_SOURCE ) );
            }
        }
    }
    




    
    
    /**
     * Determines if the given component needs to have a value
     * constructor (a constructor that takes a parmater.) on ObjectFactory.
     */
    private boolean needValueConstructor( XSComponent sc ) {
        if(!(sc instanceof XSElementDecl))  return false;
        
        XSElementDecl decl = (XSElementDecl)sc;
        if(!decl.getType().isSimpleType())  return false;
        
        return true;
    }
    
    private static final String[] reservedClassNames = new String[]{"ObjectFactory"};

    public void queueBuild( XSComponent sc, ClassItem ci ) {
        // it is an error if the same component is built twice,
        // or the association is modified.
        Binding b = new Binding(sc,ci);
        bindQueue.push(b);
        Object o = bindMap.put(sc, b);
        _assert( o==null );
    }
    
    /**
     * Sets up the context and builds the body of the specified ClassItem.
     */
    private void build( XSComponent sc, ClassItem ci ) {
        _assert( ((Binding)bindMap.get(sc)).ci==ci );
        
        for( int i=0; i<reservedClassNames.length; i++ ) {
            if( ci.getTypeAsDefined().name().equals(reservedClassNames[i]) ) {
                builder.errorReceiver.error( sc.getLocator(),
                    Messages.format(Messages.ERR_RESERVED_CLASS_NAME, reservedClassNames[i]) );
                break;
            }
        }
                
        
        addSchemaFragmentJavadoc(ci.getTypeAsDefined().javadoc(),sc);
        
        // set up the outer class to this class.
        // TODO: how to expose this feature?
        if( com.sun.tools.xjc.util.Util.getSystemProperty(this.getClass(),"nestedInterface")!=null )
            pushClassFactory( new PrefixedJClassFactoryImpl( builder, ci.getTypeAsDefined() ) );
        else
            // default
            pushClassFactory( new JClassFactoryImpl( this, ci.getTypeAsDefined() ) );
        
        ci.exp = builder.fieldBuilder.build(sc);

        ci.agm.exp = agmFragmentBuilder.build(sc,ci);
        
        popClassFactory();
        
        // acknowledge property customization on this schema component,
        // since it is OK to have a customization at the point of declaration
        // even when no one is using it.
        BIProperty prop = (BIProperty)builder.getBindInfo(sc).get(BIProperty.NAME);
        if(prop!=null)  prop.markAsAcknowledged();
        
        // when a class item is a choice content interface,
        // all the properties receive the isSet method.
        if(ci.hasGetContentMethod) {
            ci.exp.visit(new BGMWalker() {
                // with a content model like (A,A)|B, the same field
                // can show up multiple times.
                private Set visited = new HashSet();
                public Object onField(FieldItem item) {
                    if( visited.add(item) )
                        item.realization = IsSetFieldRenderer.createFactory(
                            item.realization, false, true );
                    return null;
                }

                public Object onSuper(SuperClassItem item) {
                    return null;
                }
            });
        }
    }
    
    /**
     * Copies a schema fragment into the javadoc of the generated class.
     */
    private void addSchemaFragmentJavadoc( JDocComment javadoc, XSComponent sc ) {
        BindInfo bi = builder.getBindInfo(sc);
        String doc = bi.getDocumentation();
        
        if(doc!=null && bi.hasTitleInDocumentation()/**otherwise add it later*/) {
            javadoc.appendComment(doc);
            javadoc.appendComment("\n");
        } 
        
        StringWriter out = new StringWriter();
        SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
        sc.visit(sw);
        
        Locator loc = sc.getLocator();
        String fileName = null;
        if(loc!=null) {
            fileName = loc.getPublicId();
            if(fileName==null)
                fileName = loc.getSystemId();
        }
        if(fileName==null)  fileName="";
        
        String lineNumber=Messages.format( Messages.JAVADOC_LINE_UNKNOWN);
        if(loc!=null && loc.getLineNumber()!=-1)
            lineNumber = String.valueOf(loc.getLineNumber());
        
        String componentName = 
            (String)sc.apply( new com.sun.xml.xsom.util.ComponentNameFunction() );
        javadoc.appendComment( 
            Messages.format( Messages.JAVADOC_HEADING,
                componentName, fileName, lineNumber ) );
        
        if(doc!=null && !bi.hasTitleInDocumentation()) {
            javadoc.appendComment("\n");
            javadoc.appendComment(doc);
            javadoc.appendComment("\n");
        } 
        
        javadoc.appendComment( "\n<p>\n<pre>\n" );
        javadoc.appendComment(out.getBuffer().toString());
        javadoc.appendComment( "</pre>" );
    }
    


    
    
    
        

    


    
    
    
    /**
     * Set of package names that are tested (set of <code>String</code>s.)
     * 
     * This set is used to avoid duplicating "incorrect package name"
     * errors.
     */
    private static Set checkedPackageNames = new HashSet();
    
    /**
     * Gets the Java package to which classes from
     * this namespace should go.
     * 
     * <p>
     * Usually, the getOuterClass method should be used
     * to determine where to put a class.
     */
    public JPackage getPackage(String targetNamespace) {
        XSSchema s = builder.schemas.getSchema(targetNamespace);
        
        BISchemaBinding sb = (BISchemaBinding)
            builder.getBindInfo(s).get(BISchemaBinding.NAME);
        
        String name = null;
        
        // "-p" takes precedence over everything else
        if( defaultPackageName != null )
            name = defaultPackageName;      
            
        // use the <jaxb:package> customization
        if( name == null && sb!=null && sb.getPackageName()!=null )
            name = sb.getPackageName();

        // generate the package name from the targetNamespace
        if( name == null )
            name = Util.getPackageNameFromNamespaceURI(
                targetNamespace, builder.getNameConverter() );
        
        // hardcode a package name because the code doesn't compile
        // if it generated into the default java package
        if( name == null )
            name = "generated"; // the last resort
        
        
        // check if the package name is a valid name.
        if( checkedPackageNames.add(name) ) {
            // this is the first time we hear about this package name.
            if( !JJavaName.isJavaPackageName(name) )
                // TODO: s.getLocator() is not very helpful.
                // ideally, we'd like to use the locator where this package name
                // comes from.
                builder.errorReceiver.error(s.getLocator(),
                    Messages.format(
                        Messages.ERR_INCORRECT_PACKAGE_NAME, targetNamespace, name ));
        }
        
        return builder.grammar.codeModel._package(name);
    }
    

    private static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
    
    
}
