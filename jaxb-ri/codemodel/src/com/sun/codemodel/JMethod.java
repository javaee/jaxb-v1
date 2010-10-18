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

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.codemodel.util.ClassNameComparator;


/**
 * Java method
 */

public class JMethod implements JDeclaration {

    /**
     * Modifiers for this method
     */
    private JMods mods;

    /**
     * Return type for this method
     */
    private JType type = null;

    /**
     * Name of this method
     */
    private String name = null;

    /**
     * List of parameters for this method's declaration
     */
    private final List params = new ArrayList();

    /**
     * Set of exceptions that this method may throw
     */
    private final Set _throws = new TreeSet(ClassNameComparator.theInstance);
    
    /**
     * JBlock of statements that makes up the body this method
     */
    private JBlock body = null;

    private JDefinedClass outer;

    /**
     * javadoc comments for this JMethod
     */
    private JDocComment jdoc = null;
    
    private boolean isConstructor() {
	return type == null;
    }

    /**
     * JMethod constructor
     *
     * @param mods
     *        Modifiers for this method's declaration
     *
     * @param type
     *        Return type for the method
     *
     * @param name
     *        Name of this method
     */
    JMethod(JDefinedClass outer, int mods, JType type, String name) {
        this.mods = JMods.forMethod(mods);
        this.type = type;
        this.name = name;
        this.outer = outer;
    }

    /**
     * Constructor constructor
     *
     * @param mods
     *        Modifiers for this constructor's declaration
     *
     * @param _class
     *        JClass containing this constructor
     */
    JMethod(int mods, JDefinedClass _class) {
        this.mods = JMods.forMethod(mods);
        this.type = null;
        this.name = _class.name();
        this.outer = _class;
    }

    /**
     * Add an exception to the list of exceptions that this
     * method may throw.
     *
     * @param exception
     *        Name of an exception that this method may throw
     */
    public JMethod _throws(JClass exception) {
        _throws.add(exception);
        return this;
    }
    
    public JMethod _throws(Class exception) {
        return _throws(outer.owner().ref(exception));
    }

    /**
     * Add the specified variable to the list of parameters
     * for this method signature.
     *
     * @param type
     *        JType of the parameter being added
     *
     * @param name
     *        Name of the parameter being added
     *
     * @return New parameter variable
     */
    public JVar param(int mods, JType type, String name) {
        JVar v = new JVar(JMods.forVar(mods), type, name, null);
        params.add(v);
        return v;
    }

    public JVar param(JType type, String name) {
        return param(JMod.NONE, type, name);
    }
    
    public JVar param(int mods, Class type, String name ) {
        return param(mods,outer.owner().ref(type),name);
    }
    
    public JVar param(Class type, String name) {
        return param(outer.owner().ref(type),name);
    }

    public String name() {
        return name;
    }
    
    /**
     * Returns the return type.
     * @return
     */
    public JType type() {
    	return type;
    }
    
    /**
     * Returns all the parameter types in an array.
     * @return
     *      If there's no parameter, an empty array will be returned.
     */
    public JType[] listParamTypes() {
        JType[] r = new JType[params.size()];
        for( int i=0; i<r.length; i++ )
            r[i] = ((JVar)params.get(i)).type();
        return r;
    }
    
    /**
     * Returns all the parameters in an array.
     * @return
     *      If there's no parameter, an empty array will be returned.
     */
    public JVar[] listParams() {
        return (JVar[]) params.toArray(new JVar[params.size()]);
    }
    
    /**
     * Returns true if the method has the specified signature.
     */
    public boolean hasSignature( JType[] argTypes ) {
        JVar[] p = listParams();
        if( p.length!=argTypes.length )
            return false;
                
        for( int i=0; i<p.length; i++ )
            if( !p[i].type.equals(argTypes[i]) )
                return false;
                
        return true;
    }
    

    /**
     * Get the block that makes up body of this method
     *
     * @return Body of method
     */
    public JBlock body() {
        if (body == null)
            body = new JBlock();
        return body;
    }
    
    /**
     * Creates, if necessary, and returns the class javadoc for this
     * JDefinedClass
     *
     * @return JDocComment containing javadocs for this class
     */
    public JDocComment javadoc() {
        if( jdoc == null ) 
                jdoc = new JDocComment();
        return jdoc;
    }

    public void declare(JFormatter f) {
        if( jdoc != null )
            f.g( jdoc );
        
        f.g(mods);
        if (!isConstructor())
            f.g(type);
        f.p(name).p('(');
        boolean first = true;
        for (Iterator i = params.iterator(); i.hasNext();) {
            if (!first)
                f.p(',');
            f.b((JVar) (i.next()));
            first = false;
        }
        f.p(')');
        if (!_throws.isEmpty()) {
            f.nl().i().p("throws");
            first = true;
            for (Iterator i = _throws.iterator(); i.hasNext();) {
                if (!first)
                    f.p(',');
                f.g((JClass) (i.next()));
                first = false;
            }
            f.nl().o();
        }
        if (body != null) {
            f.s(body);
        } else if (!outer.isInterface() && !mods.isAbstract() && !mods.isNative()) {
            // Print an empty body for non-native, non-abstract methods
            f.s(new JBlock());
        } else {
            f.p(';').nl();
        }
    }

    /**
     * @return
     *      the current modifiers of this method.
     *      Always return non-null valid object. 
     */
    public JMods getMods() {
        return mods;
    }
}
