/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
