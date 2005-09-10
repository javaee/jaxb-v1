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

import java.text.MessageFormat;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JLabel;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.BoundText;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * The base class for the unmarshaller event handler generator.
 * Generates an event handler that has a switch statement in it.
 * 
 * <p>
 * This class hides the detail of the actual
 * event handler generation (the entire event handler
 * won't be generated at all when the switch statement is empty)
 */
abstract class HandlerMethodGenerator {
    
    protected final PerClassGenerator parent;
    
    // references copied from the parent
    protected final JCodeModel codeModel;
    protected final boolean trace;
    protected final JVar $tracer;

    /** Name of the event handler to be generated. */
    protected final String methodName;
    
    /**
     * The class of {@link Alphabet} that this class handles.
     */
    private final Class alphabetType;
    
    /**
     * Transition table for the current automaton.
     */
    protected final TransitionTable table;
    
    
    protected HandlerMethodGenerator( PerClassGenerator _parent, String _mname, Class _alphabetType ) {
        this.parent = _parent;
        this.methodName = _mname;
        this.alphabetType = _alphabetType;
        this.codeModel = parent.parent.codeModel;
        this.trace = parent.parent.trace;
        this.$tracer = parent.$tracer;
        this.table = parent.transitionTable;
    }
    
    /**
     * Body of the case clause.
     * This field will be reset in each iteration.
     */
    private JBlock $case;
    
    /** Generates the case block for a given state. */
    protected JBlock getCase(State source) {
        if($case!=null)     return $case;
        
        // [RESULT]
        // case <state#>:
        $case = getSwitch()._case(
            JExpr.lit(parent.automaton.getStateNumber(source))).body();
        
        return $case;
    }
    
    /** Checks if the case block is generated for the given state. */
    protected boolean hasCase(State source) {
        return $case!=null;
    }
    
    private JSwitch $switch;
    
    private JVar $attIdx;

    /** Label used to handle state delegation. */
    private JLabel outerLabel;
    
    protected String getNameOfMethodDecl() {
        return methodName;
    }
    
    /**
     * Gets a switch statement object into which action handlers
     * are writtein.
     * 
     * This methods lazily creates a switch statement to avoid
     * creating a method if it's not necessary.
     */
    protected final JSwitch getSwitch() {
        if($switch!=null)   return $switch;
        
        // generate a method only when it is absolutely
        // necessary.
                    
        // [RESULT]
        // public void <methodName>( String uri, String local ) throws SAXException {
        //   int attIdx;  // used for attribute tests
        //
        //   outer:
        //   while(true) {
        //     switch(state) {
        //     }
        //     break;
        //   }
        JMethod method = parent.unmarshaller.method(
            JMod.PUBLIC, codeModel.VOID, getNameOfMethodDecl() );
        method._throws(SAXException.class);
        
        $attIdx = method.body().decl( codeModel.INT, "attIdx" ); 
        
        outerLabel = method.body().label("outer");
        JWhileLoop w = method.body()._while(JExpr.TRUE);
        
        $switch = makeSwitch(method,w.body());
        
        w.body()._break();
        
        return $switch;
    }
    
    /**
     * Creates a switch statement. Called from the getSwitch method.
     * 
     * Derived class can override this function to do extra
     * code generation.
     */
    protected JSwitch makeSwitch( JMethod method, JBlock parentBody ) {
        return parentBody._switch(parent.$state);
    }
    
    
    
//    private final Iterator filter( final Iterator alphabetIterato ) {
//        return new FilterIterator(alphabetIterato) {
//            protected boolean test(Object o) {
//                return alphabetType.isInstance(o)
//                    || o instanceof Alphabet.EnterAttribute;
//            }
//        };
//    }
    
    //
    // callback that populates the actual code into the method.
    //
    private void onState( State state, TransitionTable table ) {
        TransitionTable.Entry[] row = table.list(state);
        
        boolean canFallThrough = true;
        TransitionTable.Entry catchAll = null;
        
        for( int i=0; i<row.length && canFallThrough; i++ )  {
            Alphabet a = row[i].alphabet;
            
            if( alphabetType.isInstance(a) ) {
                // the alphabet is of the method specific type.
                canFallThrough = performTransition( state, a, row[i].transition );
            } else
            if( a.isEnterAttribute() ) {
                buildAttributeCheckClause( getCase(state), state, (Alphabet.EnterAttribute)a, row[i] );
            } else
            if( a.isDispatch() && alphabetType!=Alphabet.EnterAttribute.class && alphabetType!=Alphabet.LeaveAttribute.class ) {
                // dispatching will never happen inside attributes.
                generateDispatch( getCase(state), a.asDispatch(), row[i] );
            } else
            if( a==Alphabet.EverythingElse.theInstance ) {
                // any token would match this. So process this at the very end
                catchAll = row[i];
            }
        }
        
        if( canFallThrough && catchAll!=null )
            canFallThrough = performTransition(
                state, catchAll.alphabet, catchAll.transition );
        
        
        // if the flow still falls through, add terminator
        if( canFallThrough ) {
            if( state.getDelegatedState()!=null ) {
                // delegate to another state
                generateGoto( getCase(state), state.getDelegatedState() );
                // [RESULT]
                // continue outer;
                getCase(state)._continue(outerLabel);
            } else
            if( hasCase(state) ) {
                // add break statement
                getCase(state)._break();
            }
        }
    }
    
    /**
     * Generate the following code into the <code>block</code>.
     * 
     * <pre>
     * if(the current token matches <code>alphabet</code>) {
     *     take <code>transition</code>
     *     return;
     * }
     * </pre>
     * 
     * @param alphabet
     *      either <code>Alphabet.EverythingElse.theInstance</code>
     *      or an alphabet of the type specified by the {@link #alphabetType}.
     * @param state
     *      source state
     * 
     * @return
     *      true if it the flow can fall through the if block.
     */
    protected abstract boolean performTransition(
        State state, Alphabet alphabet, Transition action );


    /** Generates the event handler. */
    protected final void generate() {
        
        Iterator itr = parent.automaton.states();
        while(itr.hasNext()) {
            $case=null;  // reset the value
            onState( (State)itr.next(), table );
        }
    }
    
    /**
     * Adds event handler specific parameters to the specified
     * method invocation. This method is used to invoke
     * spanChildFromXXXX or revertToParentFromXXXX.
     */
    protected abstract void addParametersToContextSwitch( JInvocation inv );
    
    protected final String capitalize() {
        return Character.toUpperCase(methodName.charAt(0))
            +methodName.substring(1);
    }
    
    /**
     * Generates an action of reverting to the parent.
     */
    protected final void generateRevertToParent( JBlock $body ) {
        if(trace) {
            // [RESULT]
            // tracer.onRevertToParent();
            $body.invoke( $tracer, "onRevertToParent" );
        }
            
        // [RESULT]
        // revertToParentFromXXXX( ... );
        // return;
        JInvocation inv =
            $body.invoke("revertToParentFrom"+capitalize());
        
        addParametersToContextSwitch(inv);
        
        $body._return();
    }
    
    protected void generateSpawnChildFromExternal(
        JBlock $body, Transition tr, JExpression memento ) {
        
        // external object unmarshalling can start only from the enterElement handler.
        // because an external object always corresponds to an element.
        _assert(false);
    }
    
    /**
     * Generates an action of spawning a new child.
     */
    protected final void generateSpawnChild( JBlock $body, Transition tr ) {
        _assert(tr.alphabet instanceof Alphabet.Reference);
        
        JExpression memento = JExpr.lit(parent.automaton.getStateNumber(tr.to));
        
        
        if(tr.alphabet instanceof Alphabet.External) {
            generateSpawnChildFromExternal($body,tr,memento);
        } else
        if(tr.alphabet instanceof Alphabet.Interleave) {
            // [RESULT]
            // spawnHandlerFromXXX( new InterleaveDispatcher(), ... );
            Alphabet.Interleave ia = (Alphabet.Interleave)tr.alphabet;

            JInvocation $inv = $body.invoke(
                "spawnHandlerFrom"+capitalize())
                .arg(JExpr._new(parent.getInterleaveDispatcher(ia)))
                .arg(memento);
            addParametersToContextSwitch($inv);
            $body._return();
        } else
        if(tr.alphabet.isDispatch()) {
            // this is a special attribute and treated as such in the
            // buildAttributeCheckClause method.
            ;
        } else {
            // [RESULT]
            // <addToField>((childClass)spawnChildFromXXXX(
            //     <child class>,<no>, ... ));
            // return;
            Alphabet.StaticReference sr = (Alphabet.StaticReference)tr.alphabet;
            
            // type of the child object
            JClass childType = sr.target.getOwner().implRef;

            if(tr.alphabet instanceof Alphabet.SuperClass) {
                
                if(trace) {
                    // [RESULT]
                    // tracer.onSpawnSuper(<childType>)\n");
                    // tracer.suspend();
                    $body.invoke( $tracer, "onSpawnSuper" )
                        .arg(JExpr.lit(childType.name()));
                    $body.invoke( $tracer, "suspend" );
                }
            
                JInvocation $inv = $body.invoke(
                    "spawnHandlerFrom"+capitalize())
                    .arg(JExpr.direct(MessageFormat.format(
                        "(({0}){1}.this).new Unmarshaller(context)",
                        new Object[]{
                            childType.fullName(),   // base class name
                            parent.context.implClass.fullName(),    // name of this class
                        }
                    )))
                    .arg(memento);
                addParametersToContextSwitch($inv);
                $body._return();
            } else {
                Alphabet.Child c = (Alphabet.Child)tr.alphabet;
            
                if(trace) {
                    // [RESULT]
                    // tracer.onSpawnChild(<childType>,<field>);
                    // tracer.suspend();
                    $body.invoke( $tracer, "onSpawnChild" )
                        .arg(JExpr.lit(childType.name()))
                        .arg(JExpr.lit(c.field.getFieldUse().name));
                    $body.invoke( $tracer, "suspend" );
                }
                
                JInvocation $childObj =
                    JExpr.invoke("spawnChildFrom"+capitalize())
                        .arg( JExpr.dotclass(childType) )
                        .arg( memento );
            
                addParametersToContextSwitch($childObj);
            
                c.field.setter($body, JExpr.cast(childType,$childObj) );
            
                $body._return();
            }
        }
    }
    
    protected final void generateGoto( JBlock $body, State target ) {
        parent.generateGoto($body,target);
    }
    

    /**
     * Generates code that checks the existence of an
     * attribute and start a transition by that attribute
     * if it exists.
     * 
     */
    private void buildAttributeCheckClause(
        JBlock body, State current, Alphabet.EnterAttribute alphabet, TransitionTable.Entry tte ) {
        
        NameClass nc = alphabet.name;
        
        JExpression $context = JExpr.ref("context");
       
        if(nc instanceof SimpleNameClass) {
            // if the name class is a simple name class
            // (99% of the case)
            SimpleNameClass snc = (SimpleNameClass)nc;
        
            // [RESULT]
            // idx = context.getAttribute(...);
                
            body.assign(
                $attIdx,
                $context.invoke("getAttribute")
                    .arg(JExpr.lit(snc.namespaceURI))
                    .arg(JExpr.lit(snc.localName)));
        } else {
            // otherwise if the name is complex
            JBlock b = body.block();
            // [RESULT]
            // {
            //     Attributes a = context.getUnconsumedAttributes();
            //     for(idx=a.getLength()-1;idx>=0;idx--) {
            //         String uri = a.getURI(idx);
            //         String local = a.getLocalName(idx);
            //         if(<name test with uri,local>)
            //             break;
            //     }
            // }
            
            JVar $a = b.decl(
                codeModel.ref(org.xml.sax.Attributes.class),
                "a",
                $context.invoke("getUnconsumedAttributes"));
            JForLoop loop = b._for();
            loop.init($attIdx,
                JExpr.invoke($a,"getLength").minus(JExpr.lit(1)));
            loop.test( $attIdx.gte(JExpr.lit(0)) );
            loop.update( $attIdx.decr() );
            
            JType str = codeModel.ref(String.class);
            JVar $uri   = loop.body().decl(str,"uri"  ,$a.invoke("getURI"      ).arg($attIdx));
            JVar $local = loop.body().decl(str,"local",$a.invoke("getLocalName").arg($attIdx));
            
            loop.body()._if(parent.parent.generateNameClassTest(nc,$uri,$local))
                ._then()._break();
        }

        // [RESULT]
        // if(idx>=0) {
        //     ... handle this attribute ...
        // }
        JBlock _then = body._if(
            $attIdx.gte(JExpr.lit(0)))._then();
        
        AttOptimizeInfo aoi = calcOptimizableAttribute(tte);
        if(aoi==null) {
            // [RESULT]
            // if(...) {
            //     context.consumeAttribute(...); // consume this attribute
            //     getCurrentHandler().XXXX(...); // re-route this token
            //     return;
            // }
        
            _then.invoke($context,"consumeAttribute").arg($attIdx);
            // re-route the current token
            addParametersToContextSwitch(
                _then.invoke($context.invoke("getCurrentHandler"),methodName));
            _then._return();
        } else {
            // [RESULT]
            // if(...) {
            //     String v = context.eatAttribute(idx);
            //     <go to next state>;
            //     <parse the attribute value>;
            //     continue outer;
            // }
            JVar $v = _then.decl(JMod.FINAL, codeModel.ref(String.class),"v",
                $context.invoke("eatAttribute").arg($attIdx));
            generateGoto( _then, aoi.nextState );
            parent.eatText( _then, aoi.valueHandler, $v );
            // go to the next state first so that the error in the attribute value parsing
            // won't affect the state transition
            
            // UGLY.
            // See java.net issue 6. Sometimes re-routing of a token
            // is necessary, for example when the new state has different
            // list processing semantics than the current state.
            if( aoi.nextState.isListState ^ current.isListState ) {
                addParametersToContextSwitch(
                        _then.invoke($context.invoke("getCurrentHandler"),methodName));
                _then._return();
            } else {
                _then._continue(outerLabel);
            }
        }
    }
    
    /**
     * Generates code fragment that checks if we can take
     * the transition by dispatching.
     */
    private void generateDispatch(
        JBlock $body, Alphabet.Dispatch da, TransitionTable.Entry tte ) {
        // [RESULT]
        // {
        //     Class child = <lookupTable>(attIndex);
        //     if(child!=null) {
        //         <addToField>(spawnChild( child, <no>, ... ));
        //         return;
        //     }
        // }


        JBlock block = $body.block();
        
        JVar $childType = block.decl(codeModel.ref(Class.class),"child",
            parent.invokeLookup(da,tte));
        
            block = block._if($childType.ne(JExpr._null()))._then();
        
        if(trace) {
            // [RESULT]
            // tracer.onDispatch(<attName>,<field>);
            // tracer.suspend();
            block.invoke( $tracer, "onSpawnChild" )
                .arg(JExpr.lit('{'+da.attName.namespaceURI+'}'+da.attName.localName))
                .arg(JExpr.lit(da.field.getFieldUse().name));
            block.invoke( $tracer, "suspend" );
        }
        
        JInvocation $childObj =
            JExpr.invoke("spawnChildFrom"+capitalize())
                .arg( $childType )
                .arg( JExpr.lit(parent.automaton.getStateNumber(tte.transition.to)) );
    
        addParametersToContextSwitch($childObj);
    
        da.field.setter(block, JExpr.cast(da.field.getFieldUse().type,$childObj) );
    
        block._return();
    }
    
    /**
     * 
     * @see HandlerMethodGenerator#calcOptimizableAttribute(TransitionTable.Entry)
     */
    private static class AttOptimizeInfo {
        /**
         * Coresponds to 'bound text' in the picture below.
         */
        public final Alphabet.BoundText valueHandler;
        /**
         * Corresponds to 's4' in the picture below.
         */
        public final State nextState;
        
        AttOptimizeInfo(BoundText _valueHandler, State _nextState) {
            this.valueHandler = _valueHandler;
            this.nextState = _nextState;
        }
    }
    
    /**
     * Returns non-null if this attribute transition entry is optimizable.
     * 
     * The optimization strategy is as follows.
     * We look for an attribute transition of the form:
     *
     *              bound
     *    @foo      text     /@foo    
     * s1 ----> s2 ----> s3 ----> s4
     * 
     * where s2 and s3 do not have any other transition.
     * <p>
     * Most of the attribute transition falls into this category, and
     * this form essentially guarantees that the attribute will be
     * consumed by the same JAXB object.
     * 
     * Since we know how the attribute is unmarshalled, it's much easier
     * to just consume the value here, rather than firing three extra
     * events.
     * 
     * @return
     *      the object that represents this three step transitions
     *      if the parameter 'tte' qualifies as the transition from 's1'.
     *      Otherwise null.
     */
    private AttOptimizeInfo calcOptimizableAttribute( TransitionTable.Entry tte ) {
        if( !tte.transition.alphabet.isEnterAttribute() )
            return null;
        
        Transition[] hop1 = tte.transition.to.listTransitions();
        if( hop1.length!=1 )
            return null;
        
        Transition t1 = hop1[0];
        if( !t1.alphabet.isBoundText() )
            return null;
        
        Transition[] hop2 = t1.to.listTransitions();
        if( hop2.length!=1 )
            return null;
        
        Transition t2 = hop2[0];
        if( !t2.alphabet.isLeaveAttribute() )
            return null;
        
        return new AttOptimizeInfo( t1.alphabet.asBoundText(), t2.to );
    }

    
    protected static final void _assert( boolean b ) {
        if(!b) 
            throw new JAXBAssertionError();
    }
}




