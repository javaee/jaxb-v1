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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import message.*;

/**
 * Test client.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class TestClient implements Runnable {

    private ObjectFactory of;
    private Marshaller marshaller;

    public TestClient() {
        try {
            of = new ObjectFactory();
            marshaller = of.createMarshaller();
        } catch( JAXBException e ) {
            e.printStackTrace(); // impossible
        }
    }
    
    public void run() {
        try {
            // create a socket connection and multiplex it
            Socket socket = new Socket("localhost",38247);
            OutputStreamMultiplexer osm = new OutputStreamMultiplexer(socket.getOutputStream());
            
            sendMessage(osm,"1st message");
            sendMessage(osm,"2nd message");
            
            osm.close();
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private void sendMessage( OutputStreamMultiplexer osm, String msg ) throws JAXBException, IOException {
        Message m = of.createMessage();
        m.setValue(msg);
        
        OutputStream sub = osm.openSubStream();
        marshaller.marshal(m,sub);
        sub.close();
    }
}
