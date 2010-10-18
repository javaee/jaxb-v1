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

package batch.performance.visualizer.om;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import batch.performance.visualizer.DataSetBuilder;

/**
 * Result collection per scenario.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class ScenarioResult {
    public final String name;
    
    private final Set dataSeries = new HashSet();
    
    ScenarioResult( String _name ) {
        if( _name==null )   throw new NullPointerException();
        this.name = _name;
    }
    
    void addDataSeries( DataSeries ds ) {
        dataSeries.add(ds);
    }
    
    /**
     * Returns all the data series in this scenario.
     */
    public Iterator dataSeries() {
        return dataSeries.iterator();
    }

    /**
     * Adds all the data points to the given data set for charting.
     */
    public void addTo( DataSetBuilder dataset) {
        DataSeries[] ds = (DataSeries[]) dataSeries.toArray(new DataSeries[dataSeries.size()]);
        BigInteger[] sum = new BigInteger[ds.length];
        
        for( int i=0; i<ds.length; i++ )
            sum[i] = ds[i].sum();
        
        Set keys = new TreeSet();
        for( int i=0; i<ds.length; i++ )
            keys.addAll(ds[i].dataPoints.keySet());
        
        for( Iterator itr=keys.iterator(); itr.hasNext(); ) {
            String key = (String)itr.next();
            
            // r = \sum_i index_i
            int count = 0;
            BigInteger r = BigInteger.ZERO;
            for( int j=0; j<ds.length; j++ ) {
                BigInteger value = (BigInteger)ds[j].dataPoints.get(key);
                if( value!=null ) {
                    // index_i = value_{ij} / \sum_j value_{ij}
                    r = r.add(value.multiply(BigInteger.valueOf(ds[j].dataPoints.size())).multiply(scale).divide(sum[j]));
                    count++;
                }
            }
            
            r = r.divide(BigInteger.valueOf(count));
            
            dataset.add( r, name, key );
        }
    }
    
    public String toString() {
        return name;
    }
    
    private static final BigInteger scale = new BigInteger("100");
}

