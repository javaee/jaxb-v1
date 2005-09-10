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
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import batch.performance.Profiler;
import batch.performance.visualizer.DataSetBuilder;

/**
 * Results of one test.
 * 
 * <p>
 * Equality is based on the key values (testSpecFile,scenario,profiler,instance).
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class DataSeries {
    /**
     * Gets the location of testspec.meta.
     */
    public final URL testSpec;
    
    /**
     * Measurement scenario name.
     */
    public final ScenarioResult scenario;
    
    /**
     * Profiler that was used.
     */
    public final Profiler profiler;
    
    /**
     * Instance document that was measured, if any, or null.
     */
    public final URL instance;
    
    /**
     * Date ({@link String}) to its measured value ({@link BigInteger}).
     */
    final Map dataPoints = new TreeMap();
    
    
    DataSeries(URL testSpec, ScenarioResult scenario, Profiler profiler, URL instance) {
        this.testSpec = testSpec;
        this.scenario = scenario;
        this.profiler = profiler;
        this.instance = instance;
    }
    
    public void addDataPoint( String date, BigInteger value ) {
        dataPoints.put(date,value);
    }
    
    public boolean equals( Object o ) {
        DataSeries rhs = (DataSeries)o;
        
        if( profiler!=rhs.profiler )    return false;
        if( !scenario.equals(rhs.scenario) )    return false;
        if( !testSpec.equals(rhs.testSpec) )    return false;
        
        if( instance==null && rhs.instance==null )  return true;
        if( instance==null || rhs.instance==null )  return false;
        return instance.equals(rhs.instance);
    }
    
    public int hashCode() {
        int h = profiler.hashCode()^scenario.hashCode()^testSpec.hashCode();
        if( instance!=null )    h ^= instance.hashCode();
        
        return h;
    }

    /**
     * Adds all the data points to the given data set for charting.
     */
    public void addTo( DataSetBuilder dataset) {
        BigInteger sum = sum();
        BigInteger factor = scale.multiply(BigInteger.valueOf(dataPoints.size()));
        
        for( Iterator itr=dataPoints.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry e = (Map.Entry)itr.next();
            
            dataset.add(
                ((BigInteger)e.getValue()).multiply(factor).divide(sum),
                (instance!=null)?instance.toString():testSpec.toString(),
                (Comparable)e.getKey() );
        }
    }
    
    /**
     * Sum of all the values.
     */
    BigInteger sum() {
        BigInteger r = BigInteger.ZERO;
        for( Iterator itr=dataPoints.values().iterator(); itr.hasNext(); ) {
            BigInteger value = (BigInteger)itr.next();
            r = r.add(value);
        }
        return r;
    }
    
    private static final BigInteger scale = new BigInteger("100");
}
