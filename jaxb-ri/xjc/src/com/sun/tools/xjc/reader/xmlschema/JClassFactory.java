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
package com.sun.tools.xjc.reader.xmlschema;
import org.xml.sax.Locator;

import com.sun.codemodel.JDefinedClass;

/**
 * Encapsulate where a class is created.
 */
public interface JClassFactory {
    /**
     * Create a new JClass object with the given name.
     * 
     * The actual location where the class is created will be
     * determined by the callee, not by the caller.
     * 
     * @param sourceLocation
     *      This location is recorded as the owner of the new class.
     *      Used to report errors if necessary.
     */
    JDefinedClass create( String name, Locator sourceLocation );
    
    /**
     * Get the parent factory.
     */
    JClassFactory getParentFactory();
}