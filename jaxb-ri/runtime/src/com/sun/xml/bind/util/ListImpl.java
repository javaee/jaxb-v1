/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.bind.util;

import java.util.List;

/**
 * 
 * @since JAXB1.0
 * @author
 * 	Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ListImpl extends ProxyListImpl implements java.io.Serializable {
 
    private final static long serialVersionUID=1L;

    private boolean isModified = false;

    public ListImpl(List c) { super(c); }

    public boolean isModified() { return isModified; }
    public void setModified( boolean f ) { isModified=f; }
    
    public Object clone() {
        ListImpl r = new ListImpl(this.core);
        r.setModified(this.isModified());
        return r;
    }
}
