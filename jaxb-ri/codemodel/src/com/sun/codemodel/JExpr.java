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

package com.sun.codemodel;


/**
 * Factory methods that generate various expressions.
 */
public abstract class JExpr {

    /**
     * This class is not instanciable.
     */
    private JExpr() { }

    public static JExpression assign(JAssignmentTarget lhs, JExpression rhs) {
        return new JAssignment(lhs, rhs);
    }

    public static JExpression assignPlus(JAssignmentTarget lhs, JExpression rhs) {
        return new JAssignment(lhs, rhs, "+");
    }

    public static JInvocation _new(JClass c) {
        return new JInvocation(c);
    }

    public static JInvocation _new(JType t) {
        return new JInvocation(t);
    }
    
    public static JInvocation invoke(String method) {
        return new JInvocation((JExpression)null, method);
    }
    
    public static JInvocation invoke(JMethod method) {
        return new JInvocation((JExpression)null,method.name());
    }

    public static JInvocation invoke(JExpression lhs, JMethod method) {
        return new JInvocation(lhs, method.name());
    }

    public static JInvocation invoke(JExpression lhs, String method) {
        return new JInvocation(lhs, method);
    }

    public static JFieldRef ref(String field) {
        return new JFieldRef((JExpression)null, field);
    }

    public static JFieldRef ref(JExpression lhs, JVar field) {
        return new JFieldRef(lhs, field.name());
    }

    public static JFieldRef ref(JExpression lhs, String field) {
        return new JFieldRef(lhs, field);
    }

    public static JFieldRef refthis(String field) {
         return new JFieldRef(null, field, true);
    }

    public static JExpression dotclass(final JClass cl) {
        return new JExpressionImpl() {
                public void generate(JFormatter f) {
                    f.p('(').g(cl).p(".class)");
                }
            };
    }

    public static JExpression dotclass(final JType t) {
        return new JExpressionImpl() {
                public void generate(JFormatter f) {
                    f.p('(').g(t).p(".class)");
                }
            };
    }

    public static JArrayCompRef component(JExpression lhs, JExpression index) {
        return new JArrayCompRef(lhs, index);
    }

    public static JCast cast(JType type, JExpression expr) {
        return new JCast(type, expr);
    }

    public static JArray newArray(JType type) {
        return new JArray(type, null);
    }

    public static JArray newArray(JType type, JExpression size) {
        return new JArray(type, size);
    }

    public static JArray newArray(JType type, int size) {
        return newArray(type,JExpr.lit(size));
    }
    
    
    private static final JExpression __this = new JAtom("this");
    /**
     * Returns a reference to "this", an implicit reference
     * to the current object.
     */
    public static JExpression _this() { return __this; }

    private static final JExpression __super = new JAtom("super");
    /**
     * Returns a reference to "super", an implicit reference
     * to the super class.
     */
    public static JExpression _super() { return __super; }
    
    
    /* -- Literals -- */

    private static final JExpression __null = new JAtom("null");
    public static JExpression _null() {
        return __null;
    }
    
    /**
     * Boolean constant that represents <code>true</code>
     */
    public static final JExpression TRUE = new JAtom("true");
    
    /**
     * Boolean constant that represents <code>false</code>
     */
    public static final JExpression FALSE = new JAtom("false");

    public static JExpression lit(int n) {
        return new JAtom(Integer.toString(n));
    }

    public static JExpression lit(long n) {
        return new JAtom(Long.toString(n) + "L");
    }

    public static JExpression lit(float f) {
        return new JAtom(Float.toString(f) + "F");
    }

    public static JExpression lit(double d) {
        return new JAtom(Double.toString(d) + "D");
    }

    static final String charEscape = "\bb\tt\nn\ff\rr\"\"\''\\\\";
    
    /**
     * Escapes the given string, then surrounds it by the specified
     * quotation mark. 
     */
    public static String quotify(char quote, String s) {
        int n = s.length();
        StringBuffer sb = new StringBuffer(n + 2);
        sb.append(quote);
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            int j;
            for( j=0; j<8; j++ )
                if(c==charEscape.charAt(j*2)) {
                    sb.append('\\');
                    sb.append(charEscape.charAt(j*2+1));
                    break;
                }
            if(j==8) {
                // technically Unicode escape shouldn't be done here,
                // for it's a lexical level handling.
                // 
                // However, various tools are so broken around this area,
                // so just to be on the safe side, it's better to do
                // the escaping here (regardless of the actual file encoding)
                //
                // see bug 
                if( c<0x20 || 0x7E<c ) {
                    // not printable. use Unicode escape
                    sb.append("\\u");
                    String hex = Integer.toHexString(((int)c)&0xFFFF);
                    for( int k=hex.length(); k<4; k++ )
                        sb.append('0');
                    sb.append(hex);
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append(quote);
        return sb.toString();
    }

    public static JExpression lit(char c) {
        return new JAtom(quotify('\'', "" + c));
    }

    public static JExpression lit(String s) {
        return new JStringLiteral(s);
    }
    
    /**
     * Creates an expression directly from a source code fragment.
     * 
     * <p>
     * This method can be used as a short-cut to create a JExpression.
     * For example, instead of <code>_a.gt(_b)</code>, you can write
     * it as: <code>JExpr.direct("a>b")</code>.
     * 
     * <p>
     * Be warned that there is a danger in using this method,
     * as it obfuscates the object model.
     */
    public static JExpression direct( final String source ) {
        return new JExpressionImpl(){
            public void generate( JFormatter f ) {
                    f.p('(').p(source).p(')');
            }
        };
    }
}

