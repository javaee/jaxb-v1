/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.marshaller.FieldMarshallerGenerator;

/**
 * Represents a Java object which is marshalled/unmarshalled
 * externally.
 * 
 * An external item is required to map to a single element and
 * all its descendants.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ExternalItem extends TypeItem {

    /**
     * Name of the element that this external item can handle.
     */
    public final NameClass elementName;

    public ExternalItem(String displayName, NameClass _elementName, Locator loc) {
        super(displayName,loc);
        this.elementName = _elementName;
        
        // dummy stub. We just need something non-nullable
        this.exp = new ElementPattern(NameClass.ALL,Expression.epsilon);
    }
    
    /**
     * Creates AGM fragment that will be used for the unmarshalling validation.
     * 
     * @param pool
     *      expression objects shall be created from this pool.
     * 
     * @return
     *      if the method returns a {@link com.sun.xml.bind.GrammarImpl.Plug},
     *      it will be registered as such.
     */
    public abstract Expression createAGM( ExpressionPool pool );
    
    // TODO: think about this more.
    /**
     * CreatesAGM fragment that will be used for the runtime validation.
     */
    public abstract Expression createValidationFragment();
    
    /**
     * Generates a marshaller into the specified block.
     * 
     * @param $context
     *      A variable that evaluates to the
     *      {@link com.sun.xml.bind.serializer.XMLSerializer} object.
     */
    public abstract void generateMarshaller(
        GeneratorContext context, 
        JBlock block,
        FieldMarshallerGenerator field,
        JExpression $context );
    
    /**
     * Generates an unmarshaller into the given block.
     * The generated code should create a new child object, set up
     * the runtime appropriately so that the child object will take over
     * unmarshalling of the current element.
     * 
     * @param block
     *      Unmarshaller shall be appended into this block
     * @param memento
     *      An expression that evaluates to the current unmarshalling
     *      state of the parent.
     * @param $uri,$local,$qname,$atts
     *      Variables that evaluate to the namespace URI, the local name,
     *      and the current attributes of the current element.
     * 
     * @return
     *      The newly created child object. This object will be set to
     *      the appropriate field of the parent object by the caller.
     * 
     *      The returned variable can evaluate to null when it fails to
     *      unmarshal the current element for some reason.
     * 
     *      Note that this returned expression will be evaluated more than
     *      once.
     */
    public abstract JExpression generateUnmarshaller(
        GeneratorContext context, JExpression $unmarshallingContext,
        JBlock block, JExpression memento, JVar $uri, JVar $local, JVar $qname, JVar $atts );

// TODO: making external item available as a root is a big deal    
//    /**
//     * Generates a code fragment that creates a new
//     * {@link com.sun.tools.xjc.runtime.UnmarshallingEventHandler}
//     * that processes the root element.
//     * 
//     * If the implementation cannot start the unmarshalling, return null.
//     */
//    public abstract JExpression createRootUnmarshaller(
//        GeneratorContext context, JVar $unmarshallingContext );
    
    // this method is final. No sub-class should allow the
    // rest of the XJC to downcast ExternalItem to one of its sub-class.
    public final Object visitJI(JavaItemVisitor visitor) {
        return visitor.onExternal(this);
    }
}
