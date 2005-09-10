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

/*
 * Use is subject to the license terms.
 */
package com.sun.tools.xjc.generator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.util.Util;

/**
 * Generates code that fills in a static map.
 * 
 * This class hides the detail of filling a large table
 * while avoiding JVM limitation.
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public abstract class StaticMapGenerator
{
    /**
     * Map object.
     */
    public final JVar $map;
             
    /**
     * The invocations of <code>HashMap.put</code> method will be
     * put into this block.
     */
    private JBlock block;
    
    /**
     * Number of the put invocations in the current block.
     */
    private int cnt;
    
    /**
     * Sequence number generator.
     */
    private int ticketMaster = 1;
    
    /**
     * How many invocations of the put method do we generate in one method.
     */
    private final int THRESHOLD;
    
    protected StaticMapGenerator( JVar $map, JBlock block ) {
        this.$map = $map; 
        this.block = block;

        String debug = Util.getSystemProperty(ObjectFactoryGenerator.class,"staticThreshold");
        if(debug==null) THRESHOLD=500;
        else            THRESHOLD=Integer.parseInt(debug);
    }
    
    /**
     * Generates an association.
     */
    protected final void add( JExpression key, JExpression value ) {
        block.invoke( $map, "put" )
            .arg( key )
            .arg( value );        
            
        if( ++cnt >= THRESHOLD ) {
            // generate a next method
            JMethod m = createNewMethod(ticketMaster++);
            block.invoke(m);
            block = m.body();
            cnt = 0;
        }
    }
    
    /**
     * This method will be called when a new method needs to be created
     * to avoid the JDK limitation that restricts one method size to be <64K.
     * @param uniqueId
     *         unique id. Useful to generate unique method names.
     */
    protected abstract JMethod createNewMethod( int uniqueId );
}
