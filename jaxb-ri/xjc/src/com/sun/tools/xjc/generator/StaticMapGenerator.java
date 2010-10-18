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
