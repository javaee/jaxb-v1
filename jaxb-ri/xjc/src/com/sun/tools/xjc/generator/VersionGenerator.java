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
 * @version $Revision: 1.2 $
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
