/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// import java content classes generated by binding compiler
import primer.po.*;

/*
 * $Id: Main.java,v 1.1 2004-06-25 21:11:53 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
 
public class Main {
    
    // This sample application demonstrates how to use the ObjectFactory
    // class to create a java content tree from scratch and marshal it
    // to XML data
    
    public static void main( String[] args ) {
        try {
            // create a JAXBContext
            JAXBContext jc = JAXBContext.newInstance( "primer.po" );
            
            // create an ObjectFactory instance.
            // if the JAXBContext had been created with mutiple pacakge names,
            // we would have to explicitly use the correct package name when
            // creating the ObjectFactory.            
            ObjectFactory objFactory = new ObjectFactory();
            
            // create an empty PurchaseOrder
            PurchaseOrder po = objFactory.createPurchaseOrder();
            
            // set the required orderDate attribute
            po.setOrderDate( Calendar.getInstance() );
            
            // create shipTo USAddress object
            USAddress shipTo = createUSAddress( objFactory,
                                                "Alice Smith",
                                                "123 Maple Street",
                                                "Cambridge",
                                                "MA",
                                                "12345" );
                                                
            // set the required shipTo address 
            po.setShipTo( shipTo );
            
            // create billTo USAddress object
            USAddress billTo = createUSAddress( objFactory,
                                                "Robert Smith",
                                                "8 Oak Avenue",
                                                "Cambridge",
                                                "MA",
                                                "12345" );
            
            // set the requred billTo address
            po.setBillTo( billTo );
                                                
            // create an empty Items object
            Items items = objFactory.createItems();
            
            // get a reference to the ItemType list
            List itemList = items.getItem();
            
            // start adding ItemType objects into it
            itemList.add( createItemType( objFactory,
                                          "Nosferatu - Special Edition (1929)", 
                                          new BigInteger( "5" ), 
                                          new BigDecimal( "19.99" ), 
                                          null,
                                          null,
                                          "242-NO" ) );
            itemList.add( createItemType( objFactory,
                                          "The Mummy (1959)", 
                                          new BigInteger( "3" ), 
                                          new BigDecimal( "19.98" ), 
                                          null,
                                          null,
                                          "242-MU" ) );
            itemList.add( createItemType( objFactory,
                                          "Godzilla and Mothra: Battle for Earth/Godzilla vs. King Ghidora", 
                                          new BigInteger( "3" ), 
                                          new BigDecimal( "27.95" ), 
                                          null,
                                          null,
                                          "242-GZ" ) );
            
            // set the required Items list
            po.setItems( items );
            
            // create a Marshaller and marshal to System.out
            Marshaller m = jc.createMarshaller();
            m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            m.marshal( po, System.out );
            
        } catch( JAXBException je ) {
            je.printStackTrace();
        } 
    }
    
    public static USAddress createUSAddress( ObjectFactory objFactory,
                                               String name, String street,
                                               String city, String state,
                                               String zip ) 
        throws JAXBException {
    
        // create an empty USAddress objects                                             
        USAddress address = objFactory.createUSAddress();
        
        // set properties on it
        address.setName( name );
        address.setStreet( street );
        address.setCity( city );
        address.setState( state );
        address.setZip( new BigDecimal( zip ) );
        
        // return it
        return address;
    }
    
    public static Items.ItemType createItemType( ObjectFactory objFactory,
                                                 String productName,
                                                 BigInteger quantity,
                                                 BigDecimal price,
                                                 String comment,
                                                 Calendar shipDate,
                                                 String partNum ) 
        throws JAXBException {
   
        // create an empty ItemType object
        Items.ItemType itemType = objFactory.createItemsItemType();
        
        // set properties on it
        itemType.setProductName( productName );
        itemType.setQuantity( quantity );
        itemType.setUSPrice( price );
        itemType.setComment( comment );
        itemType.setShipDate( shipDate );
        itemType.setPartNum( partNum );
        
        // return it
        return itemType;
    }
                                           
                                                 
}
