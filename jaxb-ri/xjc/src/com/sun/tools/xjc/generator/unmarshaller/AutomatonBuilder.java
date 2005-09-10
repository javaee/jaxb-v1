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
package com.sun.tools.xjc.generator.unmarshaller;

import java.util.Map;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.ConcurExp;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionVisitor;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.ListExp;
import com.sun.msv.grammar.MixedExp;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.OneOrMoreExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.util.ExpressionFinder;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.LookupTableUse;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.JavaItem;
import com.sun.tools.xjc.grammar.JavaItemVisitor;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.util.GroupFinder;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Constructs an automaton from a ClassItem.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class AutomatonBuilder
{
    /**
     * Constructs the body of the specified automaton.
     * 
     * @param automata
     *      A map from ClassItem to Automaton.
     */
    public static void build( Automaton a, GeneratorContext context, Map automata ) {
        ClassItem ci = a.getOwner().target;
        a.setInitialState( (State)ci.exp.visit(
            new AutomatonBuilder(ci,context,automata).normal));
    }
    
    /** The current ClassItem we are visiting. */
    private final ClassItem classItem;
    
    private final GeneratorContext context;
    
    /** Map from ClassItem to Automaton. */
    private final Map otherAutomata;
    private Automaton getAutomaton( ClassItem ci ) {
        // assert(otherAutomata.containsKey(ci));
        return (Automaton)otherAutomata.get(ci);
    }
    
    // not creatable from other classes
    private AutomatonBuilder( ClassItem ci, GeneratorContext _context, Map _automata ) {
        tail = new State();
        tail.markAsFinalState();
        this.classItem = ci;
        this.context = _context;
        this.otherAutomata = _automata;
    }
    
    private State tail;
    
    /**
     * Used to generate ID number for alphabets.
     * attributes that appears earlier will get a larger number.
     * 
     * <p>
     * The number will then later used to decide order we examine
     * transitions.
     */
    private int idGen=0;
    
    
    /**
     * Common implementation for all "modes" of automaton building.
     * Specialized builders are derived from this class.
     * 
     * Right now, we have two modes, one for normal automaton building
     * and the other for handling IgnoreItem.
     */
    private abstract class Base implements ExpressionVisitor {
    
        public Object onRef( ReferenceExp exp ) {
            return exp.exp.visit(this);
        }
    
        public Object onOther( OtherExp exp ) {
            if( exp instanceof OccurrenceExp )
                return onOccurence( (OccurrenceExp)exp );
            else
                return exp.exp.visit(this);
        }

        public Object onEpsilon() {
            return tail;
        }
        
        /**
         * Returns true if the given expression contains text nodes.
         * (In other words, if the content model is *NOT* element-only.
         */
        private boolean contansText( Expression exp ) {
            // see if this element has an element content model
            // or #PCDATA content model
            return exp.visit(textFinder);
        }
        public Object onElement( ElementExp exp ) {
            // just one id is suffice since those two alphabets don't appear
            // in the same state
            int idx=idGen++;
            return onItem( exp,
                new Alphabet.EnterElement(idx,exp.getNameClass(),contansText(exp.getContentModel())),
                new Alphabet.LeaveElement(idx,exp.getNameClass()) );
        }
    
        public Object onAttribute( AttributeExp exp ) {
            // just one id is suffice since those two alphabets don't appear
            // in the same state
            int idx = idGen++;
            return onItem( exp,
                new Alphabet.EnterAttribute(idx,exp.getNameClass()),
                new Alphabet.LeaveAttribute(idx,exp.getNameClass()) );
            
        }
    
        private State onItem( NameClassAndExpression exp, Alphabet s, Alphabet e ) {
            State newTail = new State();
            newTail.addTransition( new Transition( e, tail ));
            
            tail = newTail;
            
            State contentHead = (State)exp.getContentModel().visit(this);
            
            State head = new State();
            head.addTransition( new Transition( s, contentHead ));
            
            return head;
        }
    
        public Object onInterleave( InterleaveExp exp ) {
            Expression[] children = exp.getChildren();
            State currentTail = tail;
            
            if( isInterleaveOptimizable(children) ) {
                // treat it as if <zeroOrMore><choice>.
                // this will reduce the size of the automaton, and it will
                // always work for XML Schema 1.0                
                State head = new State();
                
                for( int i=0; i<children.length; i++ ) {
                    tail = currentTail;
                    head.absorb( (State)children[i].visit(this) );
                }
                
                currentTail.absorb(head);
                
                // return head if you want to treat it as if <oneOrMore><choice>
                return currentTail;
            } else {
                // build sub-automata
                Alphabet.Interleave.Branch[] branches = new Alphabet.Interleave.Branch[children.length];
                for( int i=0; i<children.length; i++ ) {
                    tail = new State();
                    tail.markAsFinalState();
                    
                    branches[i] = new Alphabet.Interleave.Branch(
                        (State)children[i].visit(this),
                        children[i] );
                }
                
                State head = new State();
                head.addTransition(new Transition(
                    new Alphabet.Interleave( branches, idGen++ ),
                    currentTail ));
                
                return head;
            }
        }
        
        private boolean isInterleaveOptimizable( Expression[] children ) {
            for( int i=0; i<children.length; i++ )
                if( GroupFinder.find(children[i]) )
                    return false;   // can't optimize
            return true;
        }
        
        
        public Object onSequence( SequenceExp exp ) {
            Expression[] children = exp.getChildren();
            
            for( int i=children.length-1; i>=0; i-- )
                tail = (State)children[i].visit(this);
            
            return tail;
        }
    
        public Object onChoice( ChoiceExp exp ) {
            Expression[] children = exp.getChildren();
            
            State currentTail = tail;
            State head = new State();
            
            for( int i=children.length-1; i>=0; i-- ) {
                tail = currentTail;
                State localHead = (State)children[i].visit(this);
                if( localHead==currentTail )
                    continue;   // use delegation to produce a smaller state machine
                head.absorb( localHead );
            }
            
            if( exp.isEpsilonReducible() ) {
                // optimization
                if( head.hasTransition() )
                    head.setDelegatedState(currentTail);
                else
                    head = currentTail;
            }
            
            return head;
        }
    
        public Object onOneOrMore( OneOrMoreExp exp ) {
            return _onRepeated( exp.exp, false );
        }
        
        private State onOccurence(OccurrenceExp exp) {
            return _onRepeated( exp.itemExp, exp.minOccurs==0 );
        }
        
        private State _onRepeated( Expression itemExp, boolean isZeroAllowed ) {
            State _tail = tail;
            State newHead = (State)itemExp.visit(this);
            
            _tail.absorb(newHead);
            return isZeroAllowed?_tail:newHead;
        }
    
        public Object onList( ListExp exp ) {
            State head = (State)exp.exp.visit(this);
            // make it a list state
            head.isListState = true;
            return head;
        }

        public Object onNullSet() {
            return new State();
        }
    
        // shouldn't be in BGM at all
        public Object onMixed(MixedExp exp)     { throw new JAXBAssertionError(); }
    
        // not supported
        public Object onConcur(ConcurExp exp)   { throw new JAXBAssertionError(); }
    }
    
    private final ExpressionVisitor normal = new Normal();
    
    /**
     * Automaton builder which will be used initially.
     * This works for a normal content model where we expect
     * JavaItems.
     */
    private class Normal extends Base implements JavaItemVisitor {
        private Normal() {}
        
        public Object onOther( OtherExp exp ) {
            if(exp instanceof JavaItem) {
                return ((JavaItem)exp).visitJI(this);
            } else {
                return super.onOther(exp);
            }
        }
        
        public Object onChoice( ChoiceExp exp ) {
            // check the switch attribute optimization.
            LookupTableUse use = context.getLookupTableBuilder().buildTable(exp);
            
            if( use!=null ) {
                State head = new State();
                
                if( use.anomaly!=null ) {
                    head.absorb( (State) use.anomaly.visit(this) );
                }
                
                head.addTransition(new Transition(
                    new Alphabet.Dispatch(use,context.getField(currentField),idGen++),
                    tail));
                
                return head;
            } else {
                return super.onChoice(exp);
            }
        }
    
        public Object onIgnore( IgnoreItem item ) {
            // automaton will be created for the contents of IgnoreItem, too.
            // but we use a different mode because of the difference.
            return item.exp.visit(inIgnoredItem);
        }
        // this field is set to non-null when visiting inside a FieldItem
        private FieldUse currentField = null;
    
        public Object onField( FieldItem fi ) {
            if(currentField!=null)  throw new JAXBAssertionError(); // assert
            currentField = classItem.getField(fi.name);
            if(currentField==null)  throw new JAXBAssertionError(); // assert
            Object r = fi.exp.visit(this);
            currentField = null;
            return r;
        }
        public Object onInterface( InterfaceItem item ) {
            return item.exp.visit(this);
        }
    
        // this flag is set to true when visiting inside a SuperClassItem.
        private boolean inSuperClass = false;
        public Object onSuper( SuperClassItem item ) {
            inSuperClass = true;
            Object ret = item.exp.visit(this);
            inSuperClass = false;
            return ret;
        }
        public Object onExternal( ExternalItem item ) {
            State head = new State();
            
            Alphabet a;
            
            if(currentField==null)
                // assertion failed. It must be wrapped by a FieldItem
                throw new JAXBAssertionError();
            a = new Alphabet.External( item,
                    context.getField(currentField),idGen++);
            
            head.addTransition( new Transition(a,tail) );
            return head;
        }
        public Object onClass( ClassItem item ) {
            State head = new State();
            
            Alphabet a;
            if(inSuperClass)
                a = new Alphabet.SuperClass(getAutomaton(item),idGen++);
            else {
                if(currentField==null)
                    // assertion failed. It must be wrapped by a FieldItem
                    throw new JAXBAssertionError();
                a = new Alphabet.Child(
                    getAutomaton(item),
                    context.getField(currentField),idGen++);
            }
            
            head.addTransition( new Transition(a,tail) );
            return head;
        }
        public Object onPrimitive( PrimitiveItem item ) {
            if(currentField==null)
                // assertion failed. It must be wrapped by a FieldItem
                throw new JAXBAssertionError();
                
            State head = new State();
            head.addTransition( new Transition(
                new Alphabet.BoundText(idGen++,item,context.getField(currentField)), tail ));
            return head;
        }
        
        // these expressions should be wrapped by PrimitiveItem
        public Object onValue(ValueExp exp)     { throw new JAXBAssertionError(); }
        public Object onData(DataExp exp)       { throw new JAXBAssertionError(); }
        public Object onAnyString()             { throw new JAXBAssertionError(); }
    }
    
    
    /**
     * Automaton builder for the contents of {@link IgnoreItem}.
     * In this context, we may see expressions like text,data, and value.
     */
    private final ExpressionVisitor inIgnoredItem = new Ignored();
    private class Ignored extends Base {
        
        public Object onAttribute( AttributeExp exp ) {
            // since we go out to check if the attribute is present,
            // if we are going to ignore an attribute completely,
            // then there's no need to generate a state machine for it at all.
            // so just ignore it completely.
            return super.onEpsilon();
        }
        
        public Object onValue(ValueExp exp) {
            return createIgnoredTextTransition();
        }
        public Object onData(DataExp exp) {
            return createIgnoredTextTransition();
        }
        public Object onAnyString() {
            return createIgnoredTextTransition();
        }
    
        public Object onList( ListExp exp ) {
            // we are going to ignore the whole list,
            // so just treat it as one chunk of text and ignore all.
            return createIgnoredTextTransition();
        }
        
        private State createIgnoredTextTransition() {
            State head = new State();
            head.addTransition( new Transition(
                new Alphabet.IgnoredText(idGen++), tail ));
            return head;
        }
    };

        
    /**
     * Returns true if the given expression contains text nodes.
     * (In other words, if the content model is *NOT* element-only.
     */
    private static final ExpressionFinder textFinder = new ExpressionFinder() {
        public boolean onElement(ElementExp exp) {
            return false;
        }
        public boolean onAttribute(AttributeExp exp) {
            return false;
        }
        public boolean onData(DataExp exp) {
            return true;
        }
        public boolean onValue(ValueExp exp) {
            return true;
        }
        public boolean onAnyString() {
            return true;
        }
    };
        
}
