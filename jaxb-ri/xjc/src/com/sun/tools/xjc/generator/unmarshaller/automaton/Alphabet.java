/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.xjc.generator.unmarshaller.automaton;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.generator.LookupTable;
import com.sun.tools.xjc.generator.LookupTableUse;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.util.NameFinder;
import com.sun.tools.xjc.grammar.util.TextFinder;

public abstract class Alphabet
{
    /**
     * All the alphabets are numbered according to their order of
     * appearance in the schema. This determines the order transitions
     * are tested.
     */
    public final int order;
    /*
     * Some types of alphabets have unique "order number", which
     * dictates the order of processing.
     * 
     * <p>
     * It is used for EnterAttribute/Reference alphabets.
     * Consider the execution of an automaton at run-time.
     * Imagine we are in a certain state, which has two transitions
     * by attributes and both of them are present in our current element.
     * <p>
     * In cases like this, we need to pick the right transition, or
     * otherwise we will lose a chance to process some of attributes.
     * The order number is used in this case. When two conflicting
     * attribute transitions are present, we will pick a transition
     * with a bigger number.
     */
    
    public Alphabet( int _order ) {
        this.order = _order;
    }
    
    // cast functions. you still need to check the type before you call this method.
    // we won't return null
    public final Named asNamed() { return (Named)this; }
    public final Reference asReference() { return (Reference)this; }
    public final StaticReference asStaticReference() { return (StaticReference)this; }
    public final Text asText() { return (Text)this; }
    public final BoundText asBoundText() { return (BoundText)this; }
    public final Dispatch asDispatch() { return (Dispatch)this; }
    
    public final boolean isReference() { return this instanceof Reference; }
    public final boolean isEnterAttribute() { return this instanceof EnterAttribute; }
    public final boolean isLeaveAttribute() { return this instanceof LeaveAttribute; }
    public final boolean isText() { return this instanceof Text; }
    public final boolean isNamed() { return this instanceof Named; }
    public final boolean isBoundText() { return this instanceof BoundText; }
    public final boolean isDispatch() { return this instanceof Dispatch; }
    
    // visitor support
    public abstract void accept( AlphabetVisitor visitor );
    protected abstract void accept( TransitionVisitor visitor, Transition t ); 


    
    
    public static abstract class Named extends Alphabet {
        public final NameClass name;
        public Named( int _order, NameClass _name ) {
            super(_order);
            this.name = _name;
        }
    }
    
        
    public final static class EnterElement extends Named {
        /**
         * True if this element has text node.
         * False if this element has an element-only content model.
         */
        public final boolean isDataElement;
        public EnterElement( int _order, NameClass name, boolean dataElement ) {
            super(_order,name);
            isDataElement = dataElement;
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onEnterElement(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onEnterElement(this, t.to ); 
        }
        public String toString() { return '<'+name.toString()+'>'; }
    }
        
    public final static class LeaveElement extends Named {
        public LeaveElement( int _order, NameClass name ) { super(_order,name); }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onLeaveElement(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onLeaveElement(this, t.to ); 
        }
        public String toString() { return "</"+name.toString()+'>'; }
    }
        
    public final static class EnterAttribute extends Named {
        public EnterAttribute( int _order, NameClass name ) {
            super(_order,name);
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onEnterAttribute(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onEnterAttribute(this, t.to ); 
        }
        public String toString() { return '@'+name.toString(); }
    }
        
    public final static class LeaveAttribute extends Named {
        public LeaveAttribute( int _order, NameClass name ) { super(_order,name); }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onLeaveAttribute(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onLeaveAttribute(this, t.to ); 
        }
        public String toString() { return "/@"+name.toString(); }
    }
    
    
    
    
    public static abstract class Reference extends Alphabet {
        public Reference( int _order ) { super(_order); }
        
        /**
         * Returns true if the target of this referene can unmarshall
         * itself from the empty sequence of events.
         */
        public abstract boolean isNullable();
        
        public final Set head( boolean includeEE ) {
            TreeSet r = new TreeSet(OrderComparator.theInstance);
            head( r, new HashSet(), includeEE );
            return r;
        }
        
        /**
         * See the State#head method. Gets the HEAD alphabet set of
         * the target of this reference.
         */
        public abstract void head( Set result, Set visitedStates, boolean includeEE );
    }
    
    /** Interleave fork. */
    public final static class Interleave extends Reference {
        public static final class Branch {
            public final State initialState;
            public final NameClass elementName;
            public final NameClass attributeName;
            public final boolean hasText;
            
            public Branch( State s, Expression e ) {
                this.initialState = s;
                elementName = NameFinder.findElement(e);
                attributeName = NameFinder.findAttribute(e);
                hasText = TextFinder.find(e);
            }
            public NameClass getName( int idx ) {
                if(idx==0)  return elementName;
                else        return attributeName;
            }
        }

        public final Branch[] branches;

        public Interleave( Branch[] _branches, int _order ) {
            super(_order);
            this.branches = _branches;
        }
        
        /**
         * Returns true if the entire interleave is nullable.
         */
        public boolean isNullable() {
            for( int i=0; i<branches.length; i++ )
                if( !branches[i].initialState.isFinalState() )
                    return false;
            return true;
        }
        
        public int getTextBranchIndex() {
            for( int i=0; i<branches.length; i++ )
                if( branches[i].hasText )
                    return i;
            return -1;
        }
        
        public void head( Set result, Set visitedStates, boolean includeEE ) {
            for( int i=0; i<branches.length; i++ )
                branches[i].initialState.head(result,visitedStates,includeEE);
        }

        public void accept( AlphabetVisitor visitor ) {
            visitor.onInterleave(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onInterleave(this, t.to ); 
        }
        
        public String toString() {
            return "interleave";
        }
    } 
    
    /** References whose targets are determined at the compile time. */
    public static abstract class StaticReference extends Reference {
        public final Automaton target;
        public StaticReference( Automaton ta, int _order ) {
            super(_order);
            this.target=ta;
        }
        public final boolean isNullable() {
            return target.isNullable();
        }
        public void head( Set result, Set visitedStates, boolean includeEE ) {
            target.getInitialState().head(result,visitedStates,includeEE);
        }
    }
    
    public final static class Child extends StaticReference {
        public final FieldRenderer field;
        public Child( Automaton ta, FieldRenderer _field, int order ) {
            super(ta,order);
            this.field=_field;
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onChild(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onChild(this, t.to ); 
        }
        public String toString() { return "child["+target.getOwner().target.name+"]"; }
    }
    
    public final static class SuperClass extends StaticReference {
        public SuperClass( Automaton ta, int order ) { super(ta,order); }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onSuper(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onSuper(this, t.to ); 
        }
        public String toString() { return "super["+target.getOwner().target.name+"]"; }
    }
    
    /**
     * Special alphabet that represents the dispatch based on a
     * switch attribute. 
     */
    public final static class Dispatch extends Alphabet {
        public final LookupTable table;
        public final SimpleNameClass attName;
        public final FieldRenderer field;
        
        public Dispatch(LookupTableUse _tableUse, FieldRenderer _field, int _order ) {
            super(_order);
            this.table = _tableUse.table;
            this.attName = _tableUse.switchAttName;
            this.field = _field;
        }

        protected void accept(TransitionVisitor visitor, Transition t) {
            visitor.onDispatch(this,t.to);
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onDispatch(this); 
        }
        
        public String toString() { return "dispatch[@"+attName+"]"; }
    }
    
    public final static class External extends Reference {
        /** The object this alphabet is refering to. */
        public final ExternalItem owner;

        public final FieldRenderer field;
        
        public External( ExternalItem _owner, FieldRenderer _field, int _order ) {
            super(_order);
            this.owner = _owner;
            this.field = _field;
            
            head = new EnterElement(_order,owner.elementName,false);
        }
        
        private final EnterElement head;
        
        public boolean isNullable() { return false; }
        public void head( Set result, Set visitedStates, boolean includeEE ) {
            result.add(head);
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onExternal(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onExternal(this, t.to ); 
        }
        public String toString() { return "external["+owner.toString()+"]"; }
    }
    
    
    
    /**
     * Transition by a "text" event.
     */
    public static abstract class Text extends Alphabet {
        public Text(int _order) {
            super(_order);
        }
    }
    
    /**
     * Alphabet that indicates the given text will be used.
     */
    public final static class BoundText extends Text {
        public final PrimitiveItem item;
        public final FieldRenderer field;
        
        public BoundText( int _order, PrimitiveItem _item, FieldRenderer _field ) {
            super(_order);
            this.item=_item;
            this.field=_field;
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onBoundText(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onBoundText(this, t.to ); 
        }
        public String toString() { return "text"; }
    }
    
    
    /**
     * Alphabet that indicates the given text will <b>NOT</b> be used.
     */
    public final static class IgnoredText extends Text {
        public IgnoredText(int _order) {
            super(_order);
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onIgnoredText(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onIgnoredText(this, t.to ); 
        }
        public String toString() { return "ignoredText"; }
    }
    
    /**
     * Special token that indicates the epsilon transition.
     * This transition matches to any input token but doesn't consume
     * the token.
     * 
     * The order of this alphabet guarantees that it will be
     * taken only after all the other transitions are tried.
     */
    public final static class EverythingElse extends Alphabet {
        private EverythingElse() {
            super(-1);
        }
        public void accept( AlphabetVisitor visitor ) {
            visitor.onEverythingElse(this); 
        }
        protected void accept( TransitionVisitor visitor, Transition t ) {
            visitor.onEverythingElse(this, t.to ); 
        }
        public static final Alphabet theInstance = new EverythingElse();
        public String toString() { return "*"; }
    }
}
