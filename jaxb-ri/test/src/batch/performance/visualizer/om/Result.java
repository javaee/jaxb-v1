/*
 * @(#)$Id: Result.java,v 1.1 2004-06-25 21:13:08 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance.visualizer.om;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.Element;

import batch.performance.Profiler;

/**
 * Results of the performance test.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class Result {
    /**
     * Map from {@link DataSeries} to itself.
     */
    private final Map dataSeries = new HashMap();
    
    /**
     * Map from scenario name ({@link String}) to {@link ScenarioResult}.
     */
    private final Map scenarios = new TreeMap();
    
    public Result(Document dom) throws IOException {
        parse(dom);
    }

    DataSeries createDataSeries(URL testSpec, String scenario, Profiler profiler, URL instance) {
        ScenarioResult sr = getScenarioResult(scenario);
        
        DataSeries key = new DataSeries(testSpec,sr,profiler,instance);
        DataSeries value = (DataSeries)dataSeries.get(key);
        if(value==null) {
            value = key;
            dataSeries.put(value,value);
            sr.addDataSeries(value);
        }
        return value;
    }

    public DataSeries getDataSeries(URL testSpec, String scenario, Profiler profiler, URL instance) {
        DataSeries key = new DataSeries(
                testSpec,
                getScenarioResult(scenario),
                profiler,instance);
        return (DataSeries)dataSeries.get(key);
    }
    
    /**
     * Gets or creates {@link ScenarioResult}.
     */
    ScenarioResult getScenarioResult( String name ) {
        ScenarioResult sr = (ScenarioResult)scenarios.get(name);
        if(sr==null)
            scenarios.put(name,sr=new ScenarioResult(name));
        return sr;
    }
    
    
    public void parse( Document dom ) throws IOException {
        List runs = dom.getRootElement().elements("run");
        for (Iterator itr = runs.iterator(); itr.hasNext();) {
            Element run = (Element)itr.next();
            final String date = run.attributeValue("date");
            
            List groups = run.elements("group");
            for( Iterator jtr=groups.iterator(); jtr.hasNext(); ) {
            	Element group = (Element)jtr.next();
            	final URL testSpec = new URL(group.attributeValue("name"));
                
                List results = group.elements("result");
                for( Iterator ktr=results.iterator(); ktr.hasNext(); ) {
                    Element result = (Element)ktr.next();
                    
                    URL instance = null;
                    if( result.attribute("instance")!=null )
                        instance = new URL(result.attributeValue("instance"));
                    
                    DataSeries ds = createDataSeries(
                            testSpec,
                            result.attributeValue("scenario"),
                            result.attributeValue("mode").equals("speed")?Profiler.SPEED:Profiler.MEMORY,
                            instance );
                    try {
                        ds.addDataPoint(date,new BigInteger(result.getTextTrim()));
                    } catch( NumberFormatException e ) {
                        ; // throw away this data point
                    }
                }
            }
        }
    }
    
    /**
     * Enumerates all {@link ScenarioResult}s in this result.
     */
    public Iterator scenarios() {
        return scenarios.values().iterator();
    }
}
