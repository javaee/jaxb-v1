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

import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.visitor.XSFunction;

/**
 * Implements the unused callback methods by stub functions.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class AbstractXSFunctionImpl implements XSFunction {

    // these schema components will never be a direct target of the processing.
    public Object annotation(XSAnnotation ann) {
        _assert(false);
        return null;
    }
    public Object schema(XSSchema schema) {
        _assert(false);
        return null;
    }

    public Object facet(XSFacet facet) {
        _assert(false);
        return null;
    }

    public Object notation(XSNotation not) {
        _assert(false);
        return null;
    }


    protected static void _assert( boolean b ) {
        if(!b)
            throw new JAXBAssertionError();
    }
}
