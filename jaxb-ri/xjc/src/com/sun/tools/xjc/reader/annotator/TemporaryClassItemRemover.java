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
package com.sun.tools.xjc.reader.annotator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.util.Util;

/**
 * promotes useful {@link ClassCandidateItem}s to {@link ClassItem}s
 * and removes the rest.
 * 
 * <p>
 * The current implementation employs the following criteria to
 * find "unnecessary" class item candidates.
 * 
 * <ol>
 *  <li>Any candidate that are referenced from InterfaceItem or SuperClassItem
 *        cannot be removed. InterfaceItem requires its children to be a ClassItem,
 *        so does SuperClassItem. Thus we cannot remove them.
 * 
 *  <li>Any candidate that has more than one child ClassItem/InterfaceItem/
 *        PrimitiveItem cannot be removed.
 *        Those items are considered too complex to be removed.
 * 
 *  <li>Any candidate that is unrechable from any other part of the grammar
 *      (such as class items for root elements, or sometimes a grammar
 *      is not-connected.) Those items cannot be removed.
 *  
 *  <li>Any candidate that has ElementExp/AttributeExp whose name is
 *      non-simple cannot be removed.
 * 
 *  <li>Any candidate that can be reached from more than one ClassItem
 *      cannot be removed. This only applies to candidates created from
 *      elements and attributes. The idea is to make sure things like
 *      <code>foo = element foo { text }</code> is mapped to its own class,
 *      while keeping things like <code>fooType = data NMTOKEN </code>
 *      from being mapped to a class. 
 * 
 * </ol>
 * 
 * <p>
 * The following things are recently changed.
 * <ol>
 *   <!-- none as such -->
 * </ol>
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
class TemporaryClassItemRemover {

    /** debug logger. */
    private static final java.io.PrintStream debug =
        Util.getSystemProperty(TemporaryClassItemRemover.class,"debug")!=null
        ? System.out : null;
    
    public static void remove( AnnotatedGrammar grammar ) {
        ClassItem[] items = grammar.getClasses();
        
        //
        // run the first pass and determine which class items can be removed.
        //
        Pass1 p1 = new Pass1();
        
        // process expressions reachable from the start pattern first.
        // the start pattern needs a special treatment, so this has to be done
        // before starting to process other ClassItems.
        grammar.visit(p1);
        // then visit all unprocessed ClassItems individually as
        // some of them maybe unreachable from the start symbol.
        for( int i=0; i<items.length; i++ )
            p1.processIfUnvisited(items[i]);
        
        // compute {all classes}-{not removable classes}
        Set cs = new HashSet(p1.allCandidates);
        cs.removeAll( p1.notRemovableClasses );
        
        // unreachable classes are not removable.
        if(debug!=null) {
            Iterator itr = cs.iterator();
            while(itr.hasNext()) {
                ClassCandidateItem ci = (ClassCandidateItem)itr.next();
                if(!p1.reachableClasses.contains(ci))
                    debug.println( displayName(ci)+" : this is unreachable" );
            }
        }
        cs.retainAll( p1.reachableClasses );
        
        
        if(debug!=null) {
            Iterator itr = cs.iterator();
            while( itr.hasNext() ) {
                ClassCandidateItem ci = (ClassCandidateItem)itr.next();
                debug.println( " "+displayName(ci)+" will be removed" );
            }
        }
        
        
        // run the second pass and remove unnecessary candidates
        Pass2 p2 = new Pass2(grammar.getPool(),cs);
        
        grammar.visit(p2);
        for( int i=0; i<items.length; i++ )
            items[i].visit(p2);
    }

    /**
     * computes exact ClassItems to be removed.
     */
    private static class Pass2 extends ExpressionCloner {
        /**
         * {@link ClassCandidateItem}s to be removed.
         */
        private final Set rejectedCandidates;
        
        private final Set visitedExps = new HashSet();
        
        Pass2( ExpressionPool pool, Set _rejected ) {
            super(pool);
            this.rejectedCandidates = _rejected;
        }
        
        public Expression onAttribute(AttributeExp exp) {
            return pool.createAttribute(exp.getNameClass(),exp.exp.visit(this));
        }

        public Expression onElement(ElementExp exp) {
            if( visitedExps.add(exp) )
                exp.contentModel = exp.contentModel.visit(this);
            return exp;
        }

        public Expression onOther(OtherExp exp) {
            if( exp instanceof ClassCandidateItem ) {
                if( rejectedCandidates.contains(exp) )
                    // remove this node
                    return exp.exp.visit(this);
                else {
                    if( visitedExps.add(exp) )
                        exp.exp = exp.exp.visit(this);
                    
                    return ((ClassCandidateItem)exp).toClassItem();
                }
            }
            
            if( visitedExps.add(exp) )
                exp.exp = exp.exp.visit(this);
            return exp;
        }

        public Expression onRef(ReferenceExp exp) {
            if( visitedExps.add(exp) )
                exp.exp = exp.exp.visit(this);
            return exp;
        }

    }
    
    /**
     * computes exact ClassItems to be removed.
     */
    private static class Pass1 extends BGMWalker {
        
        /** this set stores all examined ClassItems and ClassCandidateItems. */
        private final Set checkedClasses = new HashSet();
        
        /**
         * this set stores all ClassCandidateItems that are reachable
         * from some other ClassItems/ClassCandidateItems.
         */
        public final Set reachableClasses = new HashSet();
        
        /**
         * This set stores ClassCandidateItems that are
         * determined not to be removed.
         */
        public final Set notRemovableClasses = new HashSet();
        
        /**
         * This set stores all ClassCandidateItems that are found.
         */
        public final Set allCandidates = new HashSet();
        
        /**
         * This set stores all the child JavaItem of the current ClassItem.
         * 
         * Often the same {@link PrimitiveItem}s are used multiple times
         * to handle different occasions, so this should better not be a 
         * {@link Set}.
         */
        private List childItems = new ArrayList();
        
        /**
         * set to true when an element/attribute with a non-simple name
         * is found.
         */
        private boolean hasNonSimpleName;
        
        /**
         * Processes a ClassItem if it's not processed yet.
         * This method is used to visit expressions which are unreachable from the root.
         */
        public void processIfUnvisited( ClassItem ci ) {
            if(!checkedClasses.contains(ci)) {
                if(debug!=null)
                    debug.println("processIf("+ci.name+")");
                ci.visit(this);
            }
        }
        
        private OtherExp parentItem = null;
            
        public Object onIgnore( IgnoreItem item ) {
            // since IgnoreItem is completely ignored, don't perform recursion.
            return null;
        }
        
        public Object onField( FieldItem item ) {
            // we are not interested in FieldItems now.
            // just perform recursion.
            item.exp.visit(this);
            return null;
        }
        
        public Object onSuper( SuperClassItem item ) { updateAndVisit(item); return null; }
        public Object onInterface( InterfaceItem item ) { updateAndVisit(item); return null; }
        
        private void updateAndVisit( JavaItem item ) {
            childItems.add(item);
            // update the parentItem field, and check the body.
            OtherExp old = parentItem;
            parentItem=item;
            item.exp.visit(this);
            parentItem=old;
        }
        
        public Object onExternal( ExternalItem item ) {
            childItems.add(item);
            // don't do recursion because this item doesn't have any
            // more interesting thing in it.
            return null;
        }
        
        public Object onPrimitive( PrimitiveItem item ) {
            // we don't need to check the body of a PrimitiveItem.
            // just store it and return.
            childItems.add(item);
            return null;
        }
        
        private static class Result {
            public Result( List _ci, boolean _has ) {
                this.childItems = _ci;
                this.hasNonSimpleName = _has;
            }
            public final List childItems;
            public final boolean hasNonSimpleName;
        }
        /**
         * Visits the children and collects all JavaItems and ClassCandidateItem
         * in a set and returns it.
         */
        private Result collectChildItems( OtherExp exp ) {
            // prepare a fresh set to collect child JavaItems.
            List oldChildItems = childItems;
            childItems = new ArrayList();
            
            OtherExp oldParent = parentItem;
            parentItem = exp;
            
            boolean oldHNSN = hasNonSimpleName;
            hasNonSimpleName = false;
            
            // visit the children.
            exp.exp.visit(this);

            Result result = new Result(childItems,hasNonSimpleName);
            
            childItems = oldChildItems;
            parentItem = oldParent;
            hasNonSimpleName = oldHNSN;
            
            return result;
        }
        
        public Object onClass( ClassItem item ) {
            if( debug!=null )
                debug.println("processing "+item.name);
            
            childItems.add(item);    // this has to be done before the checkedClasses field is examined.
            
            if(!checkedClasses.add(item))
                // Since this ClassItem is already checked, don't check it again.
                return null;
            
            // we are not interested in what the children are and how they look like,
            // but we need to recursively visit descendants.
            collectChildItems(item);
            
            return null;
        }
        
        public void onOther(OtherExp exp) {
            if(!(exp instanceof ClassCandidateItem)) {
                super.onOther(exp);
                return;
            }
            
            ClassCandidateItem cci = (ClassCandidateItem)exp;
            
            if( debug!=null )
                debug.println("processing "+displayName(cci));
            
            childItems.add(cci);   // this has to be done before the checkedClasses field is examined.
            allCandidates.add(cci);
            
            if(parentItem!=null) {
                // this class item is reachable
                if(!reachableClasses.add(cci)) {
                    if( cci.exp instanceof NameClassAndExpression )
                        // This means that this ClassItem is referenced from more than one
                        // classes. So this ClassItem cannot be removed.
                        if(notRemovableClasses.add(cci))
                            if( debug!=null )
                                debug.println(displayName(cci)+" : referenced more than once");
                }
            } else {
                // this can be the root class. Can't be removed
                notRemovableClasses.add(cci);
                if( debug!=null )
                    debug.println(displayName(cci)+" : can be a root class");
            }
            
            if((parentItem instanceof SuperClassItem)
            || (parentItem instanceof InterfaceItem)) {
                // if a ClassItem is referenced from SuperClassItem or InterfaceItem,
                // then it can't be removed.
                if(notRemovableClasses.add(cci))
                    if( debug!=null )
                        debug.println(displayName(cci)+" : referenced by a superClass/interfaceItem");
            }
            
            if(!checkedClasses.add(cci))
                // Since this ClassItem is already checked, don't check it again.
                return;
            
            Result r = collectChildItems(cci);
            
            if( r.hasNonSimpleName ) {
                if(notRemovableClasses.add(cci))
                    if( debug!=null )
                        debug.println(displayName(cci)+" : this class has non-simple element/attribute");
            }
            if( r.childItems.size()==0 ) {
                // if there is no child item, then probably
                // the existance of this element is significant.
                // so keep it.
                if(notRemovableClasses.add(cci))
                    if( debug!=null )
                        debug.println(displayName(cci)+" : this class has no child item");
            } else
            if( r.childItems.size()>1 ) {
                // if a ClassItem has more than one child items,
                // then it cannot be removed.
                if(notRemovableClasses.add(cci))
                    if( debug!=null )
                        debug.println(displayName(cci)+" : this class has multiple fields");
            }
        }

        public void onAttribute(AttributeExp exp) {
            if(!(exp.nameClass instanceof SimpleNameClass))
                hasNonSimpleName = true;
            exp.exp.visit(this);
        }

        public void onElement(ElementExp exp) {
            if(!(exp.getNameClass() instanceof SimpleNameClass))
                hasNonSimpleName = true;
            exp.contentModel.visit(this);
        }

    };
    
    private static final String displayName( ClassCandidateItem cci ) {
        return cci.name+'@'+Integer.toHexString(cci.hashCode());
    }
}
