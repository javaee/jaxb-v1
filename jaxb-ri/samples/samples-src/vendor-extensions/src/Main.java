/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
import java.io.*;
import java.util.*;

import javax.xml.bind.*;

// import java content classes generated by binding compiler
import primer.myPo.*;

/*
 * $Id: Main.java,v 1.1 2004-06-25 21:12:45 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
public class Main {
    
    // This sample application demonstrates how to modify a java content
    // tree and marshal it back to a xml data. This example demonstrates
    // customiation within the schema file, po.xsd, and the impact that these 
    // customizations have on the schema derived Java representation.
    
/*
      XML --> Unmarshal -->Serialize
       |                        |
       ?=                       |
       |                        v
      XML <-- Marshal <--Deserialize
*/

    public static void main( String[] args ) {
        final String INPUT_XML_FILE="poInput.xml";
	final String SERIALIZE_FILE="po.ser";
        final String DESERIALIZED_XML="poMarshalled.xml";

        try {
            JAXBContext jc = JAXBContext.newInstance("primer.myPo");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT ,
                   new Boolean(true));

            unmarshaller.setValidating(true);

            Validator validator = jc.createValidator();

            System.out.println( "unmarshalling from \"" + INPUT_XML_FILE + "\"..." );
            primer.myPo.PurchaseOrderType po=(primer.myPo.PurchaseOrderType)
                   unmarshaller.unmarshal(new File(INPUT_XML_FILE));
	    System.out.println("Demo superclass override of toString() method for all schema-derived JAXB classes purchaseOrderType.toString()=" + po.toString());

            System.out.println( "serializing content tree to \"" + SERIALIZE_FILE + "\"..." );
            FileOutputStream out = new FileOutputStream(SERIALIZE_FILE);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(po);
            objOut.flush();
	    out.close();

            System.out.println( "deserializing content tree from \"" + SERIALIZE_FILE + "\"..." );
            FileInputStream in = new FileInputStream(SERIALIZE_FILE);
            ObjectInputStream objIn = new ObjectInputStream(in);
            po=(primer.myPo.PurchaseOrderType)objIn.readObject();

            System.out.println( "marshalling to \"" + DESERIALIZED_XML + "\"..." );
            FileOutputStream mout = 
		new FileOutputStream(DESERIALIZED_XML);
            marshaller.marshal(po, mout);
	    in.close();
	    mout.close();

            System.out.println( "test complete." );
        } catch( JAXBException je ) {
            je.printStackTrace();
        } catch( IOException ioe ) {
            ioe.printStackTrace();
        } catch ( ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }
}

