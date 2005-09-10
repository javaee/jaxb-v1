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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.XMLDeserializerContextImpl;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;
import com.sun.tools.xjc.runtime.AbstractUnmarshallingEventHandlerImpl;
import com.sun.tools.xjc.runtime.InterleaveDispatcher;
import com.sun.tools.xjc.runtime.UnmarshallableObject;
import com.sun.tools.xjc.runtime.UnmarshallingContext;
import com.sun.tools.xjc.runtime.UnmarshallingEventHandler;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.unmarshaller.Tracer;

/**
 * Generates an unmarshaller for one class.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class PerClassGenerator {

// context information
    final UnmarshallerGenerator parent;

    private final JCodeModel        codeModel;
    
    final ClassContext      context;
        
    final Automaton         automaton;
    final JDefinedClass     unmarshaller;
    /** Unmarshaller.state variable. */
    final JFieldRef         $state;
    /** Unmarshaller.context variable. */
    final JFieldRef         $context;
    /** Context wrapper. */
    private final DeserializerContext dc;
    
    /** State transition table for the current unmarshaller. */
    final TransitionTable   transitionTable;
    
    /**
     * If the trace mode is on, this field keeps a reference to
     * the Tracer object
     */
    JVar         $tracer;
    
    /** sequence number generator. Used to generate id. */
    private int idGen = 0;
    
    
    public int createId() { return ++idGen; }
    
// internal states
    /**
     * Map from {@link Alphabet.Interleave} to {@link JDefinedClass}
     * that stores all the generated
     * {@link InterleaveDispatcher} class.
     */
    private final Map interleaveDispatcherImpls = new HashMap();
    
    
    PerClassGenerator( UnmarshallerGenerator _parent, Automaton a ) {
        this.parent = _parent;
        this.codeModel = parent.codeModel;
        this.context = a.getOwner();
        automaton = a;
        
        JDefinedClass impl = context.implClass;
    
        impl._implements( getRuntime(UnmarshallableObject.class) );
    
        // generate inner unmarshaller class.
        unmarshaller = parent.context.getClassFactory().createClass(impl,"Unmarshaller",null);
        unmarshaller._extends(getRuntime(AbstractUnmarshallingEventHandlerImpl.class));
        
        {// implement the abstract owner method.
            // [RESULT]
            // public Object owner() {
            //    return <OwnerClass>.this;
            // }
            JMethod method = unmarshaller.method(JMod.PUBLIC, Object.class, "owner" );
            method.body()._return( impl.staticRef("this") );
        }
        
        $state = JExpr.ref("state");
        $context = JExpr.ref("context");
        dc = new XMLDeserializerContextImpl($context);

        // generate the getUnmarshaller method
        // [RESULT]
        // UnmarshallingEventHandler createUnmarshaller( UnmarshallingContext context ) {
        //     return new Unmarshaller(context);
        // }
        JMethod method = impl.method(
            JMod.PUBLIC,
            getRuntime(UnmarshallingEventHandler.class),
            "createUnmarshaller");
        
        JVar $context = method.param( getRuntime(UnmarshallingContext.class), "context" );
        method.body()._return(
            JExpr._new(unmarshaller).arg($context));
        
//        // generate the getPrimaryInterfaceClass method.  PRIMARY_INTERFACE_CLASS
//        // is a private static method generated into the impl class by the
//        // SkeletonGenerator.  Every impl class has a reference to the class
//        // object of the primary interface it implements.
//        // [RESULT]
//        // Class getPrimaryInterfaceClass() {
//        //     return PRIMARY_INTERFACE_CLASS;
//        // }
//        JMethod m2 = impl.method( JMod.PUBLIC,
//                                  codeModel.ref( Class.class ),
//                                  "getPrimaryInterfaceClass" );
//        m2.body()._return( JExpr.invoke( "PRIMARY_INTERFACE_CLASS" ) );

        // builds the transition table.
        transitionTable = new TransitionTable( automaton );
    }
    
    protected void generate() {
        /*
        [RESULT]
        public <constructor>( UnmarshallingContext context ) {
            super(context,<text type string>);
            
            // if the trace mode is on,
            this.tracer = context.getTracer();
        }
        */
        {
            JMethod con = unmarshaller.constructor(JMod.PUBLIC);
            JVar $context = con.param(getRuntime(UnmarshallingContext.class),"context");
            con.body().invoke("super")
                .arg($context)
                .arg(JExpr.lit(generateEncodedTextType()));
        
            if(parent.trace) {
                $tracer = unmarshaller.field(JMod.PRIVATE,Tracer.class,"tracer");
                con.body().assign( $tracer, $context.invoke("getTracer") );
            }
        }
        
        /*
        [RESULT]
        protected <constructor>( UnmarshallingContext context, int startState ) {
            this(context);
            this.state = startState;
        } 
        */
        {
            JMethod con = unmarshaller.constructor(JMod.PROTECTED);
            JVar $context = con.param(getRuntime(UnmarshallingContext.class),"context");
            JVar $init    = con.param(codeModel.INT,"startState");
            con.body().invoke("this").arg($context);
            con.body().assign($state,$init);
        }
        
        new EnterElementMethodGenerator(this).generate();
        new LeaveElementMethodGenerator(this).generate();
        new EnterAttributeMethodGenerator(this).generate();
        new EnterLeaveMethodGenerator(this,"leaveAttribute",Alphabet.LeaveAttribute.class).generate();
        new TextMethodGenerator(this).generate();
        
        generateLeaveChild();
    }
    
    
    

    
    
    // short cut
    private JClass getRuntime(Class clazz) {
        return parent.context.getRuntime(clazz);
    }
    
    /**
     * Gets or creates an implementation of
     * {@link InterleaveDispatcher} for the specified alphabet.
     */
    public JClass getInterleaveDispatcher( Alphabet.Interleave a ) {
        JClass cls = (JClass)interleaveDispatcherImpls.get(a);
        if(cls!=null)   return cls;
        
        // create one
        JDefinedClass impl = null;
        try {
            impl = unmarshaller._class(JMod.PRIVATE, "Interleave" + createId());
        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
            _assert(false);
        }
        impl._extends(getRuntime(InterleaveDispatcher.class));
        
        // build a constructor
        // [RESULT]
        // InterleaveXXX() {
        //     super( context );
        //     init( new UnmarshallerEventHandler[] {
        //          new Unmarshaller(sites[0],s0),
        //          new Unmarshaller(sites[1],s1),
        //          ... } );
        // }
        {
            JMethod cstr = impl.constructor(JMod.PRIVATE);
            
            JInvocation arrayInit = JExpr._new( getRuntime(UnmarshallingEventHandler.class).array() ); 
            for( int i=0; i<a.branches.length; i++ )
                arrayInit.arg(JExpr._new(unmarshaller)
                    .arg(JExpr._super().ref("sites").component(JExpr.lit(i)))
                    .arg(getStateNumber(a.branches[i].initialState)));
            
            cstr.body().invoke("super").arg($context).arg(JExpr.lit(a.branches.length));
            cstr.body().invoke("init").arg(arrayInit);
        }
        
        // override abstract methods
        generateGetBranchForXXX( impl, a, "Element", 0 );
        generateGetBranchForXXX( impl, a, "Attribute", 1 );
        
        // [RESULT]
        // protected int getBranchForText() {
        //     return <idx>;
        // }
        {
            JMethod m = impl.method(JMod.PROTECTED, codeModel.INT, "getBranchForText");
            m.body()._return(JExpr.lit(a.getTextBranchIndex()));
        }

        interleaveDispatcherImpls.put( a, impl );
        return impl;
    }
    
    private void generateGetBranchForXXX( JDefinedClass clazz, Alphabet.Interleave a, String methodSuffix, int nameIdx ) {
        // [RESULT]
        // public int getBranchForXXX( String uri, String local ) {
        //     #FOREACH branch i
        //       if( < name matches branch i > )
        //         return i;
        //     #NEXT
        //     return -1;
        // }
        JMethod method = clazz.method(JMod.PROTECTED,codeModel.INT,"getBranchFor"+methodSuffix);
        JVar $uri   = method.param(codeModel.ref(String.class),"uri");
        JVar $local = method.param(codeModel.ref(String.class),"local");

        for( int i=0; i<a.branches.length; i++ ) {
            Alphabet.Interleave.Branch br = a.branches[i];
            NameClass nc = br.getName(nameIdx);
            
            if( nc.isNull() )       continue;
            
            method.body()._if(
                parent.generateNameClassTest(nc,$uri,$local)
            )._then()._return(JExpr.lit(i));
        }
        
        method.body()._return(JExpr.lit(-1));
    }

    /**
     * Generates statement(s) that performs a transition
     * to the 'target' state.
     * 
     * Depending on the amount of work that involves in
     * performing a state transition, we will either write
     * code inline or generate a separate method for that.
     */
    protected final void generateGoto( JBlock $body, State target ) {
        generateGoto( $body, getStateNumber(target) );
    }

    private JExpression getStateNumber(State state) {
        return JExpr.lit(automaton.getStateNumber(state));
    }
    private void generateGoto( JBlock $body, JExpression nextState ) {
        
        if(parent.trace)
            // [RESULT] tracer.nextState(<num>);
            $body.invoke( $tracer, "nextState" ).arg(nextState);
        
        // [RESULT] state=<target>;
        $body.assign($state,nextState);
    }
    
    
    /**
     * Generates the "leaveChild" event handler.
     */
    protected void generateLeaveChild() {
        
        if(!parent.trace)
            return; // the default implementation would be suffice.
            
        // [RESULT]
        // public void leaveChild( int nextState ) throws SAXException {
        //     tracer.nextState(nextState);
        //     super.leaveChild(nextState);
        // }
        JMethod method = unmarshaller.method(
            JMod.PUBLIC, codeModel.VOID, "leaveChild");
        method._throws( SAXException.class );
            
        JVar $nextState = method.param(codeModel.INT,"nextState");
        
        method.body().invoke( $tracer, "nextState" ).arg($nextState);
        method.body().invoke( JExpr._super(), "leaveChild" ).arg($nextState); 
    }
    
    /**
     * Generates a string that is an encoded form of
     * the text type of states.
     * 
     * 'L' indicates a list state, 'E' indicates a state with
     * a leave element event.
     */
    private String generateEncodedTextType() {
        StringBuffer buf = new StringBuffer(automaton.getStateSize());
        
        for( int i=automaton.getStateSize()-1; i>=0; i-- )
            buf.append('-');
        
        Iterator itr = automaton.states();
        while(itr.hasNext()) {
            State s = (State)itr.next();
            // note that if a state is a list state,
            // we don't care whether it's a leave element state or not.
            buf.setCharAt( automaton.getStateNumber(s),
                s.isListState?'L':'-' );
        }
        
        return buf.toString();
    }
    
    /**
     * Map from {@link Alphabet.BoundText} to {@link JMethod}.
     * A map from bound text token to its unmarshal method.
     */
    private final Map eatTextFunctions = new HashMap();
    
    /**
     * Consumes the text stored in the $attValue expression and unmarshal it
     * according to the specified BoundText alphabet.
     * 
     * @param block
     *      code will be generated into this block.
     * @param $attValue
     *      an expression that evaluates to the value to be unmarshalled.
     */
    protected final void eatText( JBlock block, Alphabet.BoundText ta, JExpression $attValue ) {
        JMethod method = (JMethod)eatTextFunctions.get(ta);
        if( method==null ) {
            // generate the method body
            method = generateEatTextFunction(ta);
            eatTextFunctions.put(ta,method);
        }
        
        block.invoke(method).arg($attValue);
    }
    
    private JMethod generateEatTextFunction( Alphabet.BoundText ta ) {
        JMethod method = unmarshaller.method(JMod.PRIVATE,codeModel.VOID,"eatText"+createId());
        method._throws(SAXException.class);
        JVar $value = method.param(JMod.FINAL,String.class,"value");
        
        // wrap the entire block into a try-catch block
        // so that exceptions thrown by the conversion method
        // will be catched and recovered
        // [RESULT]
        // try {
        //    ... conversion ...
        // } catch( Exception e ) {
        //     handleParseConversionException(e);
        // }
           
        JTryBlock $try = method.body()._try();
        JCatchBlock $catch = $try._catch(codeModel.ref(Exception.class));
        $catch.body()
            .invoke("handleParseConversionException").arg($catch.param("e"));

        if(parent.trace) {
            $try.body().invoke($tracer,"onConvertValue")
                .arg($value)
                .arg(JExpr.lit(ta.field.getFieldUse().name));
        }
        
        if(!ta.item.xducer.needsDelayedDeserialization()) {
            // [RESULT]
            // case #:
            //     <setter>(<conversion>(value));
                    
            ta.field.setter(
                $try.body(),
                TypeAdaptedTransducer.adapt(ta.item.xducer, ta.field)
                    .generateDeserializer($value,dc));
        } else {
            // this transducer needs delayed de-serialization.
            // typically, this happens for IDREFs.
            //
            // [RESULT]
            // context.addPatcher(new Runnable() {
            //     void run() {
            //         <setter>(<conversion>(value));
            //     }
            // });
                
            // TODO: in case of IDREF, sometimes a value could be invalid.
            // in that case, we'd like to ignore it, as if we haven't
            // seen it (to avoid corrupting the content tree objects.)
            //
            // can we nicely do that?   - kk
                
                
            JDefinedClass patcher = codeModel.newAnonymousClass(
                codeModel.ref(Runnable.class));
                
            {
                JMethod run = patcher.method(JMod.PUBLIC,codeModel.VOID,"run");
                
                ta.field.setter(
                    run.body(),
                    ta.item.xducer.generateDeserializer($value,dc));
            }                        
                
            $try.body().invoke($context,"addPatcher").arg(
                JExpr._new(patcher));
        }
        
        return method;
    }

    /**
     * Map from {@link Alphabet.Dispatch} to {@link JMethod}.
     * A map from a dispatch alphabet to its look up function
     */
    private final Map dispatchLookupFunctions = new HashMap();
     
    /**
     * Generates code fragment that checks if we can take
     * the transition by dispatching.
     */
    protected final JExpression invokeLookup(Alphabet.Dispatch da, TransitionTable.Entry tte ) {
        
        JMethod lookup = (JMethod)dispatchLookupFunctions.get(da);
        if(lookup==null)
            dispatchLookupFunctions.put(da,lookup=generateDispatchFunction(da,tte));
        
        return JExpr.invoke(lookup);
    }
            
    protected final JMethod generateDispatchFunction( Alphabet.Dispatch da, TransitionTable.Entry tte ) {
        // [RESULT]
        // private Class lookup<id>() {
        //     int attIndex = context.getAttribute(...);
        //     if(attIndex!=-1)
        //         return <lookupTable>(attIndex);
        //     else
        //         return null;
        // }
        
        JMethod lookup = unmarshaller.method(JMod.PRIVATE,Class.class,"lookup"+createId()); 
        lookup._throws(SAXException.class);
        JBlock body = lookup.body();
        JExpression $context = JExpr.ref("context");
        
        JVar $idx = body.decl( codeModel.INT, "idx",
            $context.invoke("getAttribute")
                .arg(JExpr.lit(da.attName.namespaceURI))
                .arg(JExpr.lit(da.attName.localName)));
        
        JConditional cond = body._if( $idx.gte(JExpr.lit(0)) );
        cond._then()
            ._return(da.table.lookup(
                parent.context,
                $context.invoke("eatAttribute").arg($idx),
                $context) );
        
        cond._else()._return(JExpr._null());
        
        return lookup;
    }


    
    private static final void _assert( boolean b ) {
        if(!b) 
            throw new JAXBAssertionError();
    }
}

            

