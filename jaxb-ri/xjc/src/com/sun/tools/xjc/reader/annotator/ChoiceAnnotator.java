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

import java.util.HashSet;
import java.util.Set;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.msv.grammar.util.ExpressionPrinter;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.util.MultiplicityCounter;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * annotates &lt;choice&gt; with ClassItem/InterfaceItem
 * so that it can be handled easily.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
class ChoiceAnnotator
{
    private static java.io.PrintStream debug = null;
    
    public static void annotate( AnnotatedGrammar g, AnnotatorController _controller) {
        
        ChoiceAnnotator ann = new ChoiceAnnotator(g,_controller);
        
        // process all class items and candidates.
        g.visit(ann.new Finder());
    }
    
    private ChoiceAnnotator( AnnotatedGrammar g, AnnotatorController _controller ) {
        this.grammar = g;
        this.classFactory = new CodeModelClassFactory(_controller.getErrorReceiver());
    }
    
    /** the grammar object to which we are adding annotation. */
    private final AnnotatedGrammar grammar;
    
    private final CodeModelClassFactory classFactory;
    
    
    private class Finder extends BGMWalker {
        private final Set visited = new HashSet();
        
        public Object onClass(ClassItem item) {
            if( visited.add(item) ) {
                item.exp = item.exp.visit(new Annotator(item));
                // recursively visit children
                super.onClass(item);
            }
            return null;    // unused
        }

        public void onOther(OtherExp exp) {
            if(exp instanceof ClassCandidateItem) {
                if( visited.add(exp) ) {
                    ClassCandidateItem cci = (ClassCandidateItem)exp;
                    cci.exp = cci.exp.visit(new Annotator(cci));
                } else
                    return;
            }
            super.onOther(exp);
        }

    }
    
    
    /**
     * Annotate the body of one ClassItem.
     */
    private class Annotator extends ExpressionCloner {
        
        private Annotator( ClassItem owner ) {
            super(grammar.getPool());
            
            this._package = owner.getTypeAsDefined()._package();
            this.className = owner.getType().name();
        }
        
        private Annotator( ClassCandidateItem owner ) {
            super(grammar.getPool());
            
            this._package = owner.targetPackage;
            this.className = owner.name;
        }
        
        // name and the package of the current class/candidate class that
        // we are dealing with.
        private final JPackage _package;
        private final String className;
        
        /**
         * A counter. this value is used to create unique names for added class items.
         */
        private int iota = 0;
        
        public Expression onRef( ReferenceExp exp ) {
            // keep the ReferenceExp to keep the name information,
            // but I'm not sure if we can reuse the same ReferenceExp object
            // because exp.exp could be re-written into different things
            // depending on where it is referenced from.
            // return new ReferenceExp( exp.name, exp.exp.visit(this) );
            exp.exp = exp.exp.visit(this);
            return exp;
        }
        
        public Expression onOther( OtherExp exp ) {
            // expands C-C,C-P,C-I relationship.
            if( exp instanceof PrimitiveItem
            ||  exp instanceof InterfaceItem
            ||  exp instanceof ClassItem
            ||  exp instanceof IgnoreItem
            ||  exp instanceof SuperClassItem
            ||  exp instanceof FieldItem
            ||  exp instanceof ClassCandidateItem )
                return exp;
            
            // this is potentially dangerous, since
            // OtherExp might be shared.
            // but this is necessary to preserve OccurrenceExp
            exp.exp = exp.exp.visit(this);
            return exp;
        }
        
        public Expression onAttribute( AttributeExp exp ) {
            Expression body = exp.exp.visit(this);
            if( body==exp.exp )    return exp;
            else    return pool.createAttribute( exp.nameClass, body );
        }
    
        public Expression onElement( ElementExp exp ) {
            /*
            although we will lose any additional information
            added to this ElementExp,
            we have to create a copy of ElementExp. Otherwise
            we cannot correctly process things like:
            
            <define name="X">
                <ref name="Z" t:role="class"/>
            </define>
            <define name="Y">
                <ref name="Z" t:role="field"/>
            </define>
            <define name="Z">
                <element name="foo">
                    <data type="string" t:role="primitive"/>
                </element>
            </define>
            
            When processing X->Z, we want to add extra FieldItem.
            When processing Y->Z, we don't want that.
            */
            
            Expression body = exp.contentModel.visit(this);
            if(body==exp.contentModel)    return exp;
            else    return new ElementPattern( exp.getNameClass(), body );
        }

        /**
         * annotate ChoiceExp with FieldItem.
         * 
         * <p>
         * children of a ChoiceExp is called "branches". In this method,
         * branches of the entire choice group is considered.
         * 
         * <p>
         * We consider a branch is "alive" if there is some JavaItem in that branch.
         * For example, &lt;empty&gt; is not a live branch. IgnoreItem is another
         * example of non-live branch.
         * 
         * <p>
         * If only one branch out of the entire branches is alive, then this choice
         * is not treated at all, and the live branch is recursively processed.
         * This handles &lt;optional> p &lt;/optional>.
         * 
         * <p>
         * Otherwise, the following algorithm is applied:
         * 
         * <p>
         * A branch is said to be "complex", if the multiplicity of child JavaItem is
         * more than one. For example,
         * 
         * <PRE><XMP>
         * <oneOrMore>
         *   <tahiti:classItem>
         *     <element name="..."/>
         *       ...
         *     </element>
         *   </tahiti:classItem>
         * </oneOrMore>
         * </XMP></PRE>
         * 
         * <p>
         * is a complex branch. If a branch is complex, then it is wrapped by a 
         * ClassItem. Wrapping by a ClassItem makes its
         * multiplicity (1,1). This ensures that every branch has the multiplicity
         * of at-most-one.
         */
        public Expression onChoice( ChoiceExp exp ) {
            
            // check whether there is only one meaningul branch, or more than one of them.
            Expression[] b = exp.getChildren();
            final boolean[] complexBranch = new boolean[b.length];
            boolean[] fieldlessBranch = new boolean[b.length];
            int numLiveBranch = 0;
            
            boolean bBranchWithField = false;
            final boolean[] bBranchWithPrimitive = new boolean[1];

            if(debug!=null) {
                debug.println( "Processing Choice: " + ExpressionPrinter.printContentModel(exp) );
                debug.println("checking each branch");
            }
            
            for( int i=0; i<b.length; i++ ) {
                final boolean[] hasChildFieldItem = new boolean[1];
                final int _i = i;

                // compute the multiplicity of the all child JavaItems and 
                // also compute whether this branch has FieldItem in it.
                Multiplicity m = Multiplicity.calc( b[i],
                    new MultiplicityCounter(){
                        protected Multiplicity isChild( Expression exp ) {
                            if(exp instanceof ElementExp)   complexBranch[_i] = true;
                            if(exp instanceof AttributeExp)   complexBranch[_i] = true;
                            if(exp instanceof FieldItem)    hasChildFieldItem[0] = true;
                            if(exp instanceof PrimitiveItem)    bBranchWithPrimitive[0] = true;
                            
                            if(exp instanceof IgnoreItem)    return Multiplicity.zero;
                            if(exp instanceof JavaItem)        return Multiplicity.one;
                            if(exp instanceof ClassCandidateItem)   return Multiplicity.one;
                            else                        return null;
                        }
                    });

                if(debug!=null) {
                    debug.println( "  Branch: " + ExpressionPrinter.printContentModel(b[i]) );
                    debug.println( "    multiplicity:"+m+"  hasChildFieldItem:"+hasChildFieldItem[0] );
                }
                
                if(m.isZero())
                    continue;        // do nothing for this branch.
                
                numLiveBranch++;
                
                if(!m.isAtMostOnce()) {
                    // memorize that this branch is complex.
                    complexBranch[i] = true;
                    continue;
                }
                
                if( !hasChildFieldItem[0] ) {
                    // memorize that this branch is fieldless.
                    fieldlessBranch[i] = true;
                    continue;
                }
                
                bBranchWithField = true;
                
                // this branch has a FieldItem. perform recursion.
                b[i] = b[i].visit(this);
            }
            
            if( numLiveBranch<=1 ) {
                // there is only one meaningful branch.
                // this happens for patterns like <optional>.
                
                // visit all unvisited branch
                for( int i=0; i<b.length; i++ )
                    if( fieldlessBranch[i] || complexBranch[i] )
                        b[i] = b[i].visit(this);
                
                Expression r = Expression.nullSet;
                for( int i=0; i<b.length; i++ )
                    r = pool.createChoice( r, b[i] );
            
                return r;
                
            } else {
                
                /*
                if we don't have any branch with FieldItem, then we just need
                one FieldItem to cover the entire branches.
                
                TODO:(?) actually this would be done better. Even if there are 
                branches with FieldItems, one created FieldItem can cover all
                FieldItem-less branches, and then that FieldItem and other
                branches can be combined. But is it an improvement?
                */
                
                for( int i=0; i<b.length; i++ ) {
                    if( complexBranch[i] ) {
                        if(debug!=null)
                            debug.println("  Insert a wrapper class on: "+ExpressionPrinter.printContentModel(exp));
                        
                        // insert a new class item here.
                        b[i] = new ClassCandidateItem(
                            classFactory,
                            grammar,
                            _package,
                            className+"Subordinate"+(++iota),
                            null,
                            b[i].visit(this) );
                    }
                }
            
                Expression r = Expression.nullSet;
                for( int i=0; i<b.length; i++ )
                    r = pool.createChoice( r, b[i] );

                if( !bBranchWithField ) {
                    // there was no branch with FieldItem.
                    
                    if( !bBranchWithPrimitive[0] ) {
                        // if there is no branch with a PrimitiveItem,
                        // add an interface item automatically.
                        
                        // compute the interface name
                        JPackage pkg = _package;
                        
                        String intfName = "I"+className+"Content";
                        
                        if(pkg.isDefined(intfName)) {
                            // the last resort
                            int cnt = 2;
                            while( pkg.isDefined(intfName+cnt) )
                                cnt++;
                            intfName = intfName + cnt;
                        }
                    
                        if(debug!=null) {
                            debug.println("  Wrap it by an interface iem: "+intfName);
                            debug.println("  "+ ExpressionPrinter.printContentModel(r) );
                        }
                        r = grammar.createInterfaceItem(
                            classFactory.createInterface( pkg, intfName, null/*TODO:location*/),
                            r, null );
                    }
                }
                return r;
            }
        }
    }
}
