/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
 * @version $Revision: 1.1 $
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
