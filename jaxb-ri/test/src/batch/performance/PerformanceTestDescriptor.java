/*
 * @(#)$Id: PerformanceTestDescriptor.java,v 1.1 2004-06-25 21:13:03 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package batch.performance;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import batch.core.Util;
import batch.core.om.Instance;
import batch.core.om.Schema;
import batch.core.om.TestDescriptor;

/**
 * {@link PerformanceTestDescriptor} for performance test.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class PerformanceTestDescriptor extends TestDescriptor {
    
    public class Config {
        public final Scenario scenario;
        public final Profiler profiler;
        /**
         * If this configuration is associated with a particular
         * instance, this field will point to that instance.
         * If it is run against the schema and therefore not 
         * associated with any instance, the field is null.
         */
        public final Instance instance;
        
        public Config(Scenario _scenario, Profiler _profiler, Instance _instance) {
            this.scenario = _scenario;
            this.profiler = _profiler;
            this.instance = _instance;
        }
        
        public String toString() {
            return getName();
        }
        
        /**
         * Returns a human readable short description of the configuration.
         */
        public String getName() {
            String r = scenario.getName();
            if(instance!=null)
                r += " : " + instance.getName();
            return r;
        }
        
        /**
         * Gets the {@link PerformanceTestDescriptor} that owns this object.
         */
        public PerformanceTestDescriptor getParent() {
            return PerformanceTestDescriptor.this;
        }
    }
    
    /**
     * {@link Config}s to be run.
     */
    public final Config[] configs;
    
    public PerformanceTestDescriptor(File _outDir, Schema _schema, Instance[] _instances, Config[] _configs ) {
        super(_outDir, _schema, _instances);
        this.configs = _configs;
    }

    /**
     * @param metaFile
     * @throws Exception
     */
    public PerformanceTestDescriptor(File metaFile) throws Exception {
        super(metaFile);
        
        Element testSpec = Util.loadXML(metaFile).getRootElement();
        
        List configs = new ArrayList();
        for( Iterator itr=testSpec.elementIterator("performance"); itr.hasNext(); ) {
            Element performance = (Element)itr.next();
        	
            // load scenario
            Scenario scenario = createScenario( metaFile, performance );
            
            // load profiler
            Profiler profiler;
            if( performance.attributeValue("profiler").equals("memory") )
                profiler = Profiler.MEMORY;
            else
                profiler = Profiler.SPEED;
            
            // associated with instance?
            if( performance.attributeValue("run","---").equals("once") ) {
                // no
                configs.add( new Config(scenario,profiler,null) );
            } else {
                // yes
                for( int i=0; i<instances.length; i++ )
                    configs.add( new Config(scenario,profiler,instances[i]) );
            }
        }
        
        this.configs = (Config[]) configs.toArray(new Config[configs.size()]);

    }

    /**
     * Creates the {@link Scenario} object from the descriptor.
     * 
     * Derived classes may override this method.
     */
    protected Scenario createScenario( File baseLocation, Element performance ) throws Exception {
        String className = performance.attributeValue("scenario");
        
        if(className==null) {
            // this is a bean-shell based script.
            return new ScriptScenario(performance.element("scenario"));
        }
        
        Class clazz = Class.forName("batch.performance.scenarios."+className);
        try {
            // try the constructor with Element
            return (Scenario)clazz.getConstructor(new Class[]{Element.class})
                .newInstance(new Object[]{performance});
        } catch( NoSuchMethodException e ) {
            // then the default constructor
            return (Scenario)clazz.newInstance();
        }
    }
}
