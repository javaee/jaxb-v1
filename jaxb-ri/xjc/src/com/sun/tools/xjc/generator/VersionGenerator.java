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

package com.sun.tools.xjc.generator;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;

/**
 * This class generates a simple Version.java class in each impl package.
 * 
 * @author Ryan Shoemaker, Sun Microsstems, Inc.
 * @version $Revision: 1.3 $
 */
final class VersionGenerator {

    private final JCodeModel codeModel;

    private final GeneratorContext context;

    private final JPackage targetPackage;
    
    public final JDefinedClass versionClass;


    VersionGenerator(
        GeneratorContext _context,
        AnnotatedGrammar _grammar,
        JPackage _pkg) {

        this.context = _context;
        this.codeModel = _grammar.codeModel;
        this.targetPackage = _pkg;

        // create the Version class skeleton
        versionClass =
            context.getClassFactory().createClass(
                targetPackage,
                "JAXBVersion",
                null);

        generate();
    }

    /**
     * Generate the body of the JAXBVersion class.
     */
    private void generate() {
        // [RESULT]:
        //    public static final String version = "x.y.z";
        versionClass.field(
            JMod.PUBLIC | JMod.STATIC | JMod.FINAL,
            codeModel.ref(String.class),
            "version",
            JExpr.lit(Messages.format(Messages.VERSION_FIELD)));
    }

    /**
     * Generate a public static field in the JDefinedClass that references 
     * the generated JAXBVersion class.
     * 
     * @param impl the class to add the reference to
     */
    void generateVersionReference(JDefinedClass impl) {
        // [RESULT]
        //    public static final Class version = JAXBVersion.class;
        impl.field(
            JMod.PUBLIC | JMod.STATIC | JMod.FINAL,
            codeModel.ref(Class.class),
            "version",
            versionClass.dotclass());
    }

    void generateVersionReference(ClassContext cc) {
        generateVersionReference(cc.implClass);
    }

}
