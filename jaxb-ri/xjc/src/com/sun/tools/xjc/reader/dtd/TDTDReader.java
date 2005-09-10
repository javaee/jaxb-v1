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
package com.sun.tools.xjc.reader.dtd;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.relaxng.datatype.DatatypeException;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.msv.datatype.xsd.DatatypeFactory;
import com.sun.msv.datatype.xsd.IDREFType;
import com.sun.msv.datatype.xsd.IDType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.reader.AbortException;
import com.sun.msv.reader.dtd.DTDReader;
import com.sun.msv.scanner.dtd.DTDParser;
import com.sun.msv.scanner.dtd.InputEntity;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.id.IDREFTransducer;
import com.sun.tools.xjc.grammar.id.IDTransducer;
import com.sun.tools.xjc.reader.GrammarReaderControllerAdaptor;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.PackageTracker;
import com.sun.tools.xjc.reader.annotator.Annotator;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.reader.annotator.FieldCollisionChecker;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIAttribute;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIContent;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIElement;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIInterface;
import com.sun.tools.xjc.reader.dtd.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Parses DTD grammar along with binding information into BGM.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class TDTDReader extends DTDReader implements AnnotatorController, PackageTracker
{
    /**
     * Parses DTD grammar and a binding information into BGM.
     * 
     * <p>
     * This method is just a utility method that covers 80% of the use
     * cases.
     * 
     * @param    bindingInfo
     *        binding information file, if any. Can be null.
     */
    public static AnnotatedGrammar parse(
        InputSource dtd,
        InputSource bindingInfo,
        ErrorReceiver errorReceiver,
        Options opts,
        ExpressionPool pool) {

        try {
            TDTDReader reader = new TDTDReader(
                new GrammarReaderControllerAdaptor(errorReceiver,opts.entityResolver),
                opts, pool, bindingInfo);

            DTDParser parser = new DTDParser();
            parser.setDtdHandler(reader);
            if( opts.entityResolver!=null )
            parser.setEntityResolver(opts.entityResolver);

            try {
                parser.parse(dtd);
            } catch (SAXParseException e) {
                return null; // this error was already handled by GrammarReaderController
            }

            return reader.getAnnotatedResult();
        } catch (IOException e) {
            errorReceiver.error(new SAXParseException(e.getMessage(),null,e));
            return null;
        } catch (SAXException e) {
            errorReceiver.error(new SAXParseException(e.getMessage(),null,e));
            return null;
        } catch (AbortException e) {
            // parsing was aborted but the error was already reported
            return null;
        }
    }
    protected TDTDReader(GrammarReaderControllerAdaptor _controller, Options opts, ExpressionPool pool, InputSource _bindInfo)
        throws AbortException {

        super(_controller, pool);
        this.opts = opts;
        bindInfo = new BindInfo(_bindInfo, _controller, codeModel, opts );
        this.errorReceiver = _controller;
        classFactory = new CodeModelClassFactory(errorReceiver);
    }

    /**
     * BGM.
     * Created in the endDTD method.
     */
    private AnnotatedGrammar annGrammar;
    
    private final Options opts;

    /**
     * binding information.
     * 
     * <p>
     * This is always non-null even if no binding information was specified.
     * (In that case, a dummy object will be provided.)
     */
    private final BindInfo bindInfo;

    private final JCodeModel codeModel = new JCodeModel();
    
    private final CodeModelClassFactory classFactory;
    
    private final ErrorReceiver errorReceiver;
    
    /** Gets the BGM parsed by this parser. */
    public AnnotatedGrammar getAnnotatedResult() {
        if (controller.hadError())
            return null;
        else
            return annGrammar;
    }

    public void startDTD(InputEntity entity) throws SAXException {
        super.startDTD(entity);

        annGrammar = new AnnotatedGrammar(grammar, codeModel);
    }
    
    public void endDTD() throws SAXException {
        super.endDTD();

        // if there was an error, abort.
        if (controller.hadError())
            return;

        resetStartPattern();

        processInterfaceDeclarations();

        // updates the topLevel pattern. This value was
        // changed between startDTD and endDTD.
        annGrammar.exp = grammar.getTopLevel();

        // check XJC extensions and realize them
        annGrammar.serialVersionUID = bindInfo.getSerialVersionUID();
        annGrammar.rootClass = bindInfo.getSuperClass();
        
        // performs annotation
        Annotator.annotate(annGrammar, this);
        FieldCollisionChecker.check( annGrammar, this );
        
        
        processConstructorDeclarations();
    }
    
    /**
     * Updates the start pattern so that only elements
     * designated as root are listed.
     */
    private void resetStartPattern() {
        Expression exp = Expression.nullSet;
        
        Iterator itr = bindInfo.elements();
        while(itr.hasNext()) {
            final BIElement e = (BIElement)itr.next();
            
            if( !e.isRoot() )    continue;
            
            ReferenceExp rexp =    grammar.namedPatterns.getOrCreate(e.name());
            if(!rexp.isDefined()) {
                // there is no such element.
                error( e.getSourceLocation(),
                    Messages.ERR_UNDEFINED_ELEMENT_IN_BINDINFO, e.name() );
                continue;
            }
            // add it to the choice.
            exp = grammar.pool.createChoice( exp, rexp );
        }
        
        if( exp==Expression.nullSet ) {
            // there was no root element.
            // we can either throw an error, or make all elements root.
/*
            error( null, ERR_NO_ROOT_ELEMENT );
*/        } else {
            // update the start pattern
            grammar.exp = exp;
        }
    }
    
    /** Processes interface declarations. */
    private void processInterfaceDeclarations() {
        Iterator itr;
        
        // first, create empty InterfaceItem declaration for all interfaces
        Map decls = new java.util.HashMap();
        itr = bindInfo.interfaces();
        while( itr.hasNext() ) {
            BIInterface decl = (BIInterface)itr.next();
            
            decls.put( decl, annGrammar.createInterfaceItem(
                classFactory.createInterface(
                    getTargetPackage(),
                    decl.name(),
                    copyLocator() ),
                Expression.nullSet,
                copyLocator() ) );
        }
        
        // create a map from unqualified class names to ClassItem/InterfaceItem
        // we cannot move this feature to AnnotatedGrammar because
        // AnnotatedGrammar in general may have classes in more than one package.
        Map fromName = new java.util.HashMap();
        
        itr = annGrammar.iterateClasses();
        while( itr.hasNext() ) {
            ClassItem ci = (ClassItem)itr.next();
            fromName.put( ci.getTypeAsDefined().name(), ci );
        }
        itr = annGrammar.iterateInterfaces();
        while( itr.hasNext() ) {
            InterfaceItem itf = (InterfaceItem)itr.next();
            fromName.put( itf.getTypeAsClass().name(), itf );
        }
        
        // traverse the interface declarations again
        // and populate its expression according to the members attribute.
        itr = decls.entrySet().iterator();
        while( itr.hasNext() ) {
            Map.Entry e = (Map.Entry)itr.next();
            BIInterface decl = (BIInterface)e.getKey();
            InterfaceItem item = (InterfaceItem)e.getValue();
            
            String[] members = decl.members();
            for( int i=0; i<members.length; i++ ) {
                Expression exp = (Expression)fromName.get(members[i]);
                if(exp==null) {
                    // there is no such class/interface
                    // TODO: error location
                    error(
                        decl.getSourceLocation(),
                        Messages.ERR_BINDINFO_NON_EXISTENT_INTERFACE_MEMBER,
                        members[i] );
                    continue;
                }
                
                item.exp = annGrammar.getPool().createChoice(
                    item.exp, exp );
            }
        }
        
        // TODO: check the cyclic interface definition
    }
    
    private JPackage getTargetPackage() {
        // "-p" takes precedence over everything else
        if(opts.defaultPackage!=null)
            return codeModel._package(opts.defaultPackage);
        else
            return bindInfo.getTargetPackage(); 
    }
    
    
    /**
     * Creates constructor declarations as specified in the
     * binding information.
     * 
     * <p>
     * Also checks that the binding file does not contain
     * declarations for non-existent elements.
     */
    private void processConstructorDeclarations() {
        Iterator itr = bindInfo.elements();
        while( itr.hasNext() ) {
            BIElement decl = (BIElement)itr.next();
            ReferenceExp rexp = grammar.namedPatterns._get(decl.name());
            if(rexp==null) {
                error(decl.getSourceLocation(),
                    Messages.ERR_BINDINFO_NON_EXISTENT_ELEMENT_DECLARATION,decl.name());
                continue;   // continue to process next declaration
            }
            
            if(!decl.isClass())
                // only element-class declaration has constructor definitions
                continue;
            
            // if this element is designated as a class item,
            // ReferenceExp must directly contain a ClassItem.
            _assert(rexp.exp instanceof ClassItem);
            
            ClassItem ci = (ClassItem)rexp.exp;
            
            decl.declareConstructors(ci,this);
        }
    }
        
    
    protected Expression createAttributeBody(
        String elementName, String attributeName, String attributeType,
        String[] enums, short attributeUse, String defaultValue )
            throws SAXException {
        
        Expression exp = super.createAttributeBody(
            elementName, attributeName, attributeType, enums,
            attributeUse, defaultValue );
        
        // get the attribute-property declaration
        BIElement edecl = bindInfo.element(elementName);
        BIAttribute decl=null;
        if(edecl!=null)     decl=edecl.attribute(attributeName);
        
        // interaction between the ID attribute and
        // binding schema seems unclear to me
        // 
        // what if a binding file specifies a different transducer
        // for IDs?
        // 
        // right now, in this implementation, the binding file
        // takes precedence.
        // 
        // -kk
        
        if(decl!=null) {
            // if there is an attribute-declaration,
            // use that information and wrap the body.
            
            // wrap the datatype by PrimitiveItem, if the conversion
            // is specified. Note that if the datatype is compound
            // (like NMTOKENS), the whole attribute value will be
            // passed to the transducer. This behavior will work fine
            // for DatabindableXducers.
            //
            // if the conversion is not specified, the normalizer
            // will be responsible to handle the datatype.
            final BIConversion conv = decl.getConversion();
            if(conv!=null)
                exp = annGrammar.createPrimitiveItem( conv.getTransducer(),
                    StringType.theInstance, // accept anything
                    exp, copyLocator() );
            
            // finally wrap the whole expression by a FieldItem.
            FieldItem fi = new FieldItem( decl.getPropertyName(), exp, copyLocator() );
            fi.realization = decl.getRealization();
            exp = fi;
            
        } else {
            // handle ID related attributes.
            // we need special transducers for them.
            if(attributeType.equals("ID"))
                exp = annGrammar.createPrimitiveItem(
                    new IDTransducer(codeModel,annGrammar.defaultSymbolSpace),
                    IDType.theInstance,
                    exp, copyLocator() );
        
            if(attributeType.equals("IDREF"))
                exp = annGrammar.createPrimitiveItem(
                    new IDREFTransducer(codeModel,annGrammar.defaultSymbolSpace,true),
                    IDREFType.theInstance,
                    exp, copyLocator() );
            
            if(attributeType.equals("IDREFS"))
                try {
                    exp = grammar.pool.createList(
                        grammar.pool.createOneOrMore( annGrammar.createPrimitiveItem(
                            new IDREFTransducer(codeModel,annGrammar.defaultSymbolSpace,false),
                            DatatypeFactory.getTypeByName("IDREFS"),
                            grammar.pool.createData(IDREFType.theInstance),
                            copyLocator())));
                } catch( DatatypeException e ) {
                    // this can never happen
                    e.printStackTrace();
                    throw new JAXBAssertionError();
                }
            
            // if no declaration is specified, just wrap it by
            // a FieldItem and let the normalizer handle its content.
            exp = new FieldItem( NameConverter.standard.toPropertyName(attributeName), exp, copyLocator() );
        }
        
        return exp;
    }    
    
    
    
    protected ReferenceExp createElementDeclaration( String elementName ) {

        final BIElement decl = bindInfo.element(elementName);

        // obtain the location of this element decl.
        Locator loc =getDeclaredLocationOf( grammar.namedPatterns.getOrCreate(elementName) );
        
        if(decl==null || decl.isClass()) {
            // this element is a class element
            
            // if the content-property declaration is there,
            // perform the annotation before
            // the super.createElementDecleration method adds
            // attributes.
            elementDecls.put(elementName,
                performContentAnnotation( elementName, decl,
                (Expression)elementDecls.get(elementName), loc ));
        }
        
        
        
        final ReferenceExp exp = super.createElementDeclaration( elementName );
        // its immediate child must be an ElementExp.
        final ElementExp eexp = (ElementExp)exp.exp;
    
        if(decl==null) {
            // none was specified in the document.
            // infer the name and make a temporary one.

                exp.exp = new ClassCandidateItem(
                    classFactory,
                    annGrammar,
                    getTargetPackage(),
                    getNameConverter().toClassName(elementName),
                    loc,
                    eexp );
        } else
        if(decl.isClass()) {
            // gets the class name.
            ClassItem t = annGrammar.createClassItem(decl.getClassObject(),eexp,loc);
            setDeclaredLocationOf(t);
            exp.exp = t;
        } else {
            // this element is specified as a value element.
            
            // make sure that the content model is in fact simple.
            if( eexp.contentModel!=Expression.anyString )
                error( eexp, Messages.ERR_CONVERSION_FOR_NON_VALUE_ELEMENT, elementName );    
            
            // wrap it by a primitive type
            BIConversion cnv = decl.getConversion();
            if(cnv!=null) {
                // the conversion is specified, use it.
                PrimitiveItem pi = annGrammar.createPrimitiveItem(
                    cnv.getTransducer(),
                    StringType.theInstance, // no guard clause
                    eexp, loc );
                exp.exp = pi;
            } else {
                // no conversion is specified. Convert it to a plain string.
                PrimitiveItem pi = annGrammar.createPrimitiveItem(
                    codeModel,
                    com.sun.msv.datatype.xsd.StringType.theInstance,
                    eexp, loc );
                exp.exp = pi;
            }
        }
        
        return exp;
    }
    
    /**
     * Annotates the given element according
     * to the content-property declaration specified in the decl.
     * 
     * @param   decl
     *      This can be null.
     * @param   loc
     *      This location should be used rather than {@link #locator}.
     */
    private Expression performContentAnnotation( String elementName, BIElement decl, Expression exp, Locator loc ) {
        
        // the root sequence of the content model.
        Expression[] children;
        
        if( exp == Expression.anyString && decl==null )
            // if the content model is just #PCDATA, it will
            // become a value.
            return exp;
        
        if( exp==Expression.epsilon )
            return exp; // no child item
            
        if(exp instanceof SequenceExp)
            // the root is in fact a sequence
            children = ((SequenceExp)exp).getChildren();
        else
            children = new Expression[]{exp};
        
        int idx=0;
        // updated content model
        Expression newContentModel = Expression.epsilon;
        
        if( decl!=null ) {
            Iterator itr = decl.iterateContents();
        
            while( itr.hasNext() ) {
                try {
                    BIContent bic = (BIContent)itr.next();
                
                    if(idx==children.length) {
                        // content-property declaration is longer than
                        // the actual content model.
                        throw new BIContent.MismatchException();
                    }
                
                    // wrap the child with FieldItem and combine it into
                    // the new content model.
                    newContentModel = grammar.pool.createSequence(
                        newContentModel, bic.wrap(children[idx]));
                    idx++;
                } catch( BIContent.MismatchException mme ) {
                    error( exp, Messages.ERR_CONTENT_PROPERTY_PARTICLE_MISMATCH,
                        elementName );
                }
            }
        }
        
        BIContent restDecl = (decl!=null) ? decl.getRest() : null;
        if( restDecl!=null ) {
            // the rest of the content model will be wrapped
            // as one particle.
            Expression rest = Expression.epsilon;
            while(idx<children.length)
                rest = grammar.pool.createSequence( rest, children[idx++] );
            
            // apply a FieldItem.
            FieldItem fi = new FieldItem( restDecl.getPropertyName(),
                                    rest, restDecl.getType(), loc );
            fi.realization = restDecl.getRealization();
            rest = fi;
            
            newContentModel = grammar.pool.createSequence(newContentModel,rest);
        } else {
            // the current situation:
            //   1. there was no content-property declaration or
            //   2. content-property declaration was there but
            //      it's too short.
            
            // children with no content-property declaration must be
            // element declarations.
            int i;
            for( i=idx; i<children.length; i++ ) {
                Expression item = children[i].peelOccurence(); 
                if(!( item instanceof ReferenceExp) || item==getAnyExp() )
                    // something other than an element declaration was found.
                    break;
            }
            
            if( i!=children.length ) {
                // this content model is complex.
                // assume <content property="content"/>
                // if there was no content-property declaration
                if(idx==0) {
                    newContentModel = new FieldItem("Content", exp, loc );
                } else {
                    // content-property declaration is too short.
                    error( exp, Messages.ERR_CONTENT_PROPERTY_DECLARATION_TOO_SHORT,
                        elementName );
                    return Expression.nullSet;
                }
            } else {
                // all the children left are simple.
                // wrap them
                for( i=idx; i<children.length; i++ )
                    newContentModel = grammar.pool.createSequence(
                        newContentModel,
                        new FieldItem( 
                            NameConverter.standard.toPropertyName(
                                ((ReferenceExp)children[i].peelOccurence()).name),
                            children[i], loc ) );
            }
        }
        
        return newContentModel;
    }
    
    /**
     * Creates a snapshot of the current {@link #locator} values.
     */
    private Locator copyLocator(){
        return new LocatorImpl(locator);
    }

//
//
// PackageTracker implementation
//
//
    public JPackage get( ReferenceExp exp ) {
        // DTD only uses only Java package
        return getTargetPackage();
    }
    
//
//
// AnnotatorController implementation
//
//
    public NameConverter getNameConverter() {
        return NameConverter.standard;
    }
    
    public PackageTracker getPackageTracker() {
        return this;
    }
    
    public void reportError( Expression[] srcs, String msg ) {

        Vector vec = new Vector();
        for( int i=0; i<srcs.length; i++ ) {
            Locator loc = getDeclaredLocationOf(srcs[i]);
            if(loc!=null)   vec.add(loc);
        }
        reportError( (Locator[])vec.toArray(new Locator[0]), msg );
    }
    public void reportError( Locator[] locs, String msg ) {
        controller.error( locs, msg, null );
    }
    public ErrorReceiver getErrorReceiver() {
        return errorReceiver;
    }
    

//
//
// error related utility methods
//
//
    protected final void error( Expression loc, String prop ) {
        error( loc, prop, null );
    }
    protected final void error( Expression loc, String prop, Object arg1 ) {
        error( loc, prop, new Object[]{arg1} );
    }
    protected final void error( Expression loc, String prop, Object[] args ) {
        reportError( new Expression[]{loc},
            Messages.format(prop,args) );
    }
    protected final void error( Locator loc, String prop, Object arg1 ) {
        error( loc, prop, new Object[]{arg1} );
    }
    protected final void error( Locator loc, String prop, Object[] args ) {
        reportError( new Locator[]{loc},
            Messages.format(prop,args) );
    }
    
    
    
    private static void _assert( boolean b ) {
        if(!b)  throw new JAXBAssertionError();
    }
    
}
