/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.generator.field.IsSetFieldRenderer;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.xmlschema.AbstractXSFunctionImpl;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.util.XSFinder;
import com.sun.xml.xsom.visitor.XSFunction;

/**
 * Property customization.
 * 
 * This customization turns an arbitrary schema component
 * into a Java property (some restrictions apply.)
 * 
 * <p>
 * All the getter methods (such as <code>getBaseType</code> or
 * <code>getBindStyle</code>) honors the delegation chain of
 * property customization specified in the spec. Namely,
 * if two property customizations are attached to an attribute
 * use and an attribute decl, then anything unspecified in the
 * attribute use defaults to attribute decl.
 * 
 * <p>
 * Property customizations are acknowledged
 * (1) when they are actually used, and
 * (2) when they are given at the component, which is mapped to a class.
 *     (so-called "point of declaration" customization)
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class BIProperty extends AbstractDeclarationImpl {
    
    // can be null
    private final String propName;
    
    // can be null
    private final String javadoc;
    
    // can be null
    private final JType baseType;
    
    /**
     * If there's a nested javaType customization, this field
     * will keep that customization. Otherwise null.
     * 
     * This customization, if present, is used to customize
     * the simple type mapping at the point of reference.
     */
    public final BIConversion conv;


    public BIProperty( Locator loc, String _propName, String _javadoc,
        JType _baseType, BIConversion _conv,
        FieldRendererFactory real, Boolean isConst, Boolean isSet ) {
        super(loc);
        
        this.propName = _propName;
        this.javadoc = _javadoc;
        this.baseType = _baseType;
        this.realization = real;
        this.isConstantProperty = isConst;
        this.needIsSetMethod = isSet;
        this.conv = _conv;
    }

    
    public void setParent( BindInfo parent ) {
        super.setParent(parent);
        if(conv!=null)
            conv.setParent(parent);
    }

    
    
    /**
     * Returns the customized property name.
     * 
     * This method honors the "enableJavaNamingConvention" customization
     * and formats the property name accordingly if necessary.
     * 
     * Thus the caller should <em>NOT</em> apply the XML-to-Java name
     * conversion algorithm to the value returned from this method.
     * 
     * @param forConstant
     *      If the property name is intended for a constant property name,
     *      set to true. This will change the result
     * 
     * @return
     *      This method can return null if the customization doesn't
     *      specify the name.
     */
    public String getPropertyName( boolean forConstant ) {
        if(propName!=null) {
            BIGlobalBinding gb = getBuilder().getGlobalBinding();
            
            if( gb.isJavaNamingConventionEnabled() && !forConstant )
                // apply XML->Java conversion
                return gb.getNameConverter().toPropertyName(propName);
            else
                return propName;    // ... or don't change the value
        }
        BIProperty next = getDefault();
        if(next!=null)  return next.getPropertyName(forConstant);
        else            return null;
    }
    
    /**
     * Gets the associated javadoc.
     * 
     * @return
     *      null if none is specfieid.
     */
    public String getJavadoc() {
        return javadoc;
    }
    
    // can be null
    public JType getBaseType() {
        if(baseType!=null)  return baseType;
        BIProperty next = getDefault();
        if(next!=null)  return next.getBaseType();
        else            return null;
    }
    
    
    // can be null
    private final FieldRendererFactory realization;
    /**
     * Gets the realization of this field.
     * @return Always return non-null.
     */
    public FieldRendererFactory getRealization() {
        if(realization!=null)   return realization;
        BIProperty next = getDefault();
        if(next!=null)  return next.getRealization();
        
        // globalBinding always has a value in this property,
        // so this can't happen
        throw new JAXBAssertionError();
    }
    
    
    // true, false, or null.
    private final Boolean needIsSetMethod;
    public boolean needIsSetMethod() {
        if(needIsSetMethod!=null)   return needIsSetMethod.booleanValue();
        BIProperty next = getDefault();
        if(next!=null)      return next.needIsSetMethod();
        
        // globalBinding always has true or false in this property,
        // so this can't happen
        throw new JAXBAssertionError();
    }
    
    
    // true, false, or null (which means the value should be inherited.)
    private Boolean isConstantProperty;
    /**
     * Gets the inherited value of the "fixedAttrToConstantProperty" customization.
     * 
     * <p>
     * Note that returning true from this method doesn't necessarily mean
     * that a property needs to be mapped to a constant property.
     * It just means that it's mapped to a constant property
     * <b>if an attribute use carries a fixed value.</b>
     * 
     * <p>
     * I don't like this semantics but that's what the spec implies.
     */
    public boolean isConstantProperty() {
        if(isConstantProperty!=null)    return isConstantProperty.booleanValue();
        
        BIProperty next = getDefault();
        if(next!=null)      return next.isConstantProperty();
        
        // globalBinding always has true or false in this property,
        // so this can't happen
        throw new JAXBAssertionError();
    }
    
    
    
    /**
     * Creates a FieldItem by following this customization.
     * 
     * @param defaultName
     *      If the name is not customized, this name will be used
     *      as the default. Note that the name conversion <b>MUST</b>
     *      be applied before this method is called if necessary.
     * @param body
     *      The body expression of the newly created FieldItem.
     * @param source
     *      Source schema component from which a field is built.
     *      This could be different from the owner of this customization.
     */
    public FieldItem createFieldItem( String defaultName, boolean forConstant, Expression body, XSComponent source ) {
        markAsAcknowledged();
        constantPropertyErrorCheck();
        
        String name = getPropertyName(forConstant);
        if(name==null)
            name = defaultName;

        FieldItem fi = new FieldItem(name, body, getBaseType(), source.getLocator() );
        fi.javadoc = concat(javadoc,
            getBuilder().getBindInfo(source).getDocumentation());
            
        fi.realization = getRealization();
        
        _assert( fi.realization!=null );    // we can't allow null because sometimes
                                            // we need to wrap it by a IsSetFieldRenderer
        if( needIsSetMethod() )
            fi.realization = IsSetFieldRenderer.createFactory( fi.realization );


        addDeclarations(fi, source.getAnnotation());
        
        if (source instanceof XSAttributeUse)
        {
          final XSAttributeDecl attributeDecl = ((XSAttributeUse) source).getDecl();
          addDeclarations(fi, attributeDecl.getAnnotation());
        }
        
        return fi;
    }


    private void addDeclarations(FieldItem fi, final XSAnnotation annon) {
      if(annon!=null) {
          final BindInfo bi = (BindInfo)annon.getAnnotation();
          if(bi!=null) {
              final BIDeclaration[] decls = bi.getDecls();
              for (int index = 0; index < decls.length; index++) {
                  fi.declarations.add(decls[index]);
              }
          }
      }
    }

    public void markAsAcknowledged() {
        if( isAcknowledged() )  return;
        
        // mark the parent as well.
        super.markAsAcknowledged();
        
        BIProperty def = getDefault();
        if(def!=null)   def.markAsAcknowledged();
    }
    
    private void constantPropertyErrorCheck() {
        if( isConstantProperty!=null && getOwner()!=null ) {
            // run additional check on the isCOnstantProperty value.
            // this value is not allowed if the schema component doesn't have
            // a fixed value constraint.
            //
            // the setParent method associates a customization with the rest of
            // XSOM object graph, so this is the earliest possible moment where
            // we can test this.
            
            if( !hasFixedValue.find(getOwner()) ) {
                getBuilder().errorReceiver.error(
                    getLocation(),
                    Messages.format(ERR_ILLEGAL_FIXEDATTR)
                );
                // set this value to null to avoid the same error to be reported more than once.
                isConstantProperty = null;
            }
        }
    }

    /**
     * Function object that returns true if a component has
     * a fixed value constraint.
     */
    private final XSFinder hasFixedValue = new XSFinder() {
        private Boolean toBoolean( boolean b ) {
            return b?Boolean.TRUE:Boolean.FALSE;
        }
        
        public Object attributeDecl(XSAttributeDecl decl) {
            return toBoolean(decl.getFixedValue()!=null);
        }

        public Object attributeUse(XSAttributeUse use) {
            return toBoolean(use.getFixedValue()!=null);
        }
        
        public Object schema(XSSchema s) {
            // we allow globalBindings to have isConstantProperty==true,
            // so this method returns true to allow this.
            return Boolean.TRUE;
        }
    };
    
    /**
     * Finds a BIProperty which this object should delegate to.
     * 
     * @return
     *      always return non-null for normal BIProperties.
     *      If this object is contained in the BIGlobalBinding, then
     *      this method returns null to indicate that there's no more default.
     */
    protected BIProperty getDefault() {
        if(getOwner()==null)    return null;
        BIProperty next = getDefault(getBuilder(),getOwner());
        if(next==this)  return null;    // global.
        else            return next;
    }
    
    private static BIProperty getDefault( BGMBuilder builder, XSComponent c ) {
        while(c!=null) {
            c = (XSComponent)c.apply(defaultCustomizationFinder);
            if(c!=null) {
                BIProperty prop = (BIProperty)builder.getBindInfo(c).get(NAME);
                if(prop!=null)  return prop;
            }
        }
        
        // default to the global one
        return builder.getGlobalBinding().getDefaultProperty();
    }
    
    
    /**
     * Finds a property customization that describes how the given
     * component should be mapped to a property (if it's mapped to
     * a property at all.)
     * 
     * <p>
     * Consider an attribute use that does NOT carry a property
     * customization. This schema component is nonetheless considered
     * to carry a (sort of) implicit property customization, whose values
     * are defaulted.
     * 
     * <p>
     * This method can be think of the method that returns this implied
     * property customization.
     * 
     * <p>
     * Note that this doesn't mean the given component needs to be
     * mapped to a property. But if it does map to a property, it needs
     * to follow this customization.
     * 
     * I think this semantics is next to non-sense but I couldn't think
     * of any other way to follow the spec.
     * 
     * @param c
     *      A customization effective on this component will be returned.
     *      Can be null just to get the global customization.
     * @return
     *      Always return non-null valid object.
     */
    public static BIProperty getCustomization( BGMBuilder builder, XSComponent c ) {
        
        // look for a customization on this component
        if( c!=null ) {
            BIProperty prop = (BIProperty)builder.getBindInfo(c).get(NAME);
            if(prop!=null)  return prop;
        }
        
        // if no such thing exists, defeault.
        return getDefault(builder,c);
    }
    
    private final static XSFunction defaultCustomizationFinder = new AbstractXSFunctionImpl() {

        public Object attributeUse(XSAttributeUse use) {
            return use.getDecl();   // inherit from the declaration
        }

        public Object particle(XSParticle particle) {
            return particle.getTerm(); // inherit from the term
        }

        public Object schema(XSSchema schema) {
            // no more delegation
            return null;
        }

        // delegates to the context schema object
        public Object attributeDecl(XSAttributeDecl decl) { return decl.getOwnerSchema(); }
        public Object wildcard(XSWildcard wc) { return wc.getOwnerSchema(); }
        public Object modelGroupDecl(XSModelGroupDecl decl) { return decl.getOwnerSchema(); }
        public Object modelGroup(XSModelGroup group) { return group.getOwnerSchema(); }
        public Object elementDecl(XSElementDecl decl) { return decl.getOwnerSchema(); }
        public Object complexType(XSComplexType type) { return type.getOwnerSchema(); }
        
        // property customizations are not allowed on these components.
        public Object attGroupDecl(XSAttGroupDecl decl) { throw new JAXBAssertionError(); }
        public Object simpleType(XSSimpleType simpleType) { throw new JAXBAssertionError(); }
        public Object empty(XSContentType empty) { throw new JAXBAssertionError(); }
    };
    
    
    private static String concat( String s1, String s2 ) {
        if(s1==null)    return s2;
        if(s2==null)    return s1;
        return s1+"\n\n"+s2;
    }
    
    public QName getName() { return NAME; }
    
    /** Name of this declaration. */
    public static final QName NAME = new QName(
        Const.JAXB_NSURI, "property" );

    private static final String ERR_ILLEGAL_FIXEDATTR =
        "BIProperty.IllegalFixedAttributeAsConstantProperty";
}

