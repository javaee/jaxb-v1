/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.grammar;

/**
 * Visitor of JavaItem.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface JavaItemVisitor {
    Object onClass(ClassItem item);
    Object onField(FieldItem item);
    Object onIgnore(IgnoreItem item);
    Object onInterface(InterfaceItem item);
    Object onPrimitive(PrimitiveItem item);
    Object onExternal(ExternalItem item);
    Object onSuper(SuperClassItem item);
}
