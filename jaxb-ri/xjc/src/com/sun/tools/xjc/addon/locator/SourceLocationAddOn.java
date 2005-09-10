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
