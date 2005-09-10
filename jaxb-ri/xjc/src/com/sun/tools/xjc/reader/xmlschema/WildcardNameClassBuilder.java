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

import java.util.Iterator;

import com.sun.msv.grammar.ChoiceNameClass;
import com.sun.msv.grammar.DifferenceNameClass;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.NamespaceNameClass;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.visitor.XSWildcardFunction;

/**
 * Builds a name class representation of a wildcard.
 * 
 * <p>
 * Singleton. Use the build method to create a NameClass.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class WildcardNameClassBuilder implements XSWildcardFunction {
    private WildcardNameClassBuilder() {}
    
    private static final XSWildcardFunction theInstance =
        new WildcardNameClassBuilder();
    
    public static NameClass build( XSWildcard wc ) {
        return (NameClass)wc.apply(theInstance);
    }
    
    public Object any(XSWildcard.Any wc) {
        return NameClass.ALL;
    }

    public Object other(XSWildcard.Other wc) {
        return new DifferenceNameClass(
            NameClass.ALL,
            new ChoiceNameClass(
                new NamespaceNameClass(""),
                new NamespaceNameClass(wc.getOtherNamespace())));
    }

    public Object union(XSWildcard.Union wc) {
        NameClass nc = null;
        for (Iterator itr = wc.iterateNamespaces(); itr.hasNext();) {
            String ns = (String) itr.next();
            
            if(nc==null)    nc = new NamespaceNameClass(ns);
            else
                nc = new ChoiceNameClass(nc,new NamespaceNameClass(ns));
        }
        
        if(nc==null)
            // there should be at least one.
            throw new JAXBAssertionError();
        
        return nc;
    }

}
