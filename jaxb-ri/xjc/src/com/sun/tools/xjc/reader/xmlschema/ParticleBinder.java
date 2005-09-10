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

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;

/**
 * Base interface for various algorithms that bind a particle.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class ParticleBinder {
    /**
     * Builds a BGM fragment from a particle.
     * The fragment will have a FieldItem as its top-level
     * binding primitive.
     * 
     * @param   superClass
     *      If the newly created class is derived from another class,
     *      this parameter specifies the base class. The callee must
     *      check any name collision.
     *      Null if the class doesn't derive from any other class.
     */
    public abstract Expression build( XSParticle p, ClassItem superClass );
    
    /**
     * Similar to the build method but this method only checks if
     * the BGM that will be built by the build method will
     * do the fallback (map all the properties into one list) or not.
     * 
     * @return 
     *      false if the fallback will not happen.
     */
    public abstract boolean checkFallback( XSParticle p, ClassItem superClass );
    
    


//
// utility methods
//
    protected ParticleBinder( BGMBuilder builder ) {
        this.builder = builder;
        this.pool = builder.pool;
    }
        
    protected final BGMBuilder builder;
    protected final ExpressionPool pool;

    /**
     * Checks if the given term is a reference to a global element
     * and if we need to have {@link com.sun.tools.xjc.grammar.SkippableElementExp}
     * instead of regular {@link com.sun.msv.grammar.ElementExp}.
     */
    protected final boolean needSkippableElement(XSElementDecl e) {
        return e.isGlobal() && e.getType().isComplexType();
    }
    
    /**
     * Checks if a reference to the given term needs "skipping"
     * (in stead of referencing the term itself, the "skip" will refer
     * to the complex type of the term, which must be a global element decl.)
     */
    protected final boolean needSkip(XSTerm t) {
        return isGlobalElementDecl(t) && builder.selector.bindToType(t) instanceof ClassItem; 
    }

    protected final boolean isGlobalElementDecl( XSTerm t ) {
        XSElementDecl e = t.asElementDecl();
        return e!=null && e.isGlobal();
    }
    
    
    /**
     * Gets the BIProperty object that applies to the given particle.
     */
    protected final BIProperty getLocalPropCustomization( XSParticle p ) {
        // check the property customization of this component first
        BIProperty cust = (BIProperty)builder.getBindInfo(p).get(BIProperty.NAME);
        if(cust!=null)  return cust;
        
        // if not, the term might have one.
        cust = (BIProperty)builder.getBindInfo(p.getTerm()).get(BIProperty.NAME);
        if(cust!=null)  return cust;
        
        // TODO: look for the schema default
        return null;
    }

        
    /**
     * Computes the label of a given particle.
     * Usually, the getLabel method should be used instead.
     */
    protected final String computeLabel( XSParticle p ) {
        // if the particle carries a customization, use that value.
        // since we are binding content models, it's always non-constant properties.
        BIProperty cust = getLocalPropCustomization(p);
        if(cust!=null && cust.getPropertyName(false)!=null)
            return cust.getPropertyName(false);
            
        // no explicit property name is given. Compute one.

        XSTerm t = p.getTerm();
            
//        // first, check if a term is going to be a class, if so, use that name.
//        ClassItem ci = builder.selector.select(t);
//        if(ci!=null) {
//            return makeJavaName(ci.getTypeAsDefined().name());
//        }
            
        // if it fails, compute the default name according to the spec.
        if(t.isElementDecl())
            // for element, take the element name.
            return makeJavaName(t.asElementDecl().getName());
        if(t.isModelGroupDecl())
            // for named model groups, take that name
            return makeJavaName(t.asModelGroupDecl().getName());
        if(t.isWildcard())
            // the spec says it will map to "any" by default.
            return "Any";
        if(t.isModelGroup()) {
            try {
                return NameGenerator.getName( builder, t.asModelGroup());
            } catch( ParseException e ) {
                // unable to generate a name.
                builder.errorReporter.error(t.getLocator(),
                    Messages.ERR_UNABLE_TO_GENERATE_NAME_FROM_MODELGROUP);
                return "undefined"; // recover from error by assuming something
            }
        }
            
            
        // there are only four types of XSTerm.
        _assert(false);
        return null;
    }

    /** Converts an XML name to the corresponding Java name. */
    private String makeJavaName( String xmlName ) {
        return builder.getNameConverter().toPropertyName(xmlName);
    }

    protected static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
    
}
