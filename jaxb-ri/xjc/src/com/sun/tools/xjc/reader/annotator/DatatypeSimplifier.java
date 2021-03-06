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

package com.sun.tools.xjc.reader.annotator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.msv.datatype.xsd.EnumerationFacet;
import com.sun.msv.datatype.xsd.ListType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.UnionType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.DataExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.util.BreadthFirstExpressionCloner;

/**
 * Tear down complex XML Schema datatype definitions into simpler ones.
 * 
 * <p>
 * For example,
 * 
 * <pre><xmp>
 * <union memberType="def">
 *   <simpleType>
 *     <list itemType="abc" />
 *   </simpleType>
 * </union>
 * </xmp></pre>
 * 
 * <p>
 * will be torn down to
 * 
 * <pre><xmp>
 * <choice>
 *   <data type="def"/>
 *   <list>
 *     <zeroOrMore>
 *       <data type="abc"/>
 *     </zeroOrMore>
 *   </list>
 * </choice>
 * </xmp></pre>
 * 
 * <p>
 * Enumerations are also torn down to choice of ValueExps.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class DatatypeSimplifier extends BreadthFirstExpressionCloner {
    
    public DatatypeSimplifier( ExpressionPool pool ) { super(pool); }
    
    /**
     * A cached result of {@link #onData(DataExp)}.
     * A map from {@link DataExp} to {@link Expression}.
     */
    private final Map dataExps = new HashMap();
    
    public Expression onAnyString() {
        return pool.createData(StringType.theInstance);
    }
    
    public Expression onData( DataExp exp ) {
        if(!( exp.dt instanceof XSDatatype  ))
            // unknown datatype object. keep it as is.
            return exp;
        
        Expression r = (Expression)dataExps.get(exp);
        if(r==null) {
            r = processDatatype( (XSDatatype)exp.dt, false );
            dataExps.put(exp,r);
        }
        return r;
    }
    
    /**
     * Simplifies a given datatype by transfering some of the semantics
     * (enumeration,list,union) into expressions.
     * 
     * <p>
     * For example, instead of having a DataExp with a datatype with enumeration,
     * this method converts it into a choice of ValueExps.
     * 
     * @param    inList
     *        This flag is set to true when we are processing a member type of an item.
     *        We cannot nest ListExp, so this flag is used to prevent such a situation.
     */
    private Expression processDatatype( XSDatatype dt, boolean inList ) {
        
        // see if this datatype has an enumeration facet.
        EnumerationFacet ef =
            (EnumerationFacet)dt.getFacetObject(XSDatatype.FACET_ENUMERATION);
        if(ef!=null)
            return processEnumeration(dt,ef);
        
        
        switch(dt.getVariety()) {
        case XSDatatype.VARIETY_ATOMIC:
            // there is nothing we can do about this datatype.
            return pool.createData(dt);
            
        case XSDatatype.VARIETY_UNION:
            return processUnion(dt,inList);
        
        case XSDatatype.VARIETY_LIST:
            return processList(dt,inList);
            
        default:
            throw new Error();    // assertion failed. impossible
        }
    }
    
    /**
     * Tears down a datatype with an enumeration facet
     * into &lt;choice> of &lt;value>s.
     */
    private Expression processEnumeration( XSDatatype type, EnumerationFacet enums ) {
        Expression exp = Expression.nullSet;
        
        Iterator itr = enums.values.iterator();
        while( itr.hasNext() ) {
            Object v = itr.next();
            
            // TODO: check v to see if it is a valid value for the datatype.
            
            // TODO: I have to provide a type name
            // TODO: instead of using "type" as the type,
            //    we should try to identify the base type that corresponds to v.
            exp = pool.createChoice( exp,
                pool.createValue(type,null,v));
        }
        
        return exp;
    }
    
    /**
     * Tears down an union datatype into
     * &lt;choice> of each member types.
     */
    private Expression processUnion( XSDatatype dt, boolean inList ) {
        if(dt.getFacetObject(XSDatatype.FACET_ENUMERATION)!=null)
            throw new Error(
                            Messages.format( Messages.ENUM_FACET_UNSUPPORTED ) );
        if(dt.getFacetObject(XSDatatype.FACET_PATTERN)!=null)
            throw new Error(
                            Messages.format( Messages.PATTERN_FACET_UNSUPPORTED ) );
        
        // since enumeration and pattern are only facets which is applicable to
        // union type. So this should mean that there is no facet at all.
        
        while(!(dt instanceof UnionType)) {
            dt = dt.getBaseType();
            if(dt==null)
                // assertion failed. there must be UnionType since the variation
                // of this datatype is VARIATION_UNION.
                throw new Error();
        }
        
        final UnionType ut = (UnionType)dt;
        Expression exp = Expression.nullSet;

        // combine member types with choice.
        for( int i=0; i<ut.memberTypes.length; i++ )
            // simplify each member type recurisvely.
            exp = pool.createChoice( exp, processDatatype(ut.memberTypes[i],inList) );
        
        return exp;
    }
    
    private Expression processList( final XSDatatype dt, boolean inList ) {
        if(dt.getFacetObject(XSDatatype.FACET_ENUMERATION)!=null)
            throw new Error(
                            Messages.format( Messages.ENUM_FACET_UNSUPPORTED ) );
        if(dt.getFacetObject(XSDatatype.FACET_PATTERN)!=null)
            throw new Error(
                            Messages.format( Messages.PATTERN_FACET_UNSUPPORTED ) );
        
        // only applicable facets are length,maxLength and minLength.

        XSDatatype d = dt;
        while(!(d instanceof ListType)) {
            d = d.getBaseType();
            if(d==null)
                // assertion failed. there must be ListType since the variation
                // of this datatype is VARIATION_LIST.
                throw new Error();
        }
        final ListType lt = (ListType)d;
        
        // simplify the item type first.
        Expression item = processDatatype(lt.itemType,true);
        
        // TODO: maybe we should process length,maxlength and minlength facets
        // accordingly. It will create more accurate BGM, but such a BGM will
        // probably more difficult to handle.
        
        Expression exp = pool.createZeroOrMore(item);
        
        if(inList)        return exp;
        else            return pool.createList(exp);
    }
}
