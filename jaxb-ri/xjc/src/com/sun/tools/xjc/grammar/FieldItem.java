/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Locator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.grammar.util.Multiplicity;

/**
 * Represents a field relationship between two objects.
 * 
 * <p>
 * Also keeps the information about how accessor should be generated.
 * 
 * <p>
 * The name property keeps the name of the field.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class FieldItem extends JavaItem {
    public FieldItem(String name, Locator loc) {
        this(name, null, loc);
    }
    public FieldItem(String name, Expression exp, Locator loc) {
        this(name, exp, null, loc);
    }

    public FieldItem(String name, Expression _exp, JType _userDefinedType, Locator loc) {
        super(name, loc);
        this.exp = _exp;
        this.userSpecifiedType = _userDefinedType;
    }
    
    /**
     * If this FieldItem needs to be realized as a Java property in
     * a special way, this field is set to the factory that can
     * create the renderer.
     * <p>
     * This field can be null, and in that case the back-end will
     * figure out the default one.
     */
    public FieldRendererFactory realization;
    
    /**
     * Default values of this field, if any.
     * This field will be a set of PrimitiveItems whose body is
     * a ValueExp.
     * <p>
     * If two FieldItems of the same FieldUse have default values,
     * it would be an error. But a default value can be specified
     * for an attribute whose type is a list, so it can correspond
     * to multiple Java objects.
     * <p>
     * This field will be null when there is no default value.
     * Note that the empty array means there was a default value.
     * For example,
     * <pre><xmp>
     * <attribute name="foo" default="">
     *   <simpleType>
     *     <list itemType="token"/>
     *   </simpleType>
     * </attribute>
     * </xmp></pre>
     */
    public DefaultValue[] defaultValues = null;
    
    /**
     * multiplicity of this field to its children (field-class/interface).
     * Note that this multiplicity and class-field multiplicity is completely
     * a different thing.
     * 
     * <p>
     * A schema reader can explicitly set a multiplicity, as long as it
     * is "larger" than the actual multiplicity. This mechanism can be
     * used for example to force generation of a list property.
     * 
     * <p>
     * If left null by a schema reader,
     * this field is computed during the first pass of the normalization.
     */
    public Multiplicity multiplicity;
    
    /**
     * If two {@link FieldItem}s have the same name and this property is
     * true on both objects, it doesn't constitute the name collision error. 
     */
    public boolean collisionExpected = false;
    
    /**
     * If set, this string will be used by the backend
     * to generate the javadoc comment for this field.
     */
    public String javadoc = null;
    
    /**
     * If this flag is set to true, the backend will generate code so that
     * the parent class will delegate method calls to this field.
     * <p>
     * (That is, if a field is of type X, the class implements X and delegate
     * all the method calls to this field.)
     * <p>
     * Delegation is technically a property of a {@link FieldUse},
     * not of a {@link FieldItem}. If one of the {@link FieldItem}s have
     * this flag true, then the associated {@link FieldUse} will be considered
     * to have the delegation. 
     * 
     * @see FieldUse#isDelegated()
     */
    private boolean delegation = false;
    
    public void setDelegation( boolean f ) {
        delegation = f;
    }
    
    protected boolean isDelegated() {
        return delegation;
    }
    
    
    /**
     * All {@link TypeItem}s that appear as the children of this type.
     * This field is computed during the first pass of the normalization.
     * 
     * <p>
     * When the type is specified by the user, these values are used
     * to make sure that the specified type is in fact OK.
     * 
     * <p>
     * Otherwise, the type of this field is computed by using these types.
     */
    private final Set types = new HashSet();
    
    /**
     * Adds a child {@link TypeItem}. This method may be called
     * from the normalizer only.
     * 
     * @exception BadTypeException
     *      if the specified type is inconsistent with
     *      the user-specified type.
     */
    public final void addType( TypeItem ti ) throws BadTypeException {
        // if there is an user-specified type, make sure that
        // this type is a proper subtype.
        if( userSpecifiedType!=null ) {
            
            throw new BadTypeException(userSpecifiedType);
        }
                
        types.add(ti);
    }
    
    /**
     * Gets all the TypeItems that can be assigned to this field.
     */
    public final TypeItem[] listTypes() {
        return (TypeItem[]) types.toArray(new TypeItem[types.size()]);
    }
    
    /**
     * Returns true if there is at least one type.
     */
    public final boolean hasTypes() {
        return !types.isEmpty();
    }
    
    public static class BadTypeException extends Exception {
        private BadTypeException( JType _type ) {
            this.type = _type;
        }
        private final JType type;
        public JType getUserSpecifiedType() { return type; }
    }

    /**
     * The type specified by the user as the type of this field.
     * 
     * <p>
     * This field is set to null when no type is specified.
     * 
     * <p>
     * To obtain the actual type of this field, use
     * the {@link #getType(JCodeModel)} method.
     * This field is made private so that everyone will use the
     * getType method.
     */
    public final JType userSpecifiedType;
    
    /**
     * Gets the type of this field.
     * 
     * note that there may be other FieldItems whose names are the same.
     * TODO: Maybe we should reject those cases?
     */
    public JType getType( JCodeModel codeModel ) {
        // if there is an user-specified type, use it.
        if( userSpecifiedType!=null )   return userSpecifiedType;
        
        // otherwise compute the base class
        JType[] classes = new JType[types.size()];
        TypeItem[] types = listTypes();
        
        for( int i=0; i<types.length; i++ )
            classes[i] = types[i].getType();
        
        return com.sun.tools.xjc.reader.TypeUtil.getCommonBaseType(codeModel,classes);
    }
    
    /**
     * Returns true if this field is unboxable.
     * 
     * This function needs to return false if one
     * of those TypeItems has the special JNullType.
     * That's why we can't rely on the getType method.
     */
    public boolean isUnboxable( JCodeModel codeModel ) {
        TypeItem[] types = listTypes();
        
        if( !getType(codeModel).isPrimitive() )
            return false;   // can't be unboxed from the first place
        
        // make sure that it doesn't include any JClass (in particular nullType)
        for( int i=0; i<types.length; i++ ) {
            JType t = types[i].getType();
            if( t instanceof JPrimitiveType )
                continue;
                
            if( ((JClass)t).getPrimitiveType()==null )
                return false;
        }
        
        return true;
    }
    
    public Object visitJI( JavaItemVisitor visitor ) {
        return visitor.onField(this);
    }
    
    public String toString() {
        return super.toString()+'['+name+']';
    }
}
