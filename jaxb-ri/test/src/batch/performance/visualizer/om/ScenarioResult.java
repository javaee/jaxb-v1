/*
 * @(#)$Id: ScenarioResult.java,v 1.1 2004-06-25 21:13:08 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
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

