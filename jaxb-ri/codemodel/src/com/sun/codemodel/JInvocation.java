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

package com.sun.codemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * JMethod invocation
 */

public class JInvocation extends JExpressionImpl implements JStatement {

    /**
     * Object expression upon which this method will be invoked, or null if
     * this is a constructor invocation
     */
    private JGenerable object;

    /**
     * Name of the method to be invoked
     */
    private String name;

    private boolean isConstructor = false;

    /**
     * List of argument expressions for this method invocation
     */
    private List args = new ArrayList();

    /**
     * If isConstructor==true, this field keeps the type to be created.
     */
    private JType type = null;

    /**
     * Invokes a method on an object.
     *
     * @param object
     *        JExpression for the object upon which
     *        the named method will be invoked,
     *        or null if none
     *
     * @param name
     *        Name of method to invoke
     */
    JInvocation(JExpression object, String name) {
        this( (JGenerable)object, name );
    }
    
    /**
     * Invokes a static method on a class.
     */
    JInvocation(JClass type, String name) {
        this( (JGenerable)type, name );
    }
    
    private JInvocation(JGenerable object, String name) {
        this.object = object;
        if (name.indexOf('.') >= 0)
            throw new IllegalArgumentException("JClass name contains '.': "
                                               + name);
        this.name = name;
    }
    
    /**
     * Invokes a constructor of an object (i.e., creates
     * a new object.)
     * 
     * @param c
     *      Type of the object to be created. If this type is
     *      an array type, added arguments are treated as array
     *      initializer. Thus you can create an expression like
     *      <code>new int[]{1,2,3,4,5}</code>.
     */
    JInvocation(JType c) {
        this.object = null;
        this.name = c.fullName();
        this.isConstructor = true;
        this.type = c;
    }

    /**
     *  Add an expression to this invocation's argument list
     *
     * @param arg
     *        Argument to add to argument list
     */
    public JInvocation arg(JExpression arg) {
        if(arg==null)   throw new IllegalArgumentException();
        args.add(arg);
        return this;
    }


    public void generate(JFormatter f) {
        if (isConstructor && type.isArray()) {
            // [RESULT] new T[]{arg1,arg2,arg3,...};
            f.p("new").p(name).p('{');
        } else {
            if (isConstructor)
                f.p("new").p(name).p('(');
            else if (object != null)
                f.g(object).p('.').p(name).p('(');
            else
                f.p(name).p('(');
        }
                
        boolean first = true;
        for (Iterator i = args.iterator(); i.hasNext();) {
            if (!first) f.p(',');
            f.g((JExpression)(i.next()));
            first = false;
        }
        
        if (isConstructor && type.isArray())
            f.p('}');
        else 
            f.p(')');
            
        if( type instanceof JAnonymousClass ) {
            ((JAnonymousClass)type).declareBody(f);
        }
    }

    public void state(JFormatter f) {
        f.g(this).p(';').nl();
    }

}
