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
package com.sun.tools.xjc.reader.xmlschema.ct;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.msv.grammar.util.NameClassCollisionChecker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.grammar.xducer.IdentityTransducer;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

/**
 * Binds a complex type derived from another complex type by extension.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ExtendedComplexTypeBuilder extends AbstractCTBuilder {
    
    /**
     * Map from {@link XSComplexType} to {@link NameClass}[2] that
     * represents the names used in its child elements [0] and
     * attributes [1].
     */
    private final Map characteristicNameClasses = new HashMap();
    
    public ExtendedComplexTypeBuilder(ComplexTypeFieldBuilder _builder) {
        super(_builder);
    }

    public boolean isApplicable(XSComplexType ct) {
        XSType baseType = ct.getBaseType();
        return baseType!=bgmBuilder.schemas.getAnyType()
            &&  baseType.isComplexType()
            &&  ct.getDerivationMethod()==XSType.EXTENSION;
    }

    public Expression build(XSComplexType ct) {
        XSComplexType baseType = ct.getBaseType().asComplexType();
        
        // build the base type if necessary.
        ClassItem baseClass = bgmBuilder.selector.bindToType(baseType);
        _assert(baseClass!=null);   // global complex type must map to a class
        
        Expression exp = new SuperClassItem(baseClass,ct.getLocator());

        // derivation by extension.
        ComplexTypeBindingMode baseTypeFlag = builder.getBindingMode(baseType);
            
        XSContentType explicitContent = ct.getExplicitContent();
        
        if(!checkIfExtensionSafe(baseType,ct)) {
            // error. We can't handle any further extension
            // TODO: report error
            bgmBuilder.errorReceiver.error(
                ct.getLocator(),
                Messages.format(Messages.ERR_NO_FURTHER_EXTENSION,
                    baseType.getName(), ct.getName() )
            );
            // recover by returning something harmless
            return Expression.epsilon;
        }
            
            
        // explicit content is always either empty or a particle.
        if( explicitContent!=null && explicitContent.asParticle()!=null ) {
                
            if( baseTypeFlag==ComplexTypeBindingMode.NORMAL) {
                // if we have additional explicit content, process them.

                builder.recordBindingMode(ct,
                    bgmBuilder.particleBinder.checkFallback(
                        explicitContent.asParticle(), baseClass)
                    ?ComplexTypeBindingMode.FALLBACK_REST
                    :ComplexTypeBindingMode.NORMAL);
                            
                exp = pool.createSequence(exp,
                    bgmBuilder.particleBinder.build(
                        explicitContent.asParticle(), baseClass));
                    
            } else {
                // adds fallback contents
                
                Expression body = bgmBuilder.typeBuilder.build(explicitContent);
                if( ct.isMixed() )
                    body = pool.createInterleave(
                        pool.createZeroOrMore(
                            bgmBuilder.grammar.createPrimitiveItem(
                                new IdentityTransducer(bgmBuilder.grammar.codeModel),
                                StringType.theInstance,
                                pool.createData(StringType.theInstance),
                                ct.getLocator()) ),
                            body );
                
                FieldItem fi = new FieldItem(
                    baseTypeFlag==ComplexTypeBindingMode.FALLBACK_CONTENT?
                        "Content":"Rest",
                    body,
                    ct.getLocator() );
                fi.multiplicity = Multiplicity.star;
                fi.collisionExpected = true;
                    
                exp = pool.createSequence(exp,fi);
                builder.recordBindingMode(ct, baseTypeFlag );
            }
        } else {
            // if it's empty, no additional processing is necessary
            builder.recordBindingMode(ct, baseTypeFlag );
        }
            
        return pool.createSequence( bgmBuilder.fieldBuilder.attributeContainer(ct), exp );
    }

    /**
     * Checks if this new extension is safe.
     * 
     * UGLY.
     * <p>
     * If you have ctA extending ctB and ctB restricting ctC, our
     * Java classes will look like CtAImpl extending CtBImpl
     * extending CtCImpl. 
     * 
     * <p>
     * Since a derived class unmarshaller uses the base class unmarshaller,
     * this could potentially result in incorrect unmarshalling.
     * We used to just reject such a case, but then we found that
     * there are schemas that are using it.
     * 
     * <p>
     * One generalized observation that we reached is that if the extension
     * is only adding new elements/attributes which has never been used
     * in any of its base class (IOW, if none of the particle / attribute use /
     * attribute wildcard can match the name of newly added elements/attributes) 
     * then it is safe to add them.
     * 
     * <p>
     * This function checks if the derivation chain to this type is
     * not using restriction, and if it is, then checks if it is safe
     * according to the above condition.
     * 
     * @return false
     *      If this complex type needs to be rejected.
     */
    private boolean checkIfExtensionSafe( XSComplexType baseType, XSComplexType thisType ) {
        XSComplexType lastType = getLastRestrictedType(baseType);
        
        if(lastType==null)
            return true;    // no restriction in derivation chain
        
        NameClass anc = NameClass.NONE;
        // build name class for attributes in new complex type
        Iterator itr = thisType.iterateDeclaredAttributeUses();
        while( itr.hasNext() )
            anc = new ChoiceNameClass( anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()) );
        // TODO: attribute wildcard
        anc = anc.simplify();
        
        NameClass enc = getNameClass(thisType.getExplicitContent()).simplify();
        
        // check against every base type ... except the root anyType
        while(lastType!=lastType.getBaseType()) {
            if(checkCollision(anc,enc,lastType))
                return false;

            if(lastType.getBaseType().isSimpleType())
                // if the base type is a simple type, there won't be
                // any further name collision.
                return true;
            
            lastType = lastType.getBaseType().asComplexType();
        } 


        
        return true;    // OK
    }
    
    /**
     * Checks if the particles/attributes defined in the type parameter
     * collides with the name classes of anc/enc.
     * 
     * @return true if there's a collision.
     */
    private boolean checkCollision(NameClass anc, NameClass enc, XSComplexType type) {
        NameClass[] chnc = (NameClass[])characteristicNameClasses.get(type);
        if(chnc==null) {
            chnc = new NameClass[2];
            chnc[0] = getNameClass(type.getContentType());
            
            // build attribute name classes
            NameClass nc = NameClass.NONE;
            Iterator itr = type.iterateAttributeUses();
            while( itr.hasNext() )
                anc = new ChoiceNameClass( anc, getNameClass(((XSAttributeUse)itr.next()).getDecl()) );
            XSWildcard wc = type.getAttributeWildcard();
            if(wc!=null)
                nc = new ChoiceNameClass( nc, WildcardNameClassBuilder.build(wc) );
            chnc[1] = nc;
            
            characteristicNameClasses.put(type,chnc);
        }
        
        return collisionChecker.check( chnc[0], enc ) || collisionChecker.check( chnc[1], anc );
    }
    
    /**
     * Gets a {@link NameClass} that represents all the terms in the given content type.
     * If t is not a particle, just return an empty name class.
     */
    private NameClass getNameClass( XSContentType t ) {
        if(t==null) return NameClass.NONE;
        XSParticle p = t.asParticle();
        if(p==null) return NameClass.NONE;
        else        return (NameClass)p.getTerm().apply(contentModelNameClassBuilder);
    }
    
    /**
     * Gets a {@link SimpleNameClass} from the name of a {@link XSDeclaration}.
     */
    private NameClass getNameClass( XSDeclaration decl ) {
        return new SimpleNameClass(decl.getTargetNamespace(),decl.getName());
    }
    
    private final NameClassCollisionChecker collisionChecker = new NameClassCollisionChecker();
    /**
     * Computes a name class that represents everything in a given content model.
     */
    private final XSTermFunction contentModelNameClassBuilder = new XSTermFunction() {
        public Object wildcard(XSWildcard wc) {
            return WildcardNameClassBuilder.build(wc);
        }

        public Object modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        public Object modelGroup(XSModelGroup group) {
            NameClass nc = NameClass.NONE;
            for( int i=0; i<group.getSize(); i++ )
                nc = new ChoiceNameClass(nc,(NameClass)group.getChild(i).getTerm().apply(this));
            return nc;
        }

        public Object elementDecl(XSElementDecl decl) {
            return getNameClass(decl);
        }
    };
    
    
    /**
     * Looks for the derivation chain t_1 > t_2 > ... > t
     * and find t_i such that t_i derives by restriction but
     * for every j>i, t_j derives by extension.
     * 
     * @return null
     *      If there's no such t_i or if t_i is any type.
     */
    private XSComplexType getLastRestrictedType( XSComplexType t ) {
        if( t.getBaseType()==bgmBuilder.schemas.getAnyType() )
            return null;   // we don't count the restriction from anyType
        if( t.getDerivationMethod()==XSType.RESTRICTION )
            return t;
            
        XSComplexType baseType = t.getBaseType().asComplexType();
        if(baseType!=null)
            return getLastRestrictedType(baseType);
        else
            return null;
    }
}
