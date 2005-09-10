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
package com.sun.tools.xjc.reader.xmlschema.cs;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.tools.xjc.grammar.ext.DOMItemFactory;
import com.sun.tools.xjc.reader.xmlschema.AbstractXSFunctionImpl;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.WildcardNameClassBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXDom;
import com.sun.xml.bind.JAXBAssertionError;
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
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSTermFunction;

/**
 * Binds components to DOM if so instructed (by a customization.)
 * 
 * This visitor returns null if a component is not mapped to a DOM.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class DOMBinder extends AbstractXSFunctionImpl {
// references to surrounding context
    private final BGMBuilder builder;
    private final ClassSelector selector;
    private final ExpressionPool pool;
    
    DOMBinder(ClassSelector _selector) {
        this.selector = _selector;
        this.builder = selector.builder;
        this.pool = builder.pool;
    }
    
// entry point
    public Expression bind(XSComponent sc) {
        return (Expression)sc.apply(this);
    }
    public TypeItem bind(XSElementDecl sc) {
        return (TypeItem)sc.apply(this);
    }
    
    
    
// meat
    public Object particle(XSParticle p) {
        BIXDom c = (BIXDom)builder.getBindInfo(p).get(BIXDom.NAME);
        if(c==null)     return null;
        
        return new Builder(c).particle(p);
    }
    
    private Expression bindTerm(XSTerm t) {
        BIXDom c = (BIXDom)builder.getBindInfo(t).get(BIXDom.NAME);
        if(c==null)     return null;
        
        return (Expression)t.apply(new Builder(c));
    }

    public Object wildcard(XSWildcard wc) {
        // the customization takes precedence.
        Expression exp = bindTerm(wc);
        if(exp!=null)     return exp;
        
        if( (wc.getMode()==XSWildcard.SKIP || wc.getMode()==XSWildcard.LAX)
         && builder.getGlobalBinding().smartWildcardDefaultBinding ) {
            try {
                // in the extension mode
                return DOMItemFactory.getInstance("W3C").create(
                    WildcardNameClassBuilder.build(wc),
                    builder.grammar,
                    wc.getLocator() );
            } catch (DOMItemFactory.UndefinedNameException e) {
                // impossible
                e.printStackTrace();
                throw new JAXBAssertionError();
            }
        }
        
        return null;
    }

    public Object modelGroupDecl(XSModelGroupDecl decl) {
        return bindTerm(decl);
    }

    public Object modelGroup(XSModelGroup group) {
        return bindTerm(group);
    }

    public Object elementDecl(XSElementDecl decl) {
        return bindTerm(decl);
    }
    
    /**
     * Builds a DOM fragment by following the customization.
     */
    private class Builder implements XSTermFunction {
        /** Customization that started this builder. */
        private final BIXDom custom;

        Builder(BIXDom c) {
            this.custom = c;
        }
        
        public Object wildcard(XSWildcard wc) {
            return custom.create(
                WildcardNameClassBuilder.build(wc),
                builder.grammar,
                wc.getLocator() );
        }

 
        public Object modelGroupDecl(XSModelGroupDecl decl) {
            return modelGroup(decl.getModelGroup());
        }

        public Object modelGroup(XSModelGroup group) {
            // TODO: duplicate of code ...
            Expression exp;
            XSModelGroup.Compositor comp = group.getCompositor();
        
            if(comp==XSModelGroup.CHOICE)   exp = Expression.nullSet;
            else                            exp = Expression.epsilon;
        
            for( int i=0; i<group.getSize(); i++ ) {
                Expression item = particle(group.getChild(i));
            
                if( comp==XSModelGroup.CHOICE)
                    exp = pool.createChoice(exp,item);
                if( comp==XSModelGroup.SEQUENCE)
                    exp = pool.createSequence(exp,item);
                if( comp==XSModelGroup.ALL)
                    exp = pool.createInterleave(exp,item);
            }
        
            return exp;
        }

        public Object elementDecl(XSElementDecl decl) {
            return custom.create(
                new SimpleNameClass( decl.getTargetNamespace(), decl.getName() ),
                builder.grammar,
                decl.getLocator() );
        }
        
        public Expression particle(XSParticle particle) {
            XSTerm t = particle.getTerm();
            Expression exp = (Expression)t.apply(this);
            return builder.processMinMax(exp,particle);
        }
    }
    
    
    
// those will never be mapped to DOM
    public Object attGroupDecl(XSAttGroupDecl decl) {
        return null;
    }

    public Object attributeDecl(XSAttributeDecl decl) {
        return null;
    }

    public Object attributeUse(XSAttributeUse use) {
        return null;
    }

    public Object complexType(XSComplexType type) {
        return null;
    }

    public Object simpleType(XSSimpleType simpleType) {
        return null;
    }

    public Object empty(XSContentType empty) {
        return null;
    }
}
