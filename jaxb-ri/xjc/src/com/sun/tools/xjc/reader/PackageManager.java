/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc.reader;

import com.sun.codemodel.JPackage;

/**
 * Keeps track of all packages that are used.
 * 
 * <p>
 * For the backend to work correctly, the frontend needs to
 * remember all packages it touched. This class will do
 * this job.
 */
public interface PackageManager
{
    /**
     * Gets the "current" package.
     * 
     * Typically, classes or interfaces are generated
     * into the returned package.
     * 
     * @return
     *      Non-null valid JPackage object.
     */
    JPackage getCurrentPackage();
}
