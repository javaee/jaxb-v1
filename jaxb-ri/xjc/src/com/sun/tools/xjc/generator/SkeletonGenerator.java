/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.fmt.JStaticFile;
import com.sun.codemodel.fmt.JStaticJavaFile;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.cls.ImplStructureStrategy;
import com.sun.tools.xjc.generator.cls.PararellStructureStrategy;
import com.sun.tools.xjc.generator.field.DefaultFieldRendererFactory;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.JAXBObject;
import com.sun.xml.bind.RIElement;

/**
 * Generates fields and accessors.
 */
public class SkeletonGenerator implements GeneratorContext
{
    /** Simplifies class/interface creation and collision detection. */
    private final CodeModelClassFactory codeModelClassFactory;
    
    private final ErrorReceiver errorReceiver;
    
    private final Options opts;

    /** map from JPackage to corresponding {@link PackageContext}. */
    private final Map packageContexts = new HashMap();
    
    /** map from {@link ClassItem} to {@link ClassContext}. */
    private final Map classContexts = new HashMap();
    
    /** the grammar object which we are processing. */
    private final AnnotatedGrammar grammar;
    
    private final JCodeModel codeModel;
    
    
    /** Map from Class objects to JClass objects for runtime. */
    private final Map runtimeClasses = new HashMap();
    
    /**
     * map from FieldUse to the corresponding JVar.
     * this map is prepared by the {@link #generateFieldDecl()} method.
     */
    private final Map fields = new java.util.HashMap();
    
    
    // satellites 
    private final LookupTableBuilder lookupTableBuilder;
    
    
    /**
     * Generates fields and accessors into Code Model
     * according to the BGM.
     * 
     * @param _errorReceiver
     *      This object will receive all the errors discovered
     *      during the back-end stage.
     * 
     * @return
     *      returns a {@link GeneratorContext} which will in turn
     *      be used to further generate marshaller/unmarshaller,
     *      or null if the processing fails (errors should have been
     *      reported to the error recevier.)
     */
    public static GeneratorContext generate(
        AnnotatedGrammar grammar, Options opt, ErrorReceiver _errorReceiver ) {
        
        try {
            return new SkeletonGenerator(grammar,opt,_errorReceiver);
        } catch( AbortException e ) {
            return null;
        }
    }
    
    
    private SkeletonGenerator(
        AnnotatedGrammar _grammar, Options opt, ErrorReceiver _errorReceiver ) {
        
        this.grammar = _grammar;
        this.opts = opt;
        this.codeModel = grammar.codeModel;
        this.errorReceiver = _errorReceiver;
        this.codeModelClassFactory = new CodeModelClassFactory(errorReceiver);
        
        // call the populate method of transducers.
        populateTransducers(grammar);
        
        // generate static runtime classes
        generateStaticRuntime();
        
        JPackage[] packages = grammar.getUsedPackages();

        if( packages.length!=0 )
            this.lookupTableBuilder =
                new LookupTableCache(new LookupTableInterner(new LookupTableFactory(
                    packages[0].subPackage("impl")
                )));
        else
            this.lookupTableBuilder = null;
        
        // generates per-package code and remember the results as contexts.
        for( int i=0; i<packages.length; i++ ) {
            JPackage pkg = packages[i];
            packageContexts.put(
                pkg,
                new PackageContext(this,grammar,opt,pkg));
        }
        
        ClassItem[] items = grammar.getClasses();
        
        // create class context
        ImplStructureStrategy strategy = new PararellStructureStrategy(codeModelClassFactory);
        for( int i=0; i<items.length; i++ )
            classContexts.put( items[i], new ClassContext(this,strategy,items[i]) );
        
        // fill in implementation classes (has to be done after all class contexts are ready)
        for( int i=0; i<items.length; i++ )
            generateClass(getClassContext(items[i])); 
        
        // things that have to be done after all the skeletons are generated
        for( int i=0; i<items.length; i++ ) {
            ClassContext cc = getClassContext(items[i]);
            
            // setup inheritance between implementation hierarchy.
            ClassItem superClass = cc.target.getSuperClass();
            if(superClass!=null) {
                // use the specified super class
                cc.implClass._extends(getClassContext(superClass).implRef);
            } else {
                // use the default one, if any
                if( grammar.rootClass!=null )
                    cc.implClass._extends(grammar.rootClass);
            }
            
            // delegation. this has to be done after all the other methods
            // are generated properly.
            FieldUse[] fus = items[i].getDeclaredFieldUses();
            for( int j=0; j<fus.length; j++ )
                if( fus[j].isDelegated() )
                    generateDelegation(
                        items[i].locator,
                        cc.implClass,
                        (JClass)fus[j].type, getField(fus[j]) );
        }
    }
    
    
    public AnnotatedGrammar getGrammar() {
        return grammar;
    }

    public JCodeModel getCodeModel() {
        return codeModel;
    }
    
    public LookupTableBuilder getLookupTableBuilder() {
        return lookupTableBuilder;
    }

    
    /**
     * Returns the package in which the runtime will live.
     * 
     * @return null
     *      if no code is generated from this package.
     */
    private JPackage getRuntimePackage() {
        if( opts.runtimePackage!=null )
            // use the user-specified one.
            return codeModel._package(opts.runtimePackage);
        
        JPackage[] pkgs = grammar.getUsedPackages();
        if( pkgs.length==0 )    return null;
        
        JPackage pkg = pkgs[0]; 
        if( pkg.name().startsWith( "org.w3") && pkgs.length > 1 ) {
            pkg = grammar.getUsedPackages()[1];
        }
        return pkg.subPackage("impl.runtime");
    }
    
    /**
     * Generates static runtime classes.
     * <p>
     * This method adds most of the classes in the
     * <tt>com.sun.tools.xjc.runtime</tt> package into the CodeModel.
     */
    private void generateStaticRuntime() {
        if( !opts.generateRuntime )
            return;     // don't generate the runtime. just skip.
            
        final JPackage pkg = getRuntimePackage();
        final String prefix = "com/sun/tools/xjc/runtime/";
        
        if( pkg==null ) return; // no code generation
        
        BufferedReader r = new BufferedReader(new InputStreamReader(
            this.getClass().getClassLoader().getResourceAsStream(prefix+"filelist")));
        String line;
        try {
            while((line=r.readLine())!=null) {
                if(line.startsWith("#"))    continue;   // comment
                
                String name = line.substring(12);
                boolean forU = line.charAt( 2)=='x';
                boolean forW = line.charAt( 4)=='x';
                boolean forM = line.charAt( 6)=='x';
                boolean forV = line.charAt( 8)=='x';
                boolean must = line.charAt(10)=='x';
                
                if( must
                || (forU && opts.generateUnmarshallingCode )
                || (forW && opts.generateValidatingUnmarshallingCode )
                || (forM && opts.generateMarshallingCode )
                || (forV && opts.generateValidationCode ) ) {
                    // this file has to be generated
                    if( name.endsWith(".java") ) {
                        String className = name.substring(0,name.length()-5);
                        Class cls = Class.forName(prefix.replace('/','.')+className);
                        
                        addRuntime(cls);            
                    } else {
                        JStaticFile s = new JStaticFile(prefix+name);
                        pkg.addResourceFile(s);
                    }
                }
            }
        
        // impossible
        } catch (IOException e) {
            e.printStackTrace();
            throw new JAXBAssertionError();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new JAXBAssertionError();
        }
    }
    
    public JClass getRuntime( Class clazz ) {
        JClass r = (JClass)runtimeClasses.get(clazz);
        if(r!=null)     return r;
        return addRuntime(clazz);
    }

    /**
     * Adds a new class to the runtime package.
     * This method can be used to introduce new runtime class
     * when it is necessary.
     * 
     * One can safely call this method multiple times with the same parameter.
     * 
     * @return
     *      Return <code>getRuntime(runtimeClass)</code>
     */
    private JClass addRuntime( Class runtimeClass ) {
        final JPackage pkg = getRuntimePackage();
        String shortName = getShortName(runtimeClass.getName());
        
        if( !pkg.hasResourceFile(shortName+".java") ) {
            URL res = runtimeClass.getResource(shortName+".java");
            if(res==null)
                throw new JAXBAssertionError("Unable to load source code of "+runtimeClass.getName()+" as a resource");
            
            JStaticJavaFile sjf = new JStaticJavaFile(pkg,shortName,
                    res, new PreProcessor() );
            if( opts.generateRuntime )  // generate it only when so instructed.
                pkg.addResourceFile(sjf);
            runtimeClasses.put( runtimeClass, sjf.getJClass() );            
        }
        
        return getRuntime(runtimeClass);
    }
    
    private class PreProcessor extends PreProcessingLineFilter {
        protected boolean getVar(char variableName) throws ParseException {
            switch(variableName) {
            case 'U':   return opts.generateUnmarshallingCode;
            case 'V':   return opts.generateValidationCode;
            case 'M':   return opts.generateMarshallingCode;
            case 'W':   return opts.generateValidatingUnmarshallingCode;
            default:
                throw new ParseException("undefined variable "+variableName,-1);
            }
        }
    };
    
    
    private String getShortName( String name ) {
        return name.substring(name.lastIndexOf('.')+1);
    }
    
    public ErrorReceiver getErrorReceiver() { return errorReceiver; }
    
    public CodeModelClassFactory getClassFactory() { return codeModelClassFactory; }

    public PackageContext getPackageContext( JPackage p ) {
        return (PackageContext)packageContexts.get(p);
    }

    public ClassContext getClassContext( ClassItem ci ) {
        return (ClassContext)classContexts.get(ci);
    }
    
    public PackageContext[] getAllPackageContexts() {
        return (PackageContext[]) packageContexts.values().toArray(
            new PackageContext[packageContexts.size()]);
    }
    
    
    public FieldRenderer getField( FieldUse fu ) {
        return (FieldRenderer)fields.get(fu);
    }
    
    /**
     * Generates the body of a class.
     * 
     */
    private void generateClass( ClassContext cc ) {


        // if serialization support is turned on, generate
        // [RESULT]
        // class ... implements Serializable {
        //     private static final long serialVersionUID = <id>;
        //     ....
        // }
        if( grammar.serialVersionUID!=null ) {
            cc.implClass._implements(Serializable.class);
            cc.implClass.field(
                JMod.PRIVATE|JMod.STATIC|JMod.FINAL,
                codeModel.LONG,
                "serialVersionUID",
                JExpr.lit(grammar.serialVersionUID.longValue()));
        }


        if(cc.target.exp instanceof NameClassAndExpression) {
            // generate code necessary to store element/attribute name
            XmlNameStoreAlgorithm nsa = XmlNameStoreAlgorithm.get((NameClassAndExpression)cc.target.exp);
                    
            nsa.populate(cc);
        
            if(cc.target.exp instanceof ElementExp ) {
                // implement the RIElement interface
                cc.implClass._implements(RIElement.class);
                
                cc.implClass.method( JMod.PUBLIC, String.class, "____jaxb_ri____getNamespaceURI")
                    .body()._return(nsa.getNamespaceURI());
                cc.implClass.method( JMod.PUBLIC, String.class, "____jaxb_ri____getLocalName")
                    .body()._return(nsa.getLocalPart());
            }
        }
        cc.implClass._implements(JAXBObject.class);

            
        FieldUse[] fus = cc.target.getDeclaredFieldUses();
        for( int j=0; j<fus.length; j++ )
            generateFieldDecl(cc,fus[j]);
            
        if( cc.target.hasGetContentMethod )
            generateChoiceContentField(cc);
            
            
        cc._package.objectFactoryGenerator.populate(cc);
                
        // generate version fields in the impl class
        cc._package.versionGenerator.generateVersionReference( cc );
    }

    /**
     * Generate the getContent method that returns the currently set field.
     */
    private void generateChoiceContentField( ClassContext cc ) {
        final FieldUse[] fus = cc.target.getDeclaredFieldUses();
        
        // find the common base type of all fields
        JType[] types = new JType[fus.length];
        for( int i=0; i<fus.length; i++ ) {
            FieldRenderer fr = getField(fus[i]);
            types[i] = fr.getValueType();
        }
        JType returnType = TypeUtil.getCommonBaseType(codeModel,types);
        
        
        // [RESULT]
        // <RETTYPE> getContent()
        MethodWriter helper = cc.createMethodWriter();
        JMethod $get = helper.declareMethod(returnType,"getContent");
        
        for( int i=0; i<fus.length; i++ ) {
            FieldRenderer fr = getField(fus[i]);
            
            // [RESULT]
            // if( <hasSetValue>() )
            //    return <get>();
            JBlock then = $get.body()._if( fr.hasSetValue() )._then();
            then._return(fr.getValue());
        }
        
        $get.body()._return(JExpr._null());
        

        
        // [RESULT]
        // boolean isSetContent()
        JMethod $isSet = helper.declareMethod(codeModel.BOOLEAN,"isSetContent");
        JExpression exp = JExpr.FALSE;
        for( int i=0; i<fus.length; i++ ) {
            exp = exp.cor(getField(fus[i]).hasSetValue());
        }
        $isSet.body()._return(exp);
        
        // [RESULT]
        // void unsetContent()
        JMethod $unset = helper.declareMethod(codeModel.VOID,"unsetContent");
        for( int i=0; i<fus.length; i++ )
            getField(fus[i]).unsetValues($unset.body());

        
        // install onSet hooks to realize
        // "set one field to unset everything else" semantics.
        for( int i=0; i<fus.length; i++ ) {
            FieldRenderer fr1 = getField(fus[i]);
            for( int j=0; j<fus.length; j++ ) {
                if(i==j)    continue;
                FieldRenderer fr2 = getField(fus[j]);
                fr2.unsetValues(fr1.getOnSetEventHandler());
            }
        }
    }
    
    
    private void generateDelegation( Locator errorSource, JDefinedClass impl, JClass _intf, FieldRenderer fr ) {
        // TODO: this casting is just a quick hack
        JDefinedClass intf = (JDefinedClass)_intf;
        
        for (Iterator itr = intf._implements(); itr.hasNext();) {
            generateDelegation( errorSource, impl, (JClass)itr.next(), fr );
        }
        
        for (Iterator itr = intf.methods(); itr.hasNext();) {
            JMethod m = (JMethod) itr.next();
            
            // make sure that there's no corriding method
            if( impl.getMethod( m.name(), m.listParamTypes() )!= null ) {
                errorReceiver.error( errorSource, Messages.format(
                    Messages.METHOD_COLLISION, m.name(), impl.fullName(), intf.fullName() ));
            }
            
            JMethod n = impl.method( JMod.PUBLIC, m.type(), m.name() );
            JVar[] mp = m.listParams();

            JInvocation inv = fr.getValue().invoke(m);
            
            if( m.type()==codeModel.VOID )
                n.body().add(inv);
            else
                n.body()._return( inv );
            
            for( int j=0; j<mp.length; j++ )
                inv.arg( n.param( mp[j].type(), mp[j].name() ) );
        }
    }
    
    
    
    
    
    /**
     * Calls the populate method of all transducers in this grammar.
     */
    private void populateTransducers( final AnnotatedGrammar grammar ) {
        PrimitiveItem[] pis = grammar.getPrimitives();
        for( int i=0; i<pis.length; i++ ) {
            pis[i].xducer.populate(grammar, this );
        }
    }
    
    
    
    
    
    
    /**
     * Determines the FieldRenderer used for the given FieldUse,
     * then generates the field declaration and accessor methods.
     * 
     * The <code>fields</code> map will be updated with the newly
     * created FieldRenderer.
     */
    private FieldRenderer generateFieldDecl( ClassContext cc, FieldUse fu ) {
        FieldRenderer field;
        
        FieldRendererFactory frf = fu.getRealization();
        if(frf==null)
            // none is specified. use the default factory
            frf = new DefaultFieldRendererFactory(codeModel);

        field = frf.create(cc,fu);
        field.generate();
        fields.put(fu,field);
       
           return field;
    }
}
