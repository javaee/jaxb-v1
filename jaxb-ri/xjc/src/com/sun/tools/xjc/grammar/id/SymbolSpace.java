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
package com.sun.tools.xjc.grammar.id;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;

/**
 * Symbol space for ID/IDREF.
 * 
 * In XJC, the whole ID space is considered to be splitted into
 * one or more "symbol space". For an IDREF to match an ID, we impose
 * additional restriction to the one stated in the XML rec.
 * 
 * <p>
 * That is, XJC'll require that the IDREF belongs to the same symbol
 * space as the ID. Having this concept allows us to assign more
 * specific type to IDREF.
 * 
 * <p>
 * See the design document for detail.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public class SymbolSpace
{
    private JType type;
    private final JCodeModel codeModel;
    
    public SymbolSpace( JCodeModel _codeModel ) {
        this.codeModel = _codeModel;
    }
    
    /**
     * Gets the Java type of this symbol space.
     * 
     * <p>
     * A symbol space is said to have a Java type X if all classes
     * pointed by IDs belonging to this symbol space are assignable
     * to X.
     */
    public JType getType() {
        if(type==null)  return codeModel.ref(Object.class);
        return type;
    }
    
    public void setType( JType _type ) {
        if( this.type==null )
            this.type = _type;
    }
    
    public String toString() {
        if(type==null)  return "undetermined";
        else            return type.name();
    }
}
