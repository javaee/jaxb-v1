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

import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import contact.Contact;

/*
 * Use is subject to the license terms.
 */

/**
 * 
 * 
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        
        String nameToLookFor = args[0];
        
        JAXBContext jaxbContext = JAXBContext.newInstance("contact"); 
        
        // set up a parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        // create an unmarshaller
        PullUnmarshaller u = new PullUnmarshaller(jaxbContext);

        xpp.setInput(new FileInputStream("contact.xml"), null);
        xpp.nextTag(); // move to the root element
        xpp.require(XmlPullParser.START_TAG, "", "addressBook");
        // check the root tag name
        xpp.nextTag(); // move to the first <contact> element.

        while (xpp.getEventType() == XmlPullParser.START_TAG) {
            // unmarshall one <contact> element into a JAXB Contact object
            Contact contact = (Contact) u.unmarshalSubTree(xpp);
            
            if( contact.getName().equals(nameToLookFor)) {
                // we found what we wanted to find. show it and quit now.
                System.out.println("the e-mail address is "+contact.getEmail());
                return;
            }
            

            if (xpp.getEventType() == XmlPullParser.TEXT)
                xpp.next(); // skip the whitespace between <contact>s.
        }

        System.out.println("Unable to find "+nameToLookFor);
    }
}
