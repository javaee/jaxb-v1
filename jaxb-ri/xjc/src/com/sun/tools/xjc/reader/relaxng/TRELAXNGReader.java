/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.relaxng;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.sun.msv.grammar.Expression;
import com.sun.msv.reader.State;
import com.sun.msv.reader.trex.ng.RELAXNGReader;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.GrammarReaderControllerAdaptor;
import com.sun.tools.xjc.reader.HierarchicalPackageTracker;
import com.sun.tools.xjc.reader.StackPackageManager;
import com.sun.tools.xjc.reader.annotator.Annotator;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.reader.annotator.AnnotatorControllerImpl;
import com.sun.tools.xjc.reader.decorator.RoleBasedDecorator;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * parses Tahiti-annotated RELAX NG grammar.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class TRELAXNGReader extends RELAXNGReader {

    public TRELAXNGReader(
        ErrorReceiver errorReceiver,
        EntityResolver entityResolver,
        SAXParserFactory parserFactory, String defaultPackage) {
            
        this(
            new GrammarReaderControllerAdaptor(errorReceiver,entityResolver),
            parserFactory, defaultPackage );
    }
    
    private TRELAXNGReader(
        GrammarReaderControllerAdaptor _controller,
        SAXParserFactory parserFactory, String defaultPackage) {
            
        super(_controller, parserFactory);
        
        if(defaultPackage==null)    defaultPackage="generated";
        
        packageManager = new StackPackageManager(annGrammar.codeModel._package(defaultPackage));

        classFactory = new CodeModelClassFactory(_controller);
        annController = new AnnotatorControllerImpl(this, _controller, packageTracker);

        decorator =
            new RoleBasedDecorator(
                this,
                _controller,
                annGrammar,
                annController.getNameConverter(),
                packageManager,
                new DefaultDecorator(this, annController.getNameConverter()));
    }
    
    
    
    /** Keeps association between ReferenceExp and JPackage. */
    protected final HierarchicalPackageTracker packageTracker = new HierarchicalPackageTracker();
    /** Keeps track of the current package. */
    protected final StackPackageManager packageManager;
    
    private final RoleBasedDecorator decorator;

    /**
     * Used to create {@link com.sun.codemodel.JDefinedClass}es.
     * Detects collision.
     */
    protected final CodeModelClassFactory classFactory;
    
    
    private final AnnotatorController annController;

    protected final AnnotatedGrammar annGrammar = new AnnotatedGrammar(pool);
    public AnnotatedGrammar getAnnotatedResult() {
        return annGrammar;
    }

    protected Expression interceptExpression(State state, Expression exp) {
        exp = super.interceptExpression(state, exp);

        // if an error was found, stop processing.
        if (controller.hadError())
            return exp;
        
        if( exp==null )
            // patterns like "div" uses null.
            return exp;
        
        exp = decorator.decorate(state, exp);

        packageTracker.associate(exp, packageManager.getCurrentPackage());

        return exp;
    }
    
    public void wrapUp() {
        // First, let the super class do its job.
        super.wrapUp();

        // if we already have an error, abort further processing.
        if (controller.hadError())
            return;

        // associate the start pattern with the root pacakge
        packageTracker.associate(annGrammar, packageManager.getCurrentPackage());

        // add missing annotations and normalizes them.
        annGrammar.exp = grammar.exp;
        Annotator.annotate(annGrammar, annController);
        grammar.exp = annGrammar.exp;
    }
    
    public void startElement(String a, String b, String c, Attributes atts) throws SAXException {
        packageManager.startElement(atts);
        super.startElement(a, b, c, atts);
    }
    public void endElement(String a, String b, String c) throws SAXException {
        super.endElement(a, b, c);
        packageManager.endElement();
    }
    
    
    protected String localizeMessage(String propertyName, Object[] args) {
        String format;
        try {
            format = ResourceBundle.getBundle("com.sun.tools.xjc.reader.relaxng.Messages").getString(propertyName);
        } catch (Exception e) {
            try {
                format = ResourceBundle.getBundle("com.sun.tools.xjc.reader.Messages").getString(propertyName);
            } catch (Exception ee) {
                return super.localizeMessage(propertyName, args);
            }
        }
        return MessageFormat.format(format, args);
    }
}
