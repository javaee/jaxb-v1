/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.codemodel;

/**
 * Anonymous class quick hack.
 * 
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
class JAnonymousClass extends JDefinedClass {

    /**
     * Base interface/class from which this anonymous class is built.
     */
    private final JClass base;
    
    JAnonymousClass( JClass _base, JCodeModel owner ) {
        super(0, _base.name(), false, owner);
        this.base = _base;
    }

    public JPackage _package() {
        // this is another quick hack but this makes 
        // JInvocation happy.
        return base._package();
    }

    public JClassContainer parentContainer() {
        // unimplemented
        throw new InternalError();
    }

}
