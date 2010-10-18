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

package com.sun.tools.xjc.grammar.util;

import java.util.Vector;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.TypeItem;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * A function object that collects all {@link TypeItem} object
 * below the given expression.
 * 
 * <p>
 * If the function hits {@link FieldItem} or {@link SuperClassItem},
 * the assertion fails.    
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class TypeItemCollector extends BGMWalker {
    private final Vector vec = new Vector();
    
    private TypeItemCollector() {
    }
    
    /**
     * public entry point.
     */
    public static TypeItem[] collect( Expression e ) {
        TypeItemCollector tic = new TypeItemCollector();
        e.visit(tic);
        return (TypeItem[])tic.vec.toArray(new TypeItem[tic.vec.size()]);
    }
    
    public Object onClass(ClassItem item) {
        vec.add(item);
        return null;
    }
    public Object onInterface(InterfaceItem item) {
        vec.add(item);
        return null;
    }
    public Object onPrimitive(PrimitiveItem item) {
        vec.add(item);
        return null;
    }
    public Object onExternal(ExternalItem item) {
        vec.add(item);
        return null;
    }
    public Object onSuper(SuperClassItem item) {
        // we are expecting TypeItem. we can never possibly see
        // this. (since SuperClassItem are not wrapped by FieldItems.)
        throw new JAXBAssertionError();
    }
    public Object onField(FieldItem item) {
        // we are expecting TypeItem. we can never possibly see
        // this. (since FieldItems don't nest.)
        throw new JAXBAssertionError();
    }
    public Object onIgnore(IgnoreItem item) { return null; }
}
