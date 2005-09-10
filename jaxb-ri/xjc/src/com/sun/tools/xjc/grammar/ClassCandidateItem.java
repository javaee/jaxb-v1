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
package com.sun.tools.xjc.grammar;

import org.xml.sax.Locator;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.OtherExp;
import com.sun.tools.xjc.util.CodeModelClassFactory;

/**
 * Used by the RELAX NG parser to indicate that this can
 * be replaced by a {@link ClassItem}. After the entire
 * grammar is parsed, an optimizer will run and which of 
 * ClassCandidateItems should be simply removed and which
 * of them should be promoted to a ClassItem.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class ClassCandidateItem extends OtherExp {
    
    public final String name;
    private final CodeModelClassFactory classFactory;
    private final AnnotatedGrammar grammar;
    public final JPackage targetPackage;
    public final Locator locator;
    
    public ClassCandidateItem( 
        CodeModelClassFactory _classFactory, AnnotatedGrammar _grammar,
        JPackage _targetPackage, String _name, Locator _loc, Expression body ) {
        super(body);
        
        this.grammar = _grammar;
        this.classFactory = _classFactory;
        this.targetPackage = _targetPackage;
        this.name = _name;
        this.locator = _loc;
    }
    
    private ClassItem ci = null;
    
    /**
     * Creates a {@link ClassItem} out of this and returns it.
     * 
     * @return
     *      Calling this method multiple times will return the same object.
     */
    public ClassItem toClassItem() {
        if(ci==null) {
            ci = grammar.createClassItem(
                classFactory.createInterface(targetPackage,name,locator),
                exp, locator );
        }
        return ci;
    }
    
    public String printName() {
        return super.printName()+"#"+name;
    }
}
