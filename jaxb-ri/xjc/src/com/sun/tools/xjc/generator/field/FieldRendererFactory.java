/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.generator.field;

import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.FieldUse;

/**
 * Creates a new instance of {@link FieldRenderer} that
 * shall be used to realize the given field use object.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface FieldRendererFactory {
    /**
     * Creates a new {@link FieldRenderer} for the specified
     * FieldUse.
     * 
     * This operation will also generate necessary fields/methods
     * to the corresponding implementation class.
     * 
     * @param context
     *      Provides context information necessary to generated fields.
     */
    FieldRenderer create( ClassContext context, FieldUse fu );
}
