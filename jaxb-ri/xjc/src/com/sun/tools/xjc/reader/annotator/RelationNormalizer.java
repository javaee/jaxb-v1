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

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.ExpressionVisitorExpression;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.FieldItem.BadTypeException;
import com.sun.tools.xjc.grammar.util.AnnotationRemover;
import com.sun.tools.xjc.grammar.util.FieldMultiplicityCounter;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Normalizes the relationships between JavaItems.
 * 
 * <h2>1st pass</h2>
 * 
 * <p>
 * Its first job is to make sure that no prohibited relationships are used.
 * For example, super-super relationship is prohibited. See the design document
 * for the complete list of the prohibited relationships.
 * Also, several relationships have special multiplicity constraints.
 * Constraints are also enforced.
 * 
 * <p>
 * Its second job is to create a FieldUse object for each class-field relationship and
 * connects a class and its fields. It is possible and allowed for one ClassItem object
 * to have multiple FieldItem objects that share the same field name.
 * 
 * <p>
 * It also strips any BGM mark-ups found under {@link IgnoreItem}s.
 * For the unmarshaller to work correctly, IgnoreItem cannot have any such
 * mark up.
 * 
 * 
 * <h2>2nd pass</h2>
 * <p>
 * In the 2nd pass, our first job is to compute the total multiplicity of each field.
 * One ClassItem can have multiple FieldItem with the same name, and one FieldItem
 * can have multiple TypeItem as its children.
 * 
 * <p>
 * In the 1st pass, we've computed the multiplicity for every FieldItem. So before
 * the 2nd pass, we are in the following situation:
 * 
 * <PRE><XMP>
 *   <group t:role="class">
 *     <element name="abc" t:role="field"> <!-- multiplicity (1,1) -->
 *       <ref name="abc.model"/>
 *     </element>
 *     <oneOrMore t:role="field"> <!-- multiplicity (1,unbounded) -->
 *       <element name="abc">
 *         <ref name="abc.model"/>
 *       </element>
 *     </oneOrMore>
 *   </group>
 * </XMP></PRE>
 * 
 * <p>
 * We'd like to know the "total" multiplicity of the field "abc". In this case,
 * it will be (2,unbounded).
 * 
 * <p>
 * Its next job is to compute the type of the field. Field values may have
 * different types, and we need to compute the common base type.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class RelationNormalizer {
    
    private RelationNormalizer( AnnotatorController _controller, AnnotatedGrammar _grammar ) {
        this.controller = _controller;
        this.pool = _grammar.getPool();
        this.grammar = _grammar;
    }
    
    private final AnnotatorController controller;
    private final ExpressionPool pool;
    private final AnnotatedGrammar grammar;
    
    /**
     * performs the normalization.
     * 
     * @param grammar
     *        The top-level expression of the parsed grammar.
     * @param controller
     *        This object is used to report errors and obtain the source location
     *        for error messages.
     */
    public static void normalize( AnnotatedGrammar grammar, AnnotatorController controller ) {
        
        RelationNormalizer n = new RelationNormalizer(controller,grammar);
        ClassItem[] classItems = grammar.getClasses();
        InterfaceItem[] interfaceItems = grammar.getInterfaces();
        
        Pass1 pass1 = n.new Pass1();
        
        for( int i=0; i<classItems.length; i++ )
            pass1.process(classItems[i]);
        for( int i=0; i<interfaceItems.length; i++ )
            interfaceItems[i].visit(pass1);
        
        
        // for each field use in each class item,
        // compute the total multiplicity.
        // also, compute the type of the field.
        for( int i=0; i<classItems.length; i++ ) {
            FieldUse[] fieldUses = classItems[i].getDeclaredFieldUses();
            for( int j=0; j<fieldUses.length; j++ ) {
                
                fieldUses[j].multiplicity =
                    FieldMultiplicityCounter.count(
                        classItems[i].exp, fieldUses[j] );
                
                // collect all possible ClassItems for this type.
                Set possibleTypes = new java.util.HashSet();
                FieldItem[] fields = (FieldItem[])fieldUses[j].items.toArray(new FieldItem[0]);
                for( int k=0; k<fields.length; k++ )
                    possibleTypes.add( fields[k].getType(grammar.codeModel) );
                
                // then compute the base type of them.
                fieldUses[j].type = TypeUtil.getCommonBaseType( grammar.codeModel, possibleTypes );
                
                if( fieldUses[j].isDelegated() && !fieldUses[j].multiplicity.isAtMostOnce() ) {
                    // if the delegation is on for this field,
                    // the multiplicity must be (1,1).
                    controller.reportError(
                        new Locator[]{classItems[i].locator},
                        Messages.format(
                            Messages.ERR_DELEGATION_MULTIPLICITY_MUST_BE_1,
                            fieldUses[j].name ) );
                }
            }
        }
    }
    
    /**
     * see the documentation of RelationNormalizer.
     * 
     * Pass1 walks the content models of all class items.
     */
    private class Pass1 implements ExpressionVisitorExpression {
        
        public Expression onAttribute( AttributeExp exp ) {
            Expression newContent = exp.exp.visit(this);
            if( newContent!=exp.exp )
                // the content model is modified.
                return pool.createAttribute( exp.getNameClass(), newContent );
            else
                return exp;
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
            
            return new ElementPattern( exp.getNameClass(), body );
        }
        
        public Expression onMixed( MixedExp exp ) {
            // <mixed> doesn't affect the multiplicity.
            return pool.createMixed(exp.exp.visit(this));
        }
        
        public Expression onList( ListExp exp ) {
            // <list> itself doesn't affect the multiplicity.
            return pool.createList(exp.exp.visit(this));
        }

        public Expression onConcur( ConcurExp exp ) {
            // possibly, it can be served by ignoring all but one branch.
            throw new Error("concur is not supported");
        }
        
        public Expression onChoice( ChoiceExp exp ) {
            Expression lhs = exp.exp1.visit(this);
            Multiplicity lhc = multiplicity;
            Expression rhs = exp.exp2.visit(this);
            Multiplicity rhc = multiplicity;
            
            multiplicity = Multiplicity.choice(lhc,rhc);
            return pool.createChoice( lhs, rhs );
        }
        
        public Expression onSequence( SequenceExp exp ) {
            Expression lhs = exp.exp1.visit(this);
            Multiplicity lhc = multiplicity;
            Expression rhs = exp.exp2.visit(this);
            Multiplicity rhc = multiplicity;
            
            multiplicity = Multiplicity.group(lhc,rhc);
            return pool.createSequence( lhs, rhs );
        }
        
        public Expression onInterleave( InterleaveExp exp ) {
            Expression lhs = exp.exp1.visit(this);
            Multiplicity lhc = multiplicity;
            Expression rhs = exp.exp2.visit(this);
            Multiplicity rhc = multiplicity;
            
            multiplicity = Multiplicity.group(lhc,rhc);
            return pool.createInterleave( lhs, rhs );
        }
        
        public Expression onOneOrMore( OneOrMoreExp exp ) {
            Expression p = pool.createOneOrMore( exp.exp.visit(this) );
            multiplicity = Multiplicity.oneOrMore(multiplicity);
            
            return p;
        }

// terminal items. starts with multiplicity (0,0)
        public Expression onEpsilon() {
            multiplicity = Multiplicity.zero;
            return Expression.epsilon;
        }
        public Expression onNullSet() {
            multiplicity = Multiplicity.zero;
            return Expression.nullSet;
        }
        public Expression onAnyString() {
            // anyString should have been completely removed.
            throw new Error();
        }
        public Expression onData( DataExp exp ) {
            multiplicity = Multiplicity.zero;
            return exp;
        }
        public Expression onValue( ValueExp exp ) {
            multiplicity = Multiplicity.zero;
            return exp;
        }
        
        public Expression onRef( ReferenceExp exp ) {
            return exp.exp.visit(this);
        }
        
        /**
         * Visits a {@link ClassItem} and process its contents.
         */
        public void process( ClassItem ci ) {

            if(!visitedClasses.add(ci)) // already processed?
                return;
            
            JavaItem oldParent = parentItem;
            parentItem = ci;
            ci.exp.visit(this);
            parentItem = oldParent;
            
            // we need the normalized result of the base classes,
            // so process them first if there's a base class.
            if(ci.getSuperClass()!=null)
                process(ci.getSuperClass());
            
            // delete any FieldUse that is already realized in 
            // the base classes.
            ci.removeDuplicateFieldUses();
            
            // recognize delegation
            FieldUse[] fus = ci.getDeclaredFieldUses();
            for (int i=0; i<fus.length; i++) {
                if(!fus[i].isDelegated())
                    continue;
                
                // type of FieldUse is not yet computed, and it cannot be
                // computed. (And there's a chiken-and-egg problem here)
                // so limit the size of FieldItem to 1, just like we do for
                // SuperClassItem.
                if( fus[i].items.size()!=1 )
                    throw new JAXBAssertionError(); // TODO: error
                
                FieldItem fi = (FieldItem)fus[i].items.iterator().next();
                JType t = fi.getType(grammar.codeModel);
                if( t==grammar.codeModel.ref(Object.class) ) {
                    // this is a special case when no common interface was
                    // found among types. We will turn off the delegation
                    // generation for this as a temporary solution.
                    // REVISIT: check what the spec is going to say about this in 2.0
                    fus[i].disableDelegation();
                    continue;
                }
                if(!isInterface(t)) {
                    controller.reportError(
                            new Locator[]{ci.locator},
                            Messages.format(Messages.ERR_DELEGATION_MUST_BE_INTERFACE,
                                    ci.name, t.fullName() ));
                }
                
                ci.getTypeAsDefined()._implements( (JClass)t );
            }
        }
        
        
    // Java items
    //=======================================
        
        public Expression onOther( OtherExp exp ) {
            
            // if it's not a java item,
            // simply recurse its contents.
            if(!(exp instanceof JavaItem)) {
                // is this OK? looks potentially dangaerous
                // when this OtherExp is shared because
                // the modification can depend on the context.
                
                // but this is necessary to preserve OccurrenceExp
                exp.exp = exp.exp.visit(this);
                return exp;
            }
            
            
            // skip any JavaItem if it is in an ignored item.
            // this will effectively clone the entire descendants of the 
            // IgnoreItem.
            if( isIgnore(parentItem) )
                return exp.exp.visit(this);
            
            
            
            if( exp instanceof IgnoreItem ) {
                exp.exp = AnnotationRemover.remove(exp.exp,pool);
                multiplicity = Multiplicity.zero;
                return exp;
            }
            
            final JavaItem oldParent = parentItem;
            if( exp instanceof JavaItem ) {
                // this is a java item.
                
                // check if this relation is allowed.
                // several relationships are prohibited (like S-S).
                sanityCheck( parentItem, (JavaItem)exp );
                
                if( isClass(parentItem) && isField(exp) ) {
                    // this is a field to the parent class item.
                    FieldItem fi = (FieldItem)exp;
                    ((ClassItem)parentItem).getOrCreateFieldUse(fi.name).items.add(fi);
                }
                
                if( isField(parentItem) && (exp instanceof TypeItem) ) {
                    TypeItem ti = (TypeItem)exp;
                    FieldItem fi = (FieldItem)parentItem;
                    try {
                        fi.addType(ti);
                    } catch( FieldItem.BadTypeException e ) {
                        // this type contradicts the user's specification.
                        controller.reportError(
                            new Locator[]{fi.locator},
                            Messages.format(
                                Messages.ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE,
                                fi.name,
                                e.getUserSpecifiedType().name(),
                                ti.getType().name()));
                    }
                }
                
                if( exp instanceof ClassItem || !visitedClasses.add(exp)) {
                    multiplicity = getJavaItemMultiplicity(exp);
                    // this one is a java item and already processed.
                    // or this one is ClassItem (we visit them in the breath-first manner)
                    // so there is no need to traverse it again.
                    // to prevent infinite recursion, return immediately.
                    return exp;
                }
                
                // then change the parent item to this object.
                parentItem = (JavaItem)exp;
            }

            
            // visit children
            if( exp instanceof ExternalItem )
                exp.exp = AnnotationRemover.remove(exp.exp,pool);
            else
                exp.exp = exp.exp.visit(this);
            
            parentItem = oldParent;
            
            
            
            // make sure that this class item is defined properly.
            // this part of the code is executed only once per each JavaItem.
            
            if( isSuperClass(exp) ) {
                // super class item must have its definition.
                SuperClassItem sci = (SuperClassItem)exp;
                if( sci.definition==null ) {
                    controller.reportError(
                        new Expression[]{exp},
                        Messages.format(Messages.ERR_MISSING_SUPERCLASS_BODY) );
                }
                else {
                    // if we couldn't find the definition, do not report this error.
                    // S-C multiplicity must be (1,1)
                    if( !multiplicity.isUnique() ) {
                        controller.reportError(
                            new Expression[]{exp,sci.definition},
                            Messages.format(
                                Messages.ERR_BAD_SUPERCLASS_BODY_MULTIPLICITY,
                                new Object[]{sci.definition.name}));
                    }
                }
            }
            
            if( isField(exp) ) {
                // store the multiplicity of this field.
                // if the multiplicity is explicitly assigned to the
                // FieldItem, that value takes precedence.
                FieldItem fi = (FieldItem)exp;
                if(fi.multiplicity==null)
                    fi.multiplicity =  multiplicity;
                else {
                    // make sure that the explicit value is larger
                    // than the actual value.
                    //
                    // I don't know exactly what will be broken if
                    // this assertion fails, but it seems like
                    // this is really a bad sign.
                    _assert( fi.multiplicity.includes(multiplicity) );
                }
                
                // it is an error if the field contains no type object
                if(!fi.hasTypes()) {
                    controller.reportError(new Locator[]{fi.locator},
                        Messages.format(Messages.ERR_EMPTY_PROPERTY,
                            new Object[]{fi.name}));
                    try {
                        // recover by adding a new type object
                        fi.addType(grammar.createPrimitiveItem(
                                new IdentityTransducer(grammar.codeModel),
                                StringType.theInstance,
                                pool.createData(StringType.theInstance),
                                fi.locator) );
                    } catch (BadTypeException e) {
                        ; // just ignore
                    }
                }
            }
            
            if( isInterface(exp) ) {
                // I-I/I-C multiplicity must be (1,1)
                InterfaceItem ii = (InterfaceItem)exp;
                if( !multiplicity.isAtMostOnce() ) {
                    // DBG
                    System.out.println( com.sun.msv.grammar.util.ExpressionPrinter.printContentModel(exp.exp) );
                    controller.reportError(
                        new Expression[]{ii},
                        Messages.format(
                            Messages.ERR_BAD_INTERFACE_CLASS_MULTIPLICITY,
                            new Object[]{ ii.name }));
                }
                
                // InterfaceItem returns the multiplicity of its children.
                // so don't touch the multiplicity field
                return exp;
            }
            
            multiplicity = getJavaItemMultiplicity(exp);
            return exp;
        }

        private Multiplicity getJavaItemMultiplicity( OtherExp item ) {
            if( item instanceof IgnoreItem )    return Multiplicity.zero;
            else                                return Multiplicity.one;
        }
        
        /**
         * performs sanity check for the use of roles.
         */
        private void sanityCheck( JavaItem parent, JavaItem child ) {
            if( isSuperClass(parent) && !isClass(child) ) {
                // super-field, super-super, super-interface.
                controller.reportError(
                    new Expression[]{parent,child},
                    Messages.format(Messages.ERR_BAD_SUPERCLASS_USE));
                return;
            }
            
            if( isPrimitive(parent) ) {
                // primitive-*.
                // since PrimitiveItems are not specified by the user,
                // it must be an internal error.
                throw new Error("internal error: use of primitive-"+child+" relation.");
            }
            
            if(( isField(parent) && ( isSuperClass(child) || isField(child)) )
                || ( isInterface(parent) && ( isSuperClass(child) || isField(child) || isPrimitive(child) ) ) ) {
                // I-S, I-F, F-S, F-F, F-P relationship.
                // TODO: diagnose better
                controller.reportError(
                    new Expression[]{parent,child},
                    Messages.format(Messages.ERR_BAD_ITEM_USE,parent,child));
                return;
            }

            
            if( isClass(parent) && (child instanceof TypeItem) ) {
                // class-class, class-interface, or class-primitive relation.
                // FieldItemAnnotator should run before this process
                // to prevent such situations from happening in the normalizer.
                throw new Error("internal error. C-C/C-I/C-P relation "
                    + ((ClassItem)parent).getTypeAsDefined().name()+" "+
                    child.toString()+
                    " "+ com.sun.msv.grammar.util.ExpressionPrinter.printContentModel(parent) );
            }
        }
        
        /**
         * this set contains all visited ClassItem objects.
         * used to prevent infinite recursion.
         */
        private final Set visitedClasses = new HashSet();
        
        /**
         * this field holds the parent item object.
         */
        private JavaItem parentItem = null;
        
        /**
         * multiplicity from the current parent (either '1', '?', '+', or '*').
         * 
         * '1' means "exactly once", '?' means "zero or one", '+' means
         * "one or more", and '*' means "zero or more".
         * This value represents how many times this expression can be matched
         * for one parent item.
         * 
         * <p>
         * For example, consider the following expression:
         * <XMP>
         * <group> <-- parent
         *   <oneOrMore>
         *     <choice>
         *       <element/>  <-- child
         *       <element/>
         *     </choice>
         *   </oneOrMore>
         * </group>
         * </XMP>
         * 
         * for one parent item, child item can appear '*' times.
         */
        private Multiplicity multiplicity = null;
    }
    
    
    
    
    
    
    
    private static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }

        
    /**
     * @return true if t is not an interface
     */
    private static boolean isInterface(JType t) {
        if(t.isPrimitive())        return false;
        return ((JClass)t).isInterface();
    }
    
    
// type check utility methods.
//=================================================
    private static boolean isClass( Object exp ) {
        return exp instanceof ClassItem;
    }
    private static boolean isSuperClass( Object exp ) {
        return exp instanceof SuperClassItem;
    }
    private static boolean isInterface( Object exp ) {
        return exp instanceof InterfaceItem;
    }
    private static boolean isField( Object exp ) {
        return exp instanceof FieldItem;
    }
//    private static boolean isType( Object exp ) {
//        return exp instanceof TypeItem;
//    }
    private static boolean isPrimitive( Object exp ) {
        return exp instanceof PrimitiveItem;
    }
    private static boolean isIgnore( Object exp ) {
        return exp instanceof IgnoreItem;
    }


}
