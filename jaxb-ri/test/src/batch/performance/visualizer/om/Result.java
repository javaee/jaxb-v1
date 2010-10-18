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
