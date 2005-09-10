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
package com.sun.tools.xjc.generator.marshaller;

import java.util.Map;
import java.util.Stack;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.ExistingBlockReference;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.xducer.SerializerContext;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * The center of gravity.
 * 
 * This object provides access to various information
 * necessary to actually generate a marshaller
 * for one class.
 * 
 * <p>
 * As {@link SerializerContext}, this class generated code
 * that invokes {@link com.sun.tools.xjc.runtime.XMLSerializer}.
 */
final class Context implements SerializerContext
{
    /**
     * The side object currently in use.
     */
    Side currentSide;
    
   
    /**
     * The pass object in use. 
     */
    Pass currentPass;
    

    public final GeneratorContext genContext;
    
    /**
     * References to a variable that evaluates to
     * the marshaller context parameter
     * ({@link com.sun.xml.bind.serializer.XMLSerializer}).
     */
    public final JVar $serializer;
    
    public final JCodeModel codeModel;
    
    /**
     * Current class item object to which we are generating
     * a marshaller.
     */
    protected final ClassItem classItem;
    
    public final ExpressionPool pool;


    /** Set to true while we are processing inside &lt;oneOrMore> item. */
    boolean inOneOrMore = false;

    private final Builder builder = new Builder(this);


    /**
     * represents in-scope field
     * if we are inside, or null if outside. */
    private FieldUse fu = null;
    
    /** sequence generator. */
    private int iota = 0;
    
    
    //
    // UGLY. This whole FMG handling (including override) needs to be
    // rewritten in somewhat cleaner way.
    // But I can't think of a nice way now  -kk.
    
    /** Map from FieldUse to FieldMarshallerGenerator. */
    private final Map fieldMarshallers;
    
    
    
    
    /**
     * @param   _genContext
     *      The GeneratorContext which provides information about
     *      the generated skeleton.
     * @param   _class
     *      A ClassItem for which we generate a marshaller.
     * @param   codeBlock
     *      Body of the marshal method. Marshalling code will be
     *      added to this code block.
     * @param   _$serializer
     *      The context parameter to the marshal method.
     * @param   _fieldMarshallers
     *      A map from {@link FieldUse} to {@link FieldMarshallers},
     *      which will be used to generate a marshaller.
     */
    public Context( GeneratorContext _genContext, ExpressionPool _pool, ClassItem _class,
        JBlock codeBlock, JVar _$serializer, Map _fieldMarshallers ) {
        this.genContext = _genContext;
        this.pool = _pool;
        this.classItem = _class;
        this.$serializer = _$serializer;
        this.fieldMarshallers = _fieldMarshallers;
        this.codeModel = classItem.owner.codeModel;
        this.currentSide = outside;  // start from outside
        pushNewBlock(new ExistingBlockReference(codeBlock));
    }
    
    
    // short cut
    protected final JClass getRuntime( Class clazz ) {
        return genContext.getRuntime(clazz);
    }
    
    protected final boolean isInside() {
        return currentSide==inside;
    }
    
    /**
     * Gets the FieldMarshallerGenerator that is responsible for
     * marshalling the given FieldItem.
     * 
     * If this field item is realized in the base class, this method
     * returns null to indicate that there is no need to marshal it.
     */
    public FieldMarshallerGenerator getMarshaller(FieldItem fi) {
        return (FieldMarshallerGenerator)
            fieldMarshallers.get(classItem.getDeclaredField(fi.name));
    }
   
    private final Stack overridedFMGs = new Stack();
    /**
     * Temporarily override the existing field marshaller generator (FMG) by
     * a new one.
     */
    public void pushNewFieldMarshallerMapping(
        FieldMarshallerGenerator original, 
        FieldMarshallerGenerator _new ) {
        
        // update the map by the new FMG
        Object old = fieldMarshallers.put(original.owner().getFieldUse(),_new);
        _assert(old==original);
        
        // memorize the replaced original FMG, so that we can later restore it.
        overridedFMGs.push(original);
    }
    /**
     * Restores the last override of FMG.
     */
    public void popFieldMarshallerMapping() {
        FieldMarshallerGenerator fmg = (FieldMarshallerGenerator)overridedFMGs.pop();
        fieldMarshallers.put(fmg.owner().getFieldUse(),fmg);
    }
        
    
    
    public void pushFieldItem( FieldItem item ) {
        // switch to inside
        _assert(fu==null);
        fu = classItem.getDeclaredField(item.name);
            
        currentSide = inside; 
        _assert(fu!=null);
    }
    public void popFieldItem( FieldItem item ) {
        _assert(fu!=null && fu.name.equals(item.name));
        fu = null;
        currentSide = outside; 
    }
    
    /**
     * If we are visiting inside a {@link FieldItem}, returns the
     * {@link FieldMarshallerGenerator} for that field.
     * Otherwise, return null to indicate that we are not visiting inside
     * a FieldItem.
     */
    public FieldMarshallerGenerator getCurrentFieldMarshaller() {
        return (FieldMarshallerGenerator)fieldMarshallers.get(fu);
    }
    
    
    
    //
    //
    // CodeBlock stack management
    //
    //
    
    /** Stack of BlockProviders. */
    private final Stack blocks = new Stack();
    
    public void pushNewBlock( BlockReference newBlock ) {
        blocks.push(newBlock);
    }
    public void pushNewBlock( JBlock block ) {
        pushNewBlock(new ExistingBlockReference(block));
    }
    public void popBlock() {
        blocks.pop();
    }
    public BlockReference getCurrentBlock() {
        return (BlockReference)blocks.peek();
    }
    
    
    
    /**
     * Generates an unique identifier.
     * 
     * <p>
     * For example, this method is useful when there is a need to create
     * temporary variables.
     * 
     * <p>
     * Returned string is unique in the sense that
     * no successive call of this method will return the same identifier.
     * Note that two different Context objects can return
     * the same identifier.
     */
    public String createIdentifier() {
        return '_'+Integer.toString(iota++);
    }
    
    
    /**
     * Builds a marshaller for the given expression by using
     * the current {@link Pass} object.
     */
    public void build(Expression exp) {
        exp.visit(builder);
    }

    
    //
    // side instances
    //
    private final Inside  inside  = new Inside(this);
    private final Outside outside = new Outside(this); 
    
    
    //
    // pass instances
    //
    final Pass bodyPass = new BodyPass(this,"Body");
    final Pass attPass = new AttributePass(this);
    final Pass uriPass = new URIPass(this);
    final Pass skipPass = new SkipPass(this);
    

//
// SerializerContext implementation
//
    public JExpression getNamespaceContext() {
        return $serializer.invoke("getNamespaceContext");
    }

    public JExpression onID(JExpression object, JExpression value) {
        return $serializer.invoke("onID").arg(object).arg(value);
    }

    public JExpression onIDREF(JExpression target) {
        return $serializer.invoke("onIDREF").arg(target);
    }
    
    public void declareNamespace(JBlock block, JExpression uri, JExpression prefix, JExpression requirePrefix) {
        block.invoke(getNamespaceContext(),"declareNamespace").arg(uri).arg(prefix).arg(requirePrefix);
    }
    
    
    
    private static void _assert( boolean b ) {
        if(!b)  throw new JAXBAssertionError();
    }


}