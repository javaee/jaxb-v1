/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.util.NotAllowedRemover;
import com.sun.tools.xjc.util.Util;
import com.sun.tools.xjc.writer.Writer;

/**
 * Forges a raw AGM into the fully-fledged annotated AGM.
 * 
 * TahitiGrammarReader can add annotation partially. This class performs
 * several processes and forges those partially annotated AGM into fully
 * annotated AGM.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class Annotator
{
    /** Set to a valid object (such as System.err) to obtain trace log. */
    private static java.io.PrintStream debug =
        Util.getSystemProperty(Annotator.class,"debug")!=null?System.out:null;

    public static void annotate(
        AnnotatedGrammar grammar,
        AnnotatorController controller ) {
        
        ClassItem[] classes;
        
        if(debug!=null) {
            debug.println("---------------------------------------------");
            debug.println("initial grammar");
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        remove <notAllowed/> from the grammar. <notAllowed/> affects the
        calculation of multiplicity and therefore has to be removed first.
        */
        {
            if(debug!=null)    debug.println("removing notAllowed");
            
            NotAllowedRemover visitor = new NotAllowedRemover(grammar.getPool());
            grammar.visit( visitor );
            if( grammar.exp==Expression.nullSet )    return;
            // abstract elements of XSD makes AGM disjoint.
            // so we have to explicitly visit each children.
            classes = grammar.getClasses();
            for( int i=0; i<classes.length; i++ )
                classes[i].exp = classes[i].exp.visit( visitor );
        }
        
        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        remove <empty/> from the grammar, FieldItems and SuperClassItems
        that have empty in it will be removed.
        */
        {
            if(debug!=null)    debug.println("removing empty JavaItems");
            
            EmptyJavaItemRemover visitor = new EmptyJavaItemRemover(grammar.getPool());
            grammar.visit( visitor );
            if( grammar.exp==Expression.nullSet )    return;
            // abstract elements of XSD makes AGM disjoint.
            // so we have to explicitly visit each children.
            classes = grammar.getClasses();
            for( int i=0; i<classes.length; i++ )
                classes[i].exp = classes[i].exp.visit( visitor );
        }
        
        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }

        /*
        remove <mixed/> from the grammar.
        */
        {
            if(debug!=null)    debug.println("removing mixed");
            
            MixedRemover visitor = new MixedRemover(grammar);
            grammar.visit( visitor );
            if( grammar.exp==Expression.nullSet )    return;
            // abstract elements of XSD makes AGM disjoint.
            // so we have to explicitly visit each children.
            classes = grammar.getClasses();
            for( int i=0; i<classes.length; i++ )
                classes[i].exp = classes[i].exp.visit( visitor );
        }
        
        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        simplify complicated datatypes
        */
        {
            if(debug!=null)    debug.println("simplifying datatypes");
            grammar.visit(
                new DatatypeSimplifier(grammar.getPool()) );
        }
        
        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        add PrimitiveItem.
        */
        {
            if(debug!=null)    debug.println("examining primitive types");
            
            PrimitiveTypeAnnotator visitor = new PrimitiveTypeAnnotator(grammar,controller);
            grammar.visit( visitor );
            if( grammar.exp==Expression.nullSet )    return;
            classes = grammar.getClasses();
            for( int i=0; i<classes.length; i++ )
                classes[i].exp = classes[i].exp.visit( visitor );
        }
        
        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        add even more ClassItem and InterfaceItem to annotate <choice>s.
        */
        if(debug!=null)    debug.println("annotating complex choices");
        ChoiceAnnotator.annotate( grammar, controller );
        
        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        then remove temporarily added class items. temporary class items
        are added while parsing various grammars into the AGM. And some
        of them are unnecessary.
        */
        if(debug!=null)    debug.println("removing temporary class items");
        TemporaryClassItemRemover.remove( grammar );

        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
        /*
        perform field annotation. this will normalize
        C-C/C-P/C-I relation and make up for missing FieldItems.
        */
        if(debug!=null)    debug.println("adding field items");
        FieldItemAnnotation.annotate( grammar, controller );

        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }

        /*
        Fully compute the type inheritance relationship between generated classes.
        This has to be done after all interfaces/classes are fixed.
        */
        if(debug!=null)    debug.println("computing type hierarchy");
        HierarchyAnnotator.annotate( grammar, controller );

        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }

        /*
        Determine type for all symbol spaces.
        This has to be done after the type hierarchy is fully computed,
        but before we compute types for FieldItems.
        */
        if(debug!=null)    debug.println("determining types for symbol spaces");
        SymbolSpaceTypeAssigner.assign( grammar, controller );

        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
//        /*
//        Checks field name collisions
//        */
//        if(debug!=null)     debug.println("checking field name collisions");
//        FieldCollisionChecker.check( grammar, controller );
        
        /*
        finally perform overall normalization. This will ensure that
        JavaItems are used correctly and compute various field values for
        JavaItems.
        */
        if(debug!=null)    debug.println("normalizing relations");
        RelationNormalizer.normalize( grammar, controller );

        if(debug!=null) {
            Writer.writeToConsole(true,grammar);
            debug.println("---------------------------------------------");
        }
        
//      the new unmarshaller of XJC needs to have SuperClassItem explicitly.
//        if(debug!=null)    debug.println("removing superClass body definition");
//        SuperClassBodyRemover.remove( grammar );
    }
}
