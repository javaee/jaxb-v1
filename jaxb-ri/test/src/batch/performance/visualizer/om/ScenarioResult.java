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

