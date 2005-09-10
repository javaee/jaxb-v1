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
