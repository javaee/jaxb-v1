/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.ArrayList;

import org.dom4j.Element;

import com.sun.codemodel.JClass;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.tools.xjc.generator.field.ArrayFieldRenderer;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.field.UntypedListFieldRenderer;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * Particles in the &lt;content> declaration in the binding file.
 * 
 */
public abstract class BIContent
{
    /**
     * Wraps a given particle.
     * 
     * <p>
     * This object should be created through
     * the {@link #create()} method.
     */
    private BIContent( Element e, BIElement _parent ) {
        this.element = e;
        this.parent = _parent;
    }
    
    /** The particle element which this object is wrapping. */
    protected final Element element;
    
    /** The parent object.*/
    protected final BIElement parent;
    
    /**
     * Gets the realization of this particle, if any.
     * 
     * @return
     *      null if the "collection" attribute was not specified.
     */
    public final FieldRendererFactory getRealization() {
        String v = element.attributeValue("collection");
        if(v==null)     return null;
        
        v = v.trim();
        if(v.equals("array"))   return ArrayFieldRenderer.theFactory;
        if(v.equals("list"))
            return new UntypedListFieldRenderer.Factory(
                parent.parent.codeModel.ref(ArrayList.class));
        
        // the correctness of the attribute value must be 
        // checked by the validator.
        throw new InternalError("unexpected collection value: "+v);
    }
    
    /**
     * Gets the property name of this particle.
     * 
     * @return
     *      always a non-null, valid string.
     */
    public final String getPropertyName() {
        String r = element.attributeValue("property");
        
        // in case of <element-ref>, @property is optional and
        // defaults to @name.
        // in all other cases, @property is mandatory.
        if(r!=null)     return r;
        return element.attributeValue("name");
    }
    
    /**
     * Gets the type of this property, if any.
     * <p>
     * &lt;element-ref> particle doesn't have the type.
     * 
     * @return
     *      null if none is specified.
     */
    public final JClass getType() {
        try {
            String type = element.attributeValue("supertype");
            if(type==null)     return null;
            
            // TODO: does this attribute defaults to the current package?
            int idx = type.lastIndexOf('.');
            if(idx<0)   return parent.parent.codeModel.ref(type);
            else        return parent.parent.getTargetPackage().ref(type);
        } catch( ClassNotFoundException e ) {
            // TODO: better error handling
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
    
    /**
     * Wraps an expression by the specified parameters.
     * <p>
     * This method is also responsible for detecting mismatch
     * between the user instruction and the actual schema.
     * (e.g., &lt;element-ref> is specified, but the corresponding
     * part of the schema is ChoiceExp.)
     */
    public final Expression wrap( Expression exp ) throws MismatchException {
        // make sure that the declaration is consistent with the schema
        if(!checkMatch(exp.peelOccurence()))
            throw new MismatchException();
        
        // wrap it by a FieldItem
        FieldItem fi = new FieldItem(
            getPropertyName(), exp, getType(), null ); // TODO: location support
        fi.realization = getRealization();
        
        return fi;
    }
    
    /** Checks if the "core" matches the expected primitive. */
    protected abstract boolean checkMatch( Expression core );
    
    public static final class MismatchException extends Exception {}
    
    
    
    
    
    /**
     * Creates an appropriate subclass of BIContent
     * by sniffing the tag name.
     * <p>
     * This method should be only called by the BIElement class.
     */
    static BIContent create( Element e, BIElement _parent ) {
        // TODO: is this the correct way to get local name?
        String tagName = e.getName();

        if( tagName.equals("element-ref") )
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    // note that every element declaration is wrapped by
                    // a ReferenceExp.
                    return exp instanceof ReferenceExp;
                }
            };
        
        if( tagName.equals("choice") )
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    return exp instanceof ChoiceExp;
                }
            };
        
        if( tagName.equals("sequence") )
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    return exp instanceof SequenceExp;
                }
            };
        
        if( tagName.equals("rest")
        ||  tagName.equals("content") )
            // "content" will be treated as "rest",
            // so that we can treat the general content-property declaration
            // as a short-hand of model-based content-property declaration.
            return new BIContent(e,_parent){
                protected boolean checkMatch( Expression exp ) {
                    // the "wrap" method of the "rest" declaration
                    // shouldn't be called.
                    // they have to be wrapped in a different way.
                    throw new JAXBAssertionError();
                }
            };
        
        // illegal tag names should be rejected by the validator
        // before we read it.
        throw new JAXBAssertionError();
    }
    
    
}
