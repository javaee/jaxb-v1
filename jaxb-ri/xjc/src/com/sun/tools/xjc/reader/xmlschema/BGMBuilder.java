/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.reader.xmlschema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Locator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.fmt.JTextFile;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.QnameValueType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.msv.util.StringPair;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.reader.annotator.DatatypeSimplifier;
import com.sun.tools.xjc.reader.annotator.FieldCollisionChecker;
import com.sun.tools.xjc.reader.annotator.HierarchyAnnotator;
import com.sun.tools.xjc.reader.annotator.RelationNormalizer;
import com.sun.tools.xjc.reader.annotator.SymbolSpaceTypeAssigner;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BISchemaBinding;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSerializable;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXSuperClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.xjc.reader.xmlschema.cs.ClassSelector;
import com.sun.tools.xjc.reader.xmlschema.ct.ComplexTypeFieldBuilder;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;

/**
 * Builds an annotated grammar from XSOM.
 * 
 * <p>
 * This class serves as the root context for all the relevant code.
 * IOW, all the classes that participate in building BGM will have
 * a reference to this class, to access things like error reporter.
 */
public class BGMBuilder implements AnnotatorController
{
    /**
     * Builds BGM from XSOM.
     * 
     * @return
     *      null if any error is encountered but the error handler
     *      didn't throw any exception.
     */
    public static AnnotatedGrammar build(
        XSSchemaSet schemas, JCodeModel codeModel,
        ErrorReceiver errorReceiver, String defPackage,
        boolean inExtensionMode ) {
        
        ErrorReceiverFilter erFilter = new ErrorReceiverFilter(errorReceiver);
        try {
            AnnotatedGrammar grammar = new BGMBuilder(schemas,codeModel,erFilter,defPackage,inExtensionMode)._build(schemas);
            if( erFilter.hadError())    return null;
            return grammar;
        } catch( AbortException e ) {
            return null;
        }
    }
    
    //
    // other satellite builder classes
    //
    public final ClassSelector selector;
    public final TypeBuilder typeBuilder;
    public final FieldBuilder fieldBuilder;
    public final ParticleBinder particleBinder;
    public final ComplexTypeFieldBuilder complexTypeBuilder;
    
    /**
     * QUICK HACK:
     * UGLY CODE:
     *      If a particle skips a global element and directly binds to
     *      its content, that particle will be stored into this set.
     */
    public final Set particlesWithGlobalElementSkip = new HashSet();
    
    /**
     * True if the compiler is running in the extension mode
     * (as opposed to the strict conformance mode.)
     */
    public final boolean inExtensionMode;
    
    
    
    
    private AnnotatedGrammar _build( XSSchemaSet schemas ) {
        // do the binding
        buildContents();
        buildTopLevelExp();
        selector.executeTasks();
        
        // additional error check
        reportUnusedCustomizations();
        
        // run annotator
        if( !errorReporter.hadError() ) {
            // TODO:
            // those code can run without keeping XSSchemaSet in memory,
            // so it should be moved to a place where the GC can reclaim
            // XSSchemaSet before those code are run.
            // If they are placed here, XSScheaSet cannot be reclaimed
            // and thus XJC requires more memory than necessary. 
            grammar.visit(new DatatypeSimplifier(grammar.getPool()));
            HierarchyAnnotator.annotate(grammar, this);
            SymbolSpaceTypeAssigner.assign(grammar, this);
            FieldCollisionChecker.check(grammar, this);
            RelationNormalizer.normalize(grammar, this);
        }
                
        return grammar;
    }
    
    /** List up all the global bindings. */
    private void promoteGlobalBindings() {
        // promote any global bindings in the schema
        for (Iterator itr = schemas.iterateSchema(); itr.hasNext();) {
            XSSchema s = (XSSchema) itr.next();
            BindInfo bi = getBindInfo(s);
            
            BIGlobalBinding gb = (BIGlobalBinding)bi.get(BIGlobalBinding.NAME);
            
            if(gb!=null && globalBinding==null) {
                globalBinding = gb;
                globalBinding.markAsAcknowledged();
            }
        }
        
        if( globalBinding==null ) {
            // no global customization is present.
            // use the default one
            globalBinding = new BIGlobalBinding( grammar.codeModel );
            BindInfo big = new BindInfo(null);
            big.addDecl(globalBinding);
            big.setOwner(this,null);
        }
            
        // check XJC extensions and realize them
        BIXSuperClass root = globalBinding.getSuperClassExtension();
        if(root!=null)
            grammar.rootClass = root.getRootClass();
        
        BIXSerializable serial = globalBinding.getSerializableExtension();
        if(serial!=null)
            grammar.serialVersionUID = new Long(serial.getUID());

        // obtain the name conversion mode
        nameConverter = globalBinding.getNameConverter();
        
        // attach global conversions to the appropriate simple types
        globalBinding.dispatchGlobalConversions(schemas);
    }
    
    /**
     * Global bindings.
     * 
     * The empty global binding is set as the default, so that
     * there will be no need to test if the value is null.
     */
    private BIGlobalBinding globalBinding;
    
    /**
     * Gets the global bindings.
     * @return
     *      Always return non-null valid object.
     */
    public BIGlobalBinding getGlobalBinding() { return globalBinding; }

    private NameConverter nameConverter;
    
    /**
     * Name converter that implements "XML->Java name conversion"
     * as specified in the spec.
     * 
     * This object abstracts the detail that we use different name
     * conversion depending on the customization.
     * 
     * <p>
     * This object should be used to perform any name conversion
     * needs, instead of the JJavaName class in CodeModel.
     */
    public NameConverter getNameConverter() { return nameConverter; }
    
    
    
    
    
    /** Fill-in the contents of each classes. */
    private void buildContents() {
        for (Iterator itr = schemas.iterateSchema(); itr.hasNext();) {
            XSSchema s = (XSSchema) itr.next();
            if(s.getTargetNamespace().equals(Const.XMLSchemaNSURI))
                continue;   // skip this built-in schema.
            
            checkMultipleSchemaBindings(s);
            processPackageJavadoc(s);
            populate(s.iterateAttGroupDecls());
            populate(s.iterateAttributeDecls());
            populate(s.iterateComplexTypes());
            populate(s.iterateElementDecls());
            populate(s.iterateModelGroupDecls());
            populate(s.iterateSimpleTypes());
        }
    }
    
    /** Reports an error if there are more than one jaxb:schemaBindings customization. */
    private void checkMultipleSchemaBindings( XSSchema schema ) {
        ArrayList locations = new ArrayList();
        
        BindInfo bi = getBindInfo(schema);
        for( int i=0; i<bi.size(); i++ ) {
            if( bi.get(i).getName()==BISchemaBinding.NAME )
                locations.add( bi.get(i).getLocation() );
        }
        if(locations.size()<=1)    return; // OK
        
        // error
        errorReporter.error( (Locator)locations.get(0),
            Messages.ERR_MULTIPLE_SCHEMA_BINDINGS,
            schema.getTargetNamespace() );
        for( int i=1; i<locations.size(); i++ )
            errorReporter.error( (Locator)locations.get(i),
                Messages.ERR_MULTIPLE_SCHEMA_BINDINGS_LOCATION);        
    }
    
    /**
     * Calls {@link ClassSelector} for each item in the iterator
     * to populate class items if there is any.
     */
    private void populate( Iterator itr ) {
        while(itr.hasNext()) {
            XSComponent sc = (XSComponent)itr.next();
            selector.bindToType(sc);
        }
    }
    
    /**
     * Generates <code>package.html</code> if the customization
     * says so.
     */
    private void processPackageJavadoc( XSSchema s ) {
        // look for the schema-wide customization
        BISchemaBinding cust = (BISchemaBinding)getBindInfo(s).get( BISchemaBinding.NAME );
        if(cust==null)      return; // not present
        
        if( cust.getJavadoc()==null )   return;     // no javadoc customization
        
        // produce a HTML file
        JTextFile html = new JTextFile("package.html");
        html.setContents(cust.getJavadoc());
        selector.getPackage(s.getTargetNamespace()).addResourceFile(html);
    }
    
    
    /** Computes the top-level expression. */
    private void buildTopLevelExp() {
        
        Expression top = Expression.nullSet;
        
        Iterator itr = schemas.iterateElementDecls();
        while(itr.hasNext()) {
            XSElementDecl decl = (XSElementDecl)itr.next();
            
            TypeItem ti = selector.bindToType(decl);
            if(ti instanceof ClassItem)
                top = pool.createChoice( top, ti );
        }
        
        if(top==Expression.nullSet)
            // no global element declaration was found.
            errorReporter.warning(null,Messages.WARN_NO_GLOBAL_ELEMENT,null);
        
        grammar.exp = top;
    }


    /**
     * Reports unused customizations to the user as errors.
     */
    private void reportUnusedCustomizations() {
        new UnusedCustomizationChecker(this).run();
    }
    
    
    
    /** The source XSOM, from which we are building BGM. */
    public final XSSchemaSet schemas;
    // TODO: should be protected
    
    public BGMBuilder( XSSchemaSet _schemas, JCodeModel codeModel,
        ErrorReceiver _errorReceiver, String defaultPackage,
        boolean _inExtensionMode ) {

        this.schemas = _schemas;
        this.inExtensionMode = _inExtensionMode;
        this.grammar = new AnnotatedGrammar(Expression.nullSet,pool,codeModel);
        this.errorReceiver = _errorReceiver;
        this.errorReporter = new ErrorReporter(_errorReceiver);
        this.simpleTypeBuilder = new SimpleTypeBuilder(this);
        
        this.typeBuilder = new TypeBuilder(this);
        this.fieldBuilder = new FieldBuilder(this);
        this.complexTypeBuilder = new ComplexTypeFieldBuilder(this);
        
        promoteGlobalBindings();

        this.selector = new ClassSelector(this,defaultPackage);
        
        // we'll use different binder based on the customization.
        if( getGlobalBinding().isModelGroupBinding() )
            this.particleBinder = new AlternativeParticleBinder(this);
        else
            this.particleBinder = new DefaultParticleBinder(this);

    }
    
    
    /**
     * Gets or creates the BindInfo object associated to a schema component.
     * 
     * @return
     *      Always return a non-null valid BindInfo object.
     *      Even if no declaration was specified, this method creates
     *      a new BindInfo so that new decls can be added.
     */
    public BindInfo getOrCreateBindInfo( XSComponent schemaComponent ) {
        
        BindInfo bi = _getBindInfoReadOnly(schemaComponent);
        if(bi!=null)    return bi;
        
        // XSOM is read-only, so we cannot add new annotations.
        // for components that didn't have annotations,
        // we maintain an external map.
        bi = new BindInfo(null);
        bi.setOwner(this,schemaComponent);
        externalBindInfos.put(schemaComponent,bi);
        return bi;
    }
    
    /**
     * Used as a constant instance to represent the empty {@link BindInfo}.
     */
    private final BindInfo emptyBindInfo = new BindInfo(null);
    

    /**
     * Gets the BindInfo object associated to a schema component.
     * 
     * @return
     *      always return a valid {@link BindInfo} object. If none
     *      is specified for the given component, a dummy empty BindInfo
     *      will be returned.
     */
    public BindInfo getBindInfo( XSComponent schemaComponent ) {
        BindInfo bi = _getBindInfoReadOnly(schemaComponent);
        if(bi!=null)    return bi;
        else            return emptyBindInfo;
    }
    
    /**
     * Gets the BindInfo object associated to a schema component.
     * 
     * @return
     *      null if no bind info is associated to this schema component.
     */
    private BindInfo _getBindInfoReadOnly( XSComponent schemaComponent ) {
        
        BindInfo bi = (BindInfo)externalBindInfos.get(schemaComponent);
        if(bi!=null)    return bi;
        
        XSAnnotation annon = schemaComponent.getAnnotation();
        if(annon!=null) {
            bi = (BindInfo)annon.getAnnotation();
            if(bi!=null) {
                if(bi.getOwner()==null)
                    bi.setOwner(this,schemaComponent);
                return bi;
            }
        }
        
        return null;
    }
    
    /**
     * A map from {@link XSComponent} to {@link BindInfo} that stores
     * binding declarations augmented by XJC.
     */
    private final Map externalBindInfos = new HashMap();
    // TODO: fill in values for built-in components.
    
    
    /** Used to format errors and warnings. */
    public final ErrorReporter errorReporter;
    /** All the errors are eventually passed to this handler. */
    public final ErrorReceiver errorReceiver;
    
    public final ExpressionPool pool = new ExpressionPool();
    public final AnnotatedGrammar grammar;
    
    public final SimpleTypeBuilder simpleTypeBuilder;

    
    

    







    



    /**
     * Wraps the specified expression with occurence modifiers
     * (such as OneOrMoreExp.)
     */
    public Expression processMinMax( Expression item, XSParticle p ) {
        return processMinMax( item, p.getMinOccurs(), p.getMaxOccurs() );
    }
    public Expression processMinMax( Expression item, int min, int max ) {
        
        Expression exp = Expression.epsilon;
        for( int i=0; i<min; i++ )
            exp = pool.createSequence( item, exp );
        
        if(max==-1) {
            if( min==1 )
                return pool.createOneOrMore(item);
            else {
                Expression exactExp = pool.createSequence( exp, pool.createZeroOrMore(item) );
                if( min<=1 )    return exactExp; 
                else            return new OccurrenceExp( exactExp, max, min, item ); 
            }
        } else {
            if(max==0)  // some people are crazy enough to put maxOccurs=0
                return Expression.nullSet;
            
            // create (A,(A, ... (A?)? ... )?
            Expression tmp = Expression.epsilon;
            for( int i=min; i<max; i++ )
                tmp = pool.createOptional( pool.createSequence( item, tmp ) );
            
            Expression exactExp = pool.createSequence(exp,tmp);
            
            if( max==1 )
                // the exact representation is simply enough that
                // there's no need of an indicator.
                return exactExp;
            
            return new OccurrenceExp( exactExp, max, min, item );
        }
    }
    
    interface ParticleHandler {
        Object particle( XSParticle p );
    }
    /**
     * Applies the specified function to each child particle, then
     * combine them by using an approrpiate binary expression.
     * 
     * @param   f
     *      This function must return an Expression object.
     *      (Maybe we should define an interface just for this method.)
     */
    protected Expression applyRecursively( XSModelGroup mg, ParticleHandler f ) {
        Expression[] exp = new Expression[mg.getSize()];
        for( int i=0; i<exp.length; i++ )
            exp[i] = (Expression)f.particle(mg.getChild(i));
        
        if(mg.getCompositor()==XSModelGroup.SEQUENCE) {
            Expression r = Expression.epsilon;
            for( int i=0; i<exp.length; i++ )
                r = pool.createSequence( r, exp[i] );
            return r;
        }
        if(mg.getCompositor()==XSModelGroup.ALL) {
            Expression r = Expression.epsilon;
            for( int i=0; i<exp.length; i++ )
                r = pool.createInterleave( r, exp[i] );
            return r;
        }
        if(mg.getCompositor()==XSModelGroup.CHOICE) {
            Expression r = Expression.nullSet;
            for( int i=0; i<exp.length; i++ )
                r = pool.createChoice( r, exp[i] );
            return r;
        }
        
        _assert(false);
        return null;
    }
    
    /**
     * Reusable <tt>attribute xsi:type {xsd:qname}</tt> expression.
     */
    private final Expression xsiTypeExp = pool.createOptional(
        pool.createAttribute(
            new SimpleNameClass(Const.XMLSchemaInstanceNSURI,"type"),
            pool.createData(QnameType.theInstance)));
    
    /**
     * Creates an expression that allows any xsi:type=QName.
     * <p>
     * The intention is to use this for elements with simple types
     * to ignore type substitution for simple types. 
     * <p>
     * This is technically too relaxing,
     * as it allows any QName, but it's a reasonable thing to do given
     * the effort it takes to correctly validate QName.
     * (if we want to do that later, we should probably define
     * our own datatype?)
     */
    public Expression createXsiTypeExp( XSElementDecl decl ) {
        return new IgnoreItem(xsiTypeExp,decl.getLocator());
    }
    
    
    /** Expression cache - {@link XSElementDecl} to {@link Expression}. */
    private final Map substitutionGroupCache = new HashMap();
    
    /**
     * Enumerates {@link XSElementDecl}s that can substitute the
     * given declaration and return the choice of all the classes
     * generated from those element decls,
     * but *excluding* the class generated from
     * <code>e</code> itself.
     * 
     * @param e
     *      A global element declaration.
     */
    public Expression getSubstitionGroupList( XSElementDecl e ) {
        Expression exp = (Expression)substitutionGroupCache.get(e);
        
        if(exp==null) {
            Set group = e.getSubstitutables();
            exp = Expression.nullSet;
            
            for( Iterator itr=group.iterator(); itr.hasNext(); ) {
                XSElementDecl decl = (XSElementDecl)itr.next();
                if( decl==e )   continue;   // exclude self
                if( decl.isAbstract() )     continue;   // exclude
                exp = pool.createChoice( exp, selector.bindToType(decl) );
            }
            
            substitutionGroupCache.put(e, exp );
        }
        
        return exp;
    }
    
    /**
     * Creates a choice of all (@xsi:type='xxx',xxx)
     * including ct itself.
     * 
     * @param strict
     *      if true, only the types allowed to substitute the given
     *      type will be listed. Otherwise, the result will be
     *      somwhat relaxed to allow invalid contents.
     *      (in particular it allows abstract types.)
     */
    public final Expression getTypeSubstitutionList( XSComplexType ct, boolean strict ) {
        
        if( !inExtensionMode ) {
            // in the strict conformance mode, we don't support the type substitution
            return Expression.nullSet;
        }
        
        Expression exp = Expression.nullSet;
        
        XSType[] group = ct.listSubstitutables();
        
        for( int i=0; i<group.length; i++ ) {
            if( strict && group[i].asComplexType().isAbstract() )
                continue;   // abstract type can't substitute 
            exp = pool.createChoice(
                    pool.createSequence(
                        pool.createAttribute(new SimpleNameClass(Const.XMLSchemaInstanceNSURI,"type"),
                            new IgnoreItem(
                                pool.createValue(QnameType.theInstance,
                                    new StringPair(Const.XMLSchemaNSURI,"qname"),
                                    new QnameValueType(
                                        group[i].getTargetNamespace(),
                                        group[i].getName() ) ), null )),
                                        selector.bindToType(group[i])
                    ), exp);
        }
        
        return exp;
    }
    
    
    
    
    
    
    
    
    
    private static void _assert( boolean b ) {
        if(!b)  throw new JAXBAssertionError();
    }


//
//
// AnnotationController implementation
//
//
    public PackageTracker getPackageTracker() {
        throw new JAXBAssertionError(); // shouldn't be called
    }

    public void reportError(Expression[] locations, String msg) {
        reportError( new Locator[0], msg );
    }

    public void reportError(Locator[] locations, String msg) {
        Locator loc = null;
        if(locations.length!=0) loc = locations[0];
        
        errorReceiver.error(loc,msg);
    }
    public ErrorReceiver getErrorReceiver() {
        return errorReceiver;
    }

}
