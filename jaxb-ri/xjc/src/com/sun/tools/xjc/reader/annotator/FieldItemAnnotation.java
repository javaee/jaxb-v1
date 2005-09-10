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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionCloner;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.msv.grammar.util.ExpressionPrinter;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.util.MultiplicityCounter;
import com.sun.tools.xjc.util.Util;

/**
 * adds missing FieldItems to the grammar.
 * 
 * This algorithm expands C-C, C-I, and C-P relationships to
 * C-F/F-C, C-F/F-I, and C-F/F-P relationships.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
class FieldItemAnnotation {
    /** debug output will be sent to this output. */
    private static PrintStream debug =
        Util.getSystemProperty(FieldItemAnnotation.class,"debug")!=null?System.err:null;

    public static void annotate(AnnotatedGrammar g, AnnotatorController controller) {

        FieldItemAnnotation ann = new FieldItemAnnotation(controller);

        // process all class items.
        ClassItem[] classes = g.getClasses();
        for (int i = 0; i < classes.length; i++) {
            if (debug != null)
                debug.println(" adding field item for " + classes[i].getTypeAsDefined().name());
            classes[i].exp = classes[i].exp.visit(ann.new Annotator(g, classes[i]));
        }
    }

    private final AnnotatorController controller;
    private FieldItemAnnotation(AnnotatorController _controller) {
        this.controller = _controller;
    }

    /**
     * this map will keep the annotated ReferenceExps. ReferenceExp is the key
     * and annotated result is the value. This map is very important to minimize
     * the number of generated classes/interfaces.
     */
    private final Map annotatedRefs = new HashMap();

    /**
     * Annotate the body of one ClassItem.
     */
    private class Annotator extends ExpressionCloner {

        private Annotator(AnnotatedGrammar g, ClassItem owner) {
            super(g.getPool());
            this.owner = owner;
        }

        /**
         * the top of this stack is the name of the nearest enclosing named item.
         * 
         * This information is used to determine the name of newly added FieldItem.
         */
        private final Stack names = new Stack();

        /** the current ClassItem object. Its body is what we are dealing with now. */
        private final ClassItem owner;

        public Expression onRef(ReferenceExp exp) {

            // If this ReferenceExp has already visited and annotated. reuse it.
            // This will prevent annotating the same ChoiceExp, etc again and again.
            Expression r = (Expression)annotatedRefs.get(exp);
            if (r != null)
                return r;

            // store the name information
            boolean pushed = false;
            if (exp.name != null && owner.exp!=exp && !(exp.exp instanceof ClassItem)) {
                // owner.exp!=exp to make sure that the name used for the class
                // will not be used for the field. 
                
                // !(exp.exp instanceof ClassItem) is ugly but it is to
                // avoid using the target class name. That class should be
                // used as a hint, and will have a lower priority that the name stack.
                names.push(exp.name);
                pushed = true;
            }
            r = exp.exp.visit(this);

            if (pushed)
                // store the annotated result.
                // if we haven't push the name,
                // the top of the names stack is the name from ancestors.
                // in that case, visiting this node next time will
                // produce a different result.
                // So we cannot memorize the result.
                annotatedRefs.put(exp, r);

            //    debug: assertion check
            // since it is now properly annotated,
            // every ClassItem, PrimitiveItem or InterfaceItem must be wrapped by FieldItem.
            r.visit(new ExpressionWalker() {
                public void onOther(OtherExp exp) {
                    if (exp instanceof FieldItem)
                        return;
                    if (exp instanceof IgnoreItem)
                        return;
                    if (exp instanceof JavaItem) {
                        System.err.println(exp);
                        throw new Error();
                    }
                }
            });

            if (pushed)
                names.pop();
            return r;
        }

        public Expression onOther(OtherExp exp) {
            // expands C-C,C-P,C-I relationship.
            if (exp instanceof TypeItem)
                return new FieldItem(decideName(exp), exp, ((TypeItem)exp).locator );

            if (exp instanceof IgnoreItem || exp instanceof SuperClassItem || exp instanceof FieldItem)
                return exp;

            // unknown OtherExps.
            _assert(!(exp instanceof JavaItem));
            // is this OK? looks potentially dangaerous
            // when this OtherExp is shared

            // but this is necessary to preserve OccurrenceExp
            exp.exp = exp.exp.visit(this);
            return exp;
        }

        public Expression onAttribute(AttributeExp exp) {
            Expression body = visitXMLItemContent(exp);
            if (body == exp.exp)
                return exp;
            else
                return pool.createAttribute(exp.nameClass, body);
        }

        public Expression onElement(ElementExp exp) {
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

            Expression body = visitXMLItemContent(exp);
            if (body == exp.contentModel)
                return exp;
            else
                return new ElementPattern(exp.getNameClass(), body);
        }

        private Expression visitXMLItemContent(NameClassAndExpression exp) {
            // if this element or attribute has a simple name, then
            // push it to the name stack.
            String name = null;
            NameClass nc = exp.getNameClass();
            if (nc instanceof SimpleNameClass)
                name = ((SimpleNameClass)nc).localName;

            // if this is the direct child of the owner, do not push it to
            // the name stack.
            /*
            Consider the following case. Typically, a ClassItem is created on <element>.
            In this case, ClassItem's exp field is an ElementExp.
            
            <classItem name="Foo">
              <element name="foo">
                <group>
                  <tahiti:classItem name="First">
                    <element name="FirstName">
                       ....
            
            In cases like this, it's silly to use "foo" as the name of children.
            So don't push it to the name stack.
            */
            if (exp == owner.exp)
                name = null;

            if (name != null)
                names.push(name);
            Expression body = exp.getContentModel().visit(this);
            if (name != null)
                names.pop();

            return body;
        }

        /**
         * <p>
         * For other live but not complex branches, if a branch doesn't contain
         * FieldItems, then it is wrapped by a FieldItem. Since FieldItem cannot be
         * wrapped by a FieldItem, we cannot wrap a branch by a FieldItem if
         * it contains FieldItem. This case happens only when a user explicitly
         * annotate a part of the grammar like this:
         * 
         * <PRE><XMP>
         * <choice>
         *   <group>
         *     <element name="A"/>
         *     <element name="B"/>
         *   </group>
         *   <group>
         *     &lt;!-- explicit annotation -->
         *     <ref name="X" t:role="field"/>
         *       ...
         *   </group>
         * </choice>
         * </XMP></PRE>
         * 
         * In this case, we recursively process that branch (since that branch may
         * contains other bare ClassItems.)
         * 
         * <p>
         * All 
         */
        public Expression onChoice(ChoiceExp exp) {

            // check whether there is only one meaningul branch, or more than one of them.
            Expression[] b = exp.getChildren();
            boolean[] fieldlessBranch = new boolean[b.length];
            int numLiveBranch = 0;

            boolean bBranchWithField = false;
            final boolean[] bBranchWithPrimitive = new boolean[1];

            if (debug != null) {
                debug.println("Processing Choice: " + ExpressionPrinter.printContentModel(exp));
                debug.println("checking each branch");
            }

            for (int i = 0; i < b.length; i++) {
                final boolean[] hasChildFieldItem = new boolean[1];

                // compute the multiplicity of the all child JavaItems and 
                // also compute whether this branch has FieldItem in it.
                Multiplicity m = Multiplicity.calc(b[i], new MultiplicityCounter() {
                    protected Multiplicity isChild(Expression exp) {
                        if (exp instanceof FieldItem)
                            hasChildFieldItem[0] = true;
                        if (exp instanceof PrimitiveItem)
                            bBranchWithPrimitive[0] = true;

                        if (exp instanceof IgnoreItem)
                            return Multiplicity.zero;
                        if (exp instanceof JavaItem)
                            return Multiplicity.one;
                        else
                            return null;
                    }
                });

                if (debug != null) {
                    debug.println("  Branch: " + ExpressionPrinter.printContentModel(b[i]));
                    debug.println("    multiplicity:" + m + "  hasChildFieldItem:" + hasChildFieldItem[0]);
                }

                if (m.isZero())
                    continue; // do nothing for this branch.

                numLiveBranch++;

                if (!hasChildFieldItem[0]) {
                    // memorize that this branch is fieldless.
                    fieldlessBranch[i] = true;
                    continue;
                }

                bBranchWithField = true;

                // this branch has a FieldItem. perform recursion.
                b[i] = b[i].visit(this);
            }

            if (numLiveBranch <= 1) {
                // there is only one meaningful branch.
                // this happens for patterns like <optional>.

                // visit all unvisited branch
                for (int i = 0; i < b.length; i++)
                    if (fieldlessBranch[i])
                        b[i] = b[i].visit(this);

                Expression r = Expression.nullSet;
                for (int i = 0; i < b.length; i++)
                    r = pool.createChoice(r, b[i]);

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

                final String fieldName = decideName(exp);

                Expression r = Expression.nullSet;
                for (int i = 0; i < b.length; i++) {
                    if (bBranchWithField && fieldlessBranch[i])
                        b[i] = new FieldItem(fieldName, b[i], null);

                    r = pool.createChoice(r, b[i]);
                }

                if (!bBranchWithField)
                    // there was no branch with FieldItem.
                    // add a FieldItem in here.
                    r = new FieldItem(fieldName, r, null);

                return r;
            }
        }

        /**
         * decides a name to be used as the field name.
         * 
         * Use the name at the stack top. As a side effect,
         * this method modifies the stack top so that fields that
         * are added later will have different names.
         */
        private String decideName(Expression hint) {
            String name = null;
            
            // first, try the name stack.
            if( !names.isEmpty() )
                name = (String)names.peek();
            
            // second, if the name stack is empty, try to use hint
            if( name==null && hint != null ) {
                if(hint instanceof NameClassAndExpression) {
                    NameClass nc = ((NameClassAndExpression)hint).getNameClass();
                    if (nc instanceof SimpleNameClass)
                        name = ((SimpleNameClass)nc).localName;
                }
                if(hint instanceof ClassItem) {
                    name = ((ClassItem)hint).name;
                }
                if(hint instanceof InterfaceItem) {
                    name = ((InterfaceItem)hint).name;
                }
            }
            
            // if everything else failed, use the default keyword "Content"
            if(name==null)  name="Content";

            return controller.getNameConverter().toPropertyName(name);
        }
    }

    //    /**
    //     * generates a field name suitable to hold a reference for the specified class.
    //     */
    //    private static String typeNameToFieldName( String bareName ) {
    //        return Character.toLowerCase(bareName.charAt(0))+bareName.substring(1);
    //    }

    private static void _assert(boolean b) {
        if (!b)
            throw new Error();
    }
}
