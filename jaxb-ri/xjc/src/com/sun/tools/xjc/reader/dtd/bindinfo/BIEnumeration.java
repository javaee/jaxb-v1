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
package com.sun.tools.xjc.reader.dtd.bindinfo;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.dom4j.Element;

import com.sun.msv.datatype.xsd.NmtokenType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.xducer.EnumerationXducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;

/**
 * &lt;enumeration> declaration in the binding file.
 */
public final class BIEnumeration implements BIConversion
{
    /** Creates an object from &lt;enumeration> declaration. */
    private BIEnumeration( Element _e, Transducer _xducer ) {
        this.e = _e;
        this.xducer = _xducer;
    }
    
    /** &lt;enumeration> element in DOM. */
    private final Element e;
    
    private final Transducer xducer;
    
    public String name() { return e.attributeValue("name"); }
    
    /** Returns a transducer for this enumeration declaration. */
    public Transducer getTransducer() { return xducer; }
    
    
    
    
    /** Creates a global enumeration declaration. */
    static BIEnumeration create( Element dom, BindInfo parent ) {
        // create a class in the target package.
        return new BIEnumeration(
            dom,
            new EnumerationXducer(
                parent.nameConverter,
                parent.classFactory.createClass(
                    parent.getTargetPackage(),
                    dom.attributeValue("name"),
                    null ), // TODO: error reporting support
                buildMemberExp(dom),
                emptyHashMap,
                null/*TODO:location support*/));
    }
    
    /** Creates an element-local enumeration declaration. */
    static BIEnumeration create( Element dom, BIElement parent ) {
        // create a class as a nested class
        return new BIEnumeration(
            dom,
            new EnumerationXducer(
                parent.parent.nameConverter,
                parent.parent.classFactory.createClass(
                    parent.getClassObject(),
                    dom.attributeValue("name"),
                    null ), // TODO: error reporting support
                buildMemberExp(dom),
                emptyHashMap,
                null/*TODO:location support*/ ));
    }
    
    private static final HashMap emptyHashMap = new HashMap();
    
    private static Expression buildMemberExp( Element dom ) {
        String members = dom.attributeValue("members");
        if(members==null) members="";   // TODO: error handling
        
        ExpressionPool pool = new ExpressionPool();
        
        Expression exp = Expression.nullSet;
        StringTokenizer tokens = new StringTokenizer(members);
        while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            
            exp = pool.createChoice( exp,
                pool.createValue(NmtokenType.theInstance,token));
        }
        
        return exp;
    }
}
