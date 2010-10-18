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
