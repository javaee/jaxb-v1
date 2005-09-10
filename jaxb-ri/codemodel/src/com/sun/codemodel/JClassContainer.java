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
package com.sun.codemodel;

import java.util.Iterator;

/**
 * The common aspec of a package and a class.
 */
public interface JClassContainer {
    /**
     * Add a new class to this package/class.
     *
     * @param mods
     *        Modifiers for this class declaration
     *
     * @param name
     *        Name of class to be added to this package
     *
     * @return Newly generated class
     * 
     * @exception JClassAlreadyExistsException
     *      When the specified class/interface was already created.
     */
    JDefinedClass _class(int mods, String name) throws JClassAlreadyExistsException;
    
    /**
     * Add a new public class to this class/package.
     * 
     * @exception JClassAlreadyExistsException
     *      When the specified class/interface was already created.
     */
    public JDefinedClass _class(String name) throws JClassAlreadyExistsException;

    /**
     * Add an interface to this class/package.
     *
     * @param mods
     *        Modifiers for this interface declaration
     *
     * @param name
     *        Name of interface to be added to this package
     *
     * @return Newly generated interface
     * 
     * @exception JClassAlreadyExistsException
     *      When the specified class/interface was already created.
     */
    public JDefinedClass _interface(int mods, String name) throws JClassAlreadyExistsException;

    /**
     * Adds a public interface to this package.
     * 
     * @exception JClassAlreadyExistsException
     *      When the specified class/interface was already created.
     */
    public JDefinedClass _interface(String name) throws JClassAlreadyExistsException;

    /**
     * Create a new class or a new interface.
     */
    public JDefinedClass _class(int mods, String name, boolean isInterface )
        throws JClassAlreadyExistsException;


    /**
     * Returns an iterator that walks the nested classes defined in this
     * class.
     */
    public Iterator classes();
    
    /**
     * Parent JClassContainer.
     * 
     * If this is a package, this method returns a parent package,
     * or null if this package is the root package.
     * 
     * If this is an outer-most class, this method returns a package
     * to which it belongs.
     * 
     * If this is an inner class, this method returns the outer
     * class.
     */
    public JClassContainer parentContainer();
    
    /**
     * Get the root code model object.
     */
    public JCodeModel owner();
}
