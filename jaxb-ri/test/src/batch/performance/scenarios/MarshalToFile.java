/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package batch.performance.scenarios;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import batch.core.om.Instance;
import batch.performance.PerformanceTestDescriptor;
import batch.performance.ScenarioException;

public class MarshalToFile extends AbstractScenarioImpl {

    private JAXBContext context;
    private Marshaller marshaller;
    private Object u;
    private FileOutputStream out;
    private File tmp;
    private String tmpname = "tmp.out";

    public String getName() {
        return "MarshalToFile";
    }

    public void run() throws ScenarioException {
        try {
            for (int i = 0; i < 100; i++) {
                marshaller.marshal(u, out);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void prepare(
        PerformanceTestDescriptor descriptor,
        ClassLoader classLoader,
        Instance instance)
        throws ScenarioException {
        super.prepare(descriptor, classLoader, instance);
        try {
            context =
                JAXBContext.newInstance(
                                        descriptor.schema.targetPackageName,
                                        classLoader);
            marshaller = context.createMarshaller();
            u = instance.unmarshal(context);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        try {
            tmp = new File(descriptor.outDir, tmpname);
            out = new FileOutputStream(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public void teardown() throws ScenarioException {
        super.teardown();
        context = null;
        marshaller = null;
        try {
            out.close();
            tmp.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
