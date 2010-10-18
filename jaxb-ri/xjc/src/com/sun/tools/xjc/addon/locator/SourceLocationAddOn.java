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

package com.sun.tools.xjc.addon.locator;

import java.io.IOException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.CodeAugmenter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.runtime.UnmarshallingContext;
import com.sun.xml.bind.Locatable;

/**
 * Generates JAXB objects that implement {@link Locatable}.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class SourceLocationAddOn implements CodeAugmenter {

    public String getOptionName() {
        return "Xlocator";
    }

    public String getUsage() {
        return "  -Xlocator          :  enable source location support for generated code";
    }

    public int parseArgument(Options opt, String[] args, int i) throws BadCommandLineException, IOException {
        return 0;   // no option recognized
    }

    private static final String fieldName = "locator";

    public boolean run(
        AnnotatedGrammar grammar,
        GeneratorContext context,
        Options opt,
        ErrorHandler errorHandler ) {
        
        JCodeModel codeModel = grammar.codeModel;
        
        ClassItem[] cis = grammar.getClasses();
        for (int i = 0; i < cis.length; i++) {
            JDefinedClass impl = context.getClassContext(cis[i]).implClass;
            if (cis[i].getSuperClass() == null) {
                JVar $loc = impl.field(JMod.PROTECTED, Locator.class, fieldName);
                impl._implements(Locatable.class);

                impl.method(JMod.PUBLIC, Locator.class, "sourceLocation").body()._return($loc);
            }

            JClass[] inner = impl.listClasses();
            for (int j = 0; j < inner.length; j++)
                if (inner[j].name().equals("Unmarshaller")) {
                    JDefinedClass unm = (JDefinedClass)inner[j];
                    
                    JMethod cons = unm.getConstructor(
                        new JType[]{context.getRuntime(UnmarshallingContext.class)});
                    // TODO: if(cons==null) exit gracefully;
                    
                    // [RESULT]
                    // if(locator==null)
                    //     locator = new LocatorImpl(context.getLocator());
                    JFieldRef locatorField = JExpr.ref(fieldName);
                    cons.body()._if(locatorField.eq(JExpr._null()))
                        ._then().assign(locatorField,
                            JExpr._new(codeModel.ref(LocatorImpl.class))
                                .arg(cons.listParams()[0].invoke("getLocator")));
                }
        }
        
        return true;
    }
}
