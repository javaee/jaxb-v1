/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JDefinedClass;

/**
 * This customization will specify the root class for the generated
 * class. This is used as a child of a {@link BIGlobalBinding} object,
 * and this doesn't implement BIDeclaration by itself.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class BIXSuperClass {

    private final JDefinedClass cls;
    
    public BIXSuperClass( JDefinedClass _cls ) {
        cls = _cls;
        _cls.hide();    // don't actually generate this class.
    }
    
    public JDefinedClass getRootClass() { return cls; }
}
