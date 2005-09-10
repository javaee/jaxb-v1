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

import java.util.Iterator;
import java.util.Set;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.field.FieldRendererFactory;
import com.sun.tools.xjc.grammar.util.Multiplicity;

/**
 * aggregated field information.
 * 
 * Information about one field can be possibly spanned across
 * multiple FieldItems. This object serves as a bundle of those FieldItems
 * that share the same name.
 * 
 * @author
 *    <a href="mailto:kohsuke.kawaguchi@sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class FieldUse {
    
    /** Field name */
    public final String name;
    
    /** Parent ClassItem object to which this field belongs. */
    public final ClassItem owner;
    
    public final JCodeModel codeModel;
    
    /**
     * Item type of this field.
     * For example, if this field is a set of Object then this
     * field is set to 'Object'.
     * This field is computed in the 2nd pass.
     */
    public JType type;
    
    /**
     * Set of {@link FieldItem}s that shares the same name.
     * This field is computed in the 1st pass.
     */
    public final Set items = new java.util.HashSet();
    
    
    
    
    protected FieldUse( String name, ClassItem _owner ) {
        this.name = name;
        this.owner = _owner;
        this.codeModel = owner.owner.codeModel;
    }

    public final JCodeModel getCodeModel() {
        return owner.owner.codeModel;
    }
    
    
    
    /**
     * Computes the realization property of this field.
     */
    public FieldRendererFactory getRealization() {
        // use the first one found.
        // TODO: I believe some error handling is necessary when
        // two FieldItems specify contradictory rendererFactories.
        Iterator itr = items.iterator();
        while(itr.hasNext()) {
            FieldRendererFactory frf = ((FieldItem)itr.next()).realization;
            if( frf!=null )    return frf;
        }
        return null;
    }
    
    
    /**
     * Computes the default value of this field.
     * 
     * @return null
     *      If no default value is present.
     */
    public DefaultValue[] getDefaultValues() {
        // use the first one found.
        // TODO: I believe some error handling is necessary when
        // two FieldItems specify default values at the same time.
        Iterator itr = items.iterator();
        while(itr.hasNext()) {
            DefaultValue[] dv = ((FieldItem)itr.next()).defaultValues;
            if( dv!=null ) return dv;
        }
        return null;
    }
    
    /**
     * Compute the javadoc for this field.
     * 
     * @return
     *      always return non-null String (could be empty)
     */
    public String getJavadoc() {
        StringBuffer buf = new StringBuffer();
        FieldItem[] items = getItems();
        for( int i=0; i<items.length; i++ )
            if(items[i].javadoc!=null) {
                if(i!=0)    buf.append("\n\n");
                buf.append(items[i].javadoc);
            }
        return buf.toString();
    }

    
    /**
     * Returns false if this field is not unboxable.
     * 
     * This function checks whether the aggregated FieldItems
     * contain the special null type. If so, we can't use unboxed
     * types.
     */
    public boolean isUnboxable() {
        FieldItem[] items = getItems();
        for( int i=0; i<items.length; i++ )
            if(!items[i].isUnboxable(codeModel))
                return false;
        
        return true;
    }
    
    /**
     * Check if the delegation is turned on for this field.
     * 
     * @see FieldItem#isDelegated()
     */
    public boolean isDelegated() {
        FieldItem[] items = getItems();
        for( int i=0; i<items.length; i++ )
            if(items[i].isDelegated())
                return true;
        return false;
    }
    
    /**
     * Turns off the delegation for this field.
     */
    public void disableDelegation() {
        FieldItem[] items = getItems();
        for( int i=0; i<items.length; i++ )
            items[i].setDelegation(false);
    }
    
    
    
    
    
    public FieldItem[] getItems() {
        return (FieldItem[])items.toArray(new FieldItem[0]);
    }
    
    /**
     * Total multiplicity from the parent class to items of this field.
     * This field is computed in the 2nd pass.
     */
    public Multiplicity multiplicity;
}
