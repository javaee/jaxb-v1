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
package com.sun.tools.xjc.grammar.ext;

import org.xml.sax.Locator;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.trex.ElementPattern;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ExternalItem;

/**
 * A common implementation for DOM-like binding.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
abstract class AbstractDOMItem extends ExternalItem {
    
    private final Expression agm;
    protected final JCodeModel codeModel;
    
    public AbstractDOMItem(NameClass _elementName, AnnotatedGrammar grammar, Locator loc) {
        super("dom", _elementName,loc);
        ExpressionPool pool = grammar.getPool();
        
        this.codeModel = grammar.codeModel;

        // TODO: add the child parameter and ask the caller to build the right representation  
//      this.exp = child;
        ReferenceExp any = new ReferenceExp(null);
        any.exp = pool.createMixed( 
            pool.createZeroOrMore( pool.createChoice(
                pool.createAttribute(NameClass.ALL),
                new ElementPattern( NameClass.ALL, any ) ) ) );
        this.exp = new ElementPattern( _elementName, any ); 
        this.agm = exp;
    }
    
    /**
     * Utility method that creates a reference to a class
     * that doesn't exist in the classpath by assuming that 
     * it derives from {@link Object}.
     */
    protected final JType createPhantomType( String name ) {
        try {
            JDefinedClass def = codeModel._class(name);
            def.hide();
            return def;
        } catch (JClassAlreadyExistsException e) {
            return e.getExistingClass();
        }
        
    }
    
    public Expression createAGM(ExpressionPool pool) {
        return agm;
    }

    public Expression createValidationFragment() {
        return agm;
    }


}
