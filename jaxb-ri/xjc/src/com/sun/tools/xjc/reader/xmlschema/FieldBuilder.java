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
package com.sun.tools.xjc.reader.xmlschema;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Locator;

import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.ValueExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.generator.field.ConstFieldRenderer;
import com.sun.tools.xjc.generator.field.XsiNilFieldRenderer;
import com.sun.tools.xjc.generator.field.XsiTypeFieldRenderer;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.DefaultValue;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.xducer.BuiltinDatatypeTransducerFactory;
import com.sun.tools.xjc.grammar.xducer.WhitespaceTransducer;
import com.sun.tools.xjc.reader.Const;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.xsom.XSAttContainer;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.ModelGroupImpl;
import com.sun.xml.xsom.impl.ParticleImpl;
import com.sun.xml.xsom.impl.Ref;
import com.sun.xml.xsom.impl.SchemaImpl;

/**
 * Builds an expression that has {@link FieldItem} as its top-level
 * binding expression.
 * 
 * <p>
 * There is at least one ugly code that you need to aware of
 * when you are modifying the code. See the documentation
 * about <a href="package.html#stref_cust">
 * "simple type customization at the point of reference."</a>
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class FieldBuilder extends AbstractXSFunctionImpl {
    
    FieldBuilder( BGMBuilder _builder ) {
        this.builder = _builder;
        this.pool = builder.pool;
    }
    
    /** Type-safe wrapper method. */
    public final Expression build( XSComponent sc ) {
        return (Expression)sc.apply(this);
    }
    
    private final BGMBuilder builder;
    private final ExpressionPool pool;


    public Object attGroupDecl(XSAttGroupDecl decl) {
        return attributeContainer(decl);
    }
    
    public Expression attributeContainer(XSAttContainer decl) {
        Expression exp = Expression.epsilon;
        
        Iterator itr = decl.iterateAttGroups();
        while(itr.hasNext())
            exp = pool.createSequence( exp, build((XSAttGroupDecl)itr.next()) );

        itr = decl.iterateDeclaredAttributeUses();
        while(itr.hasNext())
            exp = pool.createSequence( exp, build((XSAttributeUse)itr.next()) );
            
        return exp;
    }

    public Object attributeDecl(XSAttributeDecl arg0) {
        _assert(false);
        return null;
    }

    public Object attributeUse(final XSAttributeUse use) {
        final BIProperty cust = getPropCustomization(use);
        
        
        AttributeExp body = builder.typeBuilder._attributeDecl(use.getDecl());
        final Expression originalBody=body.exp;
        
        // TODO: we need to put these logic to one place
        // so that the effect of BIProperty would be consistent
        // across all the schema components.
        
        
        boolean hasFixedValue = use.getFixedValue()!=null;
        
        if( hasFixedValue ) {
            // refine the BGM to incorporate the fixed value
            String token = use.getFixedValue();;
            
            Expression contents =
                FixedExpBuilder.build( body.exp, token, builder.grammar, use.getContext() );
            if(contents==Expression.nullSet) {
                Locator loc;
                if(use.getDecl().getFixedValue()!=null)   loc = use.getDecl().getLocator();
                else                                    loc = use.getLocator();
                builder.errorReporter.error( loc, Messages.ERR_INCORRECT_FIXED_VALUE, token );
            } else
                body = new AttributeExp( body.nameClass, contents );
        } else {
            // TODO:
            // it's nice if we can warn the user if the attUse or attDecl
            // has fixedAttrToConstantProperty="true", but we have no way to
            // check this right now.
            ;
        }
        
        // map to a constant property ?
        final boolean toConstant =
            BIProperty.getCustomization(builder,use).isConstantProperty() &&
            use.getFixedValue()!=null;
        
        // compute the default name for this property
        String xmlName = use.getDecl().getName();
        String defaultName = 
            toConstant?makeJavaConstName(xmlName):makeJavaName(xmlName);
        
        final FieldItem exp = createFieldItem( defaultName, toConstant, body, use );
        
        
        if(use.getDefaultValue()!=null ) {
            // this attribute use has a default value. Reflect it to BGM
            String token = use.getDefaultValue();
            
            Expression contents =
                FixedExpBuilder.build( body.exp, token, builder.grammar, use.getContext() );
            if(contents==Expression.nullSet) {
                Locator loc;
                if(use.getDecl().getDefaultValue()!=null)   loc = use.getDecl().getLocator();
                else                                        loc = use.getLocator();
                builder.errorReporter.error( loc, Messages.ERR_INCORRECT_DEFAULT_VALUE, token );
            }
            
            final ArrayList values = new ArrayList();
            contents.visit(new BGMWalker() {
                public Object onPrimitive(PrimitiveItem item) {
                    values.add(new DefaultValue(item.xducer,(ValueExp)item.exp));
                    return null; // return value unused
                }
            });
            exp.defaultValues = (DefaultValue[]) values.toArray(new DefaultValue[values.size()]);
        }
        
        if(toConstant)
            // specify a realization of this field so that it will be
            // realized as a constant property
            exp.realization = ConstFieldRenderer.theFactory;

        if(hasFixedValue) {          
            // update the type of FieldItem or else we can't handle
            // fixed="" for list of something
            originalBody.visit(new BGMWalker() {
                public Object onPrimitive(PrimitiveItem item) {
                    try {
                        exp.addType(item);
                    } catch( FieldItem.BadTypeException e ) {
                        // this type contradicts the user's specification.
                        builder.errorReporter.error(
                            use.getLocator(),
                            Messages.ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE,
                            exp.userSpecifiedType.name(),
                            item.getType().name());
                        if(cust!=null) {
                            builder.errorReporter.error(
                                cust.getLocation(),
                                Messages.ERR_CONFLICT_BETWEEN_USERTYPE_AND_ACTUALTYPE_ATTUSE_SOURCE );
                        }
                    }
                    return null;
                }
            });
        }
        
        if(!use.isRequired())   return pool.createOptional(exp);
        return exp;
    }

    /**
     * Gets the property customization applicable to the given attribute use.
     */
    private BIProperty getPropCustomization( XSAttributeUse use ) {
        // check the property customization of this component first
        BIProperty cust = (BIProperty)builder.getBindInfo(use).get(BIProperty.NAME);
        if(cust!=null)  return cust;
        
        // if not, default to the attribute declaration
        return (BIProperty)builder.getBindInfo(use.getDecl()).get(BIProperty.NAME);
        
    }


    
    
    public Object complexType(XSComplexType type) {
        return builder.complexTypeBuilder.build(type);
    }

    public Object simpleType(XSSimpleType type) {
        // we don't allow a property customization on a simple type,
        // so the look up starts from the parent schema.
        return simpleType(type,type.getOwnerSchema());
    }
    
    /**
     * Maps a given simple type to a field, while using a property customization
     * on the "property" component if it exists.
     * 
     * @param property
     *      can be null, in that case only the global default will be considered.
     */
    public Expression simpleType( XSSimpleType type, XSComponent property ) {
        BIProperty prop = BIProperty.getCustomization(
            builder, property );
        
        return prop.createFieldItem(
            "Value", false,
            builder.simpleTypeBuilder.build(type), type );
    }

    public Object particle(XSParticle p) {
        /*
        Basically, this method just needs to call:
        
            return builder.particleBinder.build(p,???);
        
        But the second parameter depends on the caller.
        If this is used to process an explicit content of a complex type,
        then the base type needs to be provided.
        If this is used to process the body of a model group declaration,
        then the second parameter will be null.
        
        Thus, we can't do this inside this method, where no context information
        is available.
        
        Fortunately, this method will never be called from outside directly.
        (If this assertion fails, that means this assumption is wrong.
        
        Thus the processing of a particle is done in another overloaded version
        of the particle method.
        */
        _assert(false);
        return null;
    }
    
    private Expression particle( XSParticle p, ClassItem superClass ) {
        return builder.particleBinder.build(p,superClass);
    }

    public Object empty(XSContentType ct) {
        return Expression.epsilon;
    }

    /** Wraps the given term by a dummy particle. */
    private XSParticle makeParticle( XSTerm t ) {
        // UGLY HACK
        return new ParticleImpl( null, null, (Ref.Term)t, t.getLocator() );
    }

    public Object modelGroupDecl(XSModelGroupDecl decl) {
        // push a new JClassFactory so that the new classes will be prefixed by
        // the model group name
        builder.selector.pushClassFactory(
            new PrefixedJClassFactoryImpl( builder, decl ) );
        
        Object r = build(decl.getModelGroup());
        
        builder.selector.popClassFactory();
        
        return r;
    }

    // called when a wildcard is mapped to a class
    public Object wildcard(XSWildcard wc) {
        return particle( makeParticle(wc), null );
    }
    
    // called when a model group (not model group decl) is mapped to a class
    public Object modelGroup(XSModelGroup mg) {
        if( builder.getGlobalBinding().isModelGroupBinding() ) {
            // otherwise just apply things recursively.
            return builder.applyRecursively( mg,
                new BGMBuilder.ParticleHandler() {
                    public Object particle(XSParticle p) {
                        return FieldBuilder.this.particle(p,null);
                    }
                } );
        } else {
            // make a shallow copy of this model group so that the class customization
            // attached to this model group won't be recognized by the particle builder.
            XSModelGroup mg2 = new ModelGroupImpl(
                (SchemaImpl)mg.getOwnerSchema(), null, mg.getLocator(),
                mg.getCompositor(), mg.getChildren() );
            
            return particle( makeParticle(mg2), null );
        }
    }

    /**
     * Calls the other overloaded version of the createField by using
     * a default name computed from the schema component.
     */
    public FieldItem createFieldItem( Expression typeExp, XSDeclaration source, boolean forConstant ) {
        String defaultName = builder.getNameConverter().toPropertyName(source.getName());
        
        return createFieldItem( defaultName, forConstant, typeExp, source );
    }
    
    /**
     * Calls the other overloaded version of the createField method
     * by computing a default name from a model group.
     */
    public Expression createFieldItem( Expression typeExp, XSModelGroup modelGroup ) {
        try {
            String defaultName = NameGenerator.getName( builder, modelGroup );
            return createFieldItem( defaultName, false, typeExp, modelGroup );
        } catch( ParseException e ) {
            // unable to generate a name.
            builder.errorReporter.error( modelGroup.getLocator(),
                Messages.ERR_CLASS_NAME_IS_REQUIRED );
                    
            // recover by using the empty content model
            return Expression.epsilon;
        }
    }
    
    
    /**
     * Creates a FieldItem. If the schema component has a property
     * customization, it will be honored.
     * 
     * @param defaultName
     *      If the name is not customized, this name will be used
     *      as the default. Note that the name conversion <b>MUST</b>
     *      be applied before this method is called if necessary.
     * @param typeExp
     *      The body expression of the newly created FieldItem.
     * @param source
     *      Schema component from which a new field will be created.
     */
    public FieldItem createFieldItem( String defaultName, boolean forConstant, Expression typeExp, XSComponent source ) {
        BIProperty cust = BIProperty.getCustomization(builder,source);
        return cust.createFieldItem( defaultName, forConstant, typeExp, source );
    }

    public Object elementDecl(XSElementDecl decl) {
        Expression body;

        boolean isMappedToType = (builder.selector.bindToType(decl)!= null);
        
        if( !isMappedToType ) {
            return createFieldItem( builder.typeBuilder.elementDeclFlat(decl), decl, false );
        }
        
        // this element will become a class.
        
        Expression type = builder.selector.bindToType(decl.getType());
        
        if(type!=null) {
            // the type of this element decl will become a class.
            _assert( type instanceof ClassItem );
            ClassItem defaultType = (ClassItem)type;
            
            if( decl.getType() instanceof XSComplexType
            && builder.getGlobalBinding().isTypeSubstitutionSupportEnabled() )
                // The core type expression has to come before the substitutable
                // type list, otherwise you'll always get @xsi:type.
                type = pool.createChoice( type, builder.getTypeSubstitutionList(
                    (XSComplexType)decl.getType(), false ) );
            else
                type = pool.createSequence( type, builder.createXsiTypeExp(decl) );
            
            
            if( builder.getGlobalBinding().isTypeSubstitutionSupportEnabled() ) {
                // if both the element and the underlying type become
                // classes then have the element class inherit the type class.
                FieldItem fi = new FieldItem("ValueObject",type,decl.getLocator());
                fi.realization = new XsiTypeFieldRenderer.Factory(defaultType);
                fi.setDelegation(true);
                fi.javadoc=Messages.format(Messages.JAVADOC_VALUEOBJECT_PROPERTY,
                    defaultType.getType().fullName(),fi.name);
                body = fi;
            } else {
                // old code that doesn't handle type substitution
                body = new SuperClassItem(type,decl.getLocator());
            }
        } else {
            // UGLY CODE WARNING:
            // set the referer so that the base type can find this element type
            // if it's a simple type. See comments to the referer field of
            // SimpleTypeBuilder.
            // builder.simpleTypeBuilder.referer = decl;
            builder.simpleTypeBuilder.refererStack.push( decl );

            body = build(decl.getType());

            builder.simpleTypeBuilder.refererStack.pop();

            body= pool.createSequence( body, builder.createXsiTypeExp(decl) );
        }
        
        SimpleNameClass name =
            new SimpleNameClass(decl.getTargetNamespace(),decl.getName());
        
        if(decl.isNillable()) {
            // add xsi:nil support for this generated class.
            
            // [RESULT]
            // <choice>
            //    ... original content model ...
            //   <field name="nil">
            //     <attribute name="xsi:nil">
            //       <primitive>
            //         <data type="boolean"/>
            //       </primitive>
            //     </attribute>
            //   </field>
            // </choice>
    
            FieldItem fi = new FieldItem( "Nil", buildXsiNilExpForClass(), decl.getLocator() );
            // this field needs to be rendered in a different way
            fi.realization = XsiNilFieldRenderer.theFactory;
            fi.javadoc = Messages.format(Messages.JAVADOC_NIL_PROPERTY);
            
            // check the collision with nil. since body and the Nil field
            // are separated by choice, this is not detected by a regular
            // property name collision checker.
            // UGLY. FIX ME. this check should be done elsewhere
            body.visit(new BGMWalker() {
                public Object onField(FieldItem item) {
                    if( item.name.equals("Nil") )
                        throw new InternalError();  // collision!
                    return null;
                }
            });
            
            body = pool.createChoice( fi, body );
        }
        
        return new ElementPattern(name,body);
    }
    
    /**
     * Builds a BGM type fragment for the xsi:nil attribute.
     * Returned expression shall be used to add xsi:nil support
     * for classes.
     */
    private Expression buildXsiNilExpForClass() {
        return new AttributeExp(
            new SimpleNameClass(Const.XMLSchemaInstanceNSURI,"nil"),
            builder.grammar.createPrimitiveItem(
                WhitespaceTransducer.create(
                    BuiltinDatatypeTransducerFactory.get(
                        builder.grammar,BooleanType.theInstance),
                    builder.grammar.codeModel,
                    WhitespaceNormalizer.COLLAPSE),
                BooleanType.theInstance,
                pool.createData(BooleanType.theInstance),
                null )
            );
    }
    
    /** Converts an XML name to the corresponding Java name. */
    private String makeJavaName( String xmlName ) {
        return builder.getNameConverter().toPropertyName(xmlName);
    }
    
    /** Converts an XML name to the corresponding Java constant name. */
    private String makeJavaConstName( String xmlName ) {
        return builder.getNameConverter().toConstantName(xmlName);
    }


}
