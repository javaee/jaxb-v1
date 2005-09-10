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
package com.sun.tools.xjc.reader.xmlschema.parser;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.sun.tools.xjc.reader.Const;
import com.sun.xml.bind.JAXBAssertionError;

/**
 * This filter detects W3C XML Schema features that are not supported
 * by the JAXB RI.
 * 
 * @author <ul><li>Ryan Shoemaker, Sun Microsystems, Inc.</li></ul>
 * @version $Revision: 1.2 $
 * @since JAXB1.0
 */
public class ProhibitedFeaturesFilter extends XMLFilterImpl {

    private Locator locator = null;
    
    // as prohibited XML schema features are detected, warnings will be
    // delivered to this event handler.    
    private ErrorHandler errorHandler = null;
    
    // in strict mode, the compiler must report errors for XSchema features 
    // listed in appendix E.2 - in non-strict mode, some of these features
    // can be safely ignored and only generate warnings
    private boolean strict = true;
    
    // error type codes 
    // for schema features that are errors in strict mode but OK in the extension mode
    private static final int REPORT_DISABLED_IN_STRICT_MODE = 1;
    // for schema features that are errors in strict mode but just warnings in the extension mode
    private static final int REPORT_RESTRICTED = 2;
    // for schema features that are warnings regardless of mode
    private static final int REPORT_WARN = 3;
    // for schema features that simply aren't supported regardless of mode
    private static final int REPORT_UNSUPPORTED_ERROR = 4;
    
    public ProhibitedFeaturesFilter( ErrorHandler eh, boolean strict ) {
        errorHandler = eh;
        this.strict = strict;
    }


    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement( String uri, String localName, 
                              String qName, Attributes atts )
        throws SAXException {
                    
        if( strict && localName.equals( "any" ) && "skip".equals(atts.getValue( "processContents" )) ) {
            // no support for processContents="skip", some support for "lax" & "strict"
            // BugID: 4740924
            //    processContents="skip" is treated as "lax". So we issue a warning
            report( REPORT_WARN, Messages.PROCESSCONTENTS_ATTR_OF_ANY, locator ); 
        } else
        if( localName.equals( "anyAttribute" ) ) {
            // if strict then ERROR, else WARN
            report( REPORT_RESTRICTED, 
                    strict == true ? Messages.ANY_ATTR : Messages.ANY_ATTR_WARNING, 
                    locator );
        } else if( localName.equals( "complexType" ) ) {
            /*
                abstract complex type is an error only when it is referenced
                from element declarations. Thus this check cannot be done in
                this class.
                
                Instead, the check is implemented in BGMBuilder. See
                the BGMBuilder.checkAbstractComplexType method.
            */
//            // reject if contains abstract attribute
//            if( atts.getValue( "abstract" ) != null ) {
//                reject( "\"abstract\" attribute of <complexType> is not supported", locator ); 
//            }

            // if block != #all, then WARN
            if( ( atts.getValue( "block" ) != null ) &&
                ( !parseComplexTypeBlockAttr( atts.getValue( "block" ) ) ) ) {
                report( REPORT_WARN, 
                        Messages.BLOCK_ATTR_OF_COMPLEXTYPE, 
                        locator );
            }
            
            // always WARN
            if( atts.getValue( "final" ) != null ) {
                report( REPORT_WARN,
                        Messages.FINAL_ATTR_OF_COMPLEXTYPE,
                        locator );
            }
        } else if( localName.equals( "element" ) ) {
            // reject if contains abstract, substitutionGroup attributes
            // always an ERROR
            if( ( atts.getValue( "abstract" ) != null ) && 
                ( parsedBooleanTrue( atts.getValue( "abstract" ) ) ) ) {
                // allow <element name="foo" abstract="false">
                report( REPORT_DISABLED_IN_STRICT_MODE, Messages.ABSTRACT_ATTR_OR_ELEMENT, 
                        locator ); 
            }
            if( ( atts.getValue( "substitutionGroup" ) != null ) && 
                ( !atts.getValue( "substitutionGroup" ).trim().equals( "" ) ) ) {
                // allow <element ... substitutionGroup="">
                report( REPORT_DISABLED_IN_STRICT_MODE,
                        Messages.SUBSTITUTIONGROUP_ATTR_OF_ELEMENT, 
                        locator ); 
            }
            
            // always WARN
            if( atts.getValue( "final" ) != null ) {
                report( REPORT_WARN,
                        Messages.FINAL_ATTR_OF_ELEMENT,
                        locator );
            }
            
            // if block != #all, then WARN
            if( ( atts.getValue( "block" ) != null ) &&
                ( !parseElementBlockAttr( atts.getValue( "block" ) ) ) ) {
                report( REPORT_WARN, 
                        Messages.BLOCK_ATTR_OF_ELEMENT, 
                        locator );
            }
        } else if( localName.equals( "key" ) ) {
            // if strict == true, then ERROR, else WARN
            report( REPORT_RESTRICTED, 
                    strict == true ? Messages.KEY : Messages.KEY_WARNING , 
                    locator );
        } else if( localName.equals( "keyref" ) ) {
            // if strict == true, then ERROR, else WARN
            report( REPORT_RESTRICTED, 
                    strict == true ? Messages.KEYREF : Messages.KEYREF_WARNING, 
                    locator );
        } else if( localName.equals( "notation" ) ) {
            // if strict == true, then ERROR, else WARN
            report( REPORT_RESTRICTED, 
                    strict == true ? Messages.NOTATION : Messages.NOTATION_WARNING, 
                    locator );
        } else if( localName.equals( "unique" ) ) {
            // if strict == true, then ERROR, else WARN
            report( REPORT_RESTRICTED, 
                    strict == true ? Messages.UNIQUE : Messages.UNIQUE_WARNING, 
                    locator );
        } else if( localName.equals( "redefine" ) ) {
            // even though this feature is supported in the compiler,
            // we have to turn it off.
            report( REPORT_UNSUPPORTED_ERROR, 
                    Messages.REDEFINE, 
                    locator ); 
        } else if( localName.equals( "schema" ) ) {
            // always WARN
            if( ( atts.getValue( "blockDefault" ) != null ) && 
                ( !atts.getValue( "blockDefault" ).equals( "#all" ) ) ){
                report( REPORT_WARN,
                        Messages.BLOCKDEFAULT_ATTR_OF_SCHEMA,
                        locator );
            }

            // always WARN
            if( atts.getValue( "finalDefault" ) != null ) {
                report( REPORT_WARN,
                        Messages.FINALDEFAULT_ATTR_OF_SCHEMA,
                        locator );
            }
            
            if( atts.getValue(Const.JAXB_NSURI,"extensionBindingPrefixes") != null ) {
                report( REPORT_DISABLED_IN_STRICT_MODE,
                        Messages.EXTENSIONBINDINGPREFIXES_OF_SCHEMA,
                        locator );
            }
        }
        // <field> and <selector> should be ignored, but since they can only 
        // be contained within <key>, <keyref>, and <unique> which are already 
        // ignored, there is no need to have special code to detect them.  It
        // would generate unnecessary warnings anyway...
        
        // pass content on to the next handler
        super.startElement( uri, localName, qName, atts );
    }


    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator( Locator locator ) {
        super.setDocumentLocator( locator );
        this.locator = locator;
    }


    /**
     * send an error message to the ErrorHandler and throw the
     * SAXException to halt the parser.
     */    
    private void report( int type, String msg, Locator loc ) 
        throws SAXException {
            
        SAXParseException spe = null;
        
        if( type==REPORT_RESTRICTED && !strict )
            type = REPORT_WARN;
        if( type==REPORT_DISABLED_IN_STRICT_MODE && !strict )
            return; // no error
                                 
        // an overly complicated switch to determine the most appropriate
        // prefix for the error/warning message.  There are two classes of
        // prohibited features: unsupported and error/warning.  The unsupported
        // features always cause an error.  The error/warning features cause an
        // error when the compiler is running in strict mode and a warning when
        // running in extension mode.                   
        switch( type ) {
            case REPORT_RESTRICTED:
            case REPORT_DISABLED_IN_STRICT_MODE:
                spe = new SAXParseException( 
                    Messages.format( Messages.STRICT_MODE_PREFIX ) + "\n\t" + Messages.format( msg ), 
                    loc ); 
                errorHandler.error( spe );
                throw spe;
            case REPORT_WARN:
                spe = new SAXParseException( 
                    Messages.format( Messages.WARNING_PREFIX ) + " " + Messages.format( msg ), 
                    loc ); 
                errorHandler.warning( spe );
                break;
            case REPORT_UNSUPPORTED_ERROR:
                spe = new SAXParseException( 
                    Messages.format( Messages.UNSUPPORTED_PREFIX ) + " " + Messages.format( msg ), 
                    loc ); 
                errorHandler.error( spe );
                throw spe;
            default:
                throw new JAXBAssertionError();
        }
    }
//
//    /**
//     * debug messages
//     */
//    private static boolean debug = Util.getSystemProperty("jaxb.debug")!=null;
    
    /**
     * return true iff the lexical boolean is "true" or "1".
     */
    private static boolean parsedBooleanTrue( String lexicalBoolean ) 
        throws SAXParseException {
        if( lexicalBoolean.equals( "true" ) || lexicalBoolean.equals( "1" ) ) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Parse the lexical value of <element> block attribute.  
     * 
     * @return true iff lexicalBlock == "#all" or lexicalBlock contains
     * all three strings: "restriction", "extension", and "substitution"
     */
    private static boolean parseElementBlockAttr( String lexicalBlock ) {
        if( lexicalBlock.equals( "#all" ) ||
             ( ( lexicalBlock.indexOf( "restriction" ) != -1 ) &&
               ( lexicalBlock.indexOf( "extension" ) != -1 ) &&
               ( lexicalBlock.indexOf( "substitution" ) != -1 ) ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parse the lexical value of <complexType> block attribute.  
     * 
     * @return true iff lexicalBlock == "#all" or lexicalBlock contains
     * both strings: "restriction", and "extension"
     */
    private static boolean parseComplexTypeBlockAttr( String lexicalBlock ) {
        if( lexicalBlock.equals( "#all" ) ||
             ( ( lexicalBlock.indexOf( "restriction" ) != -1 ) &&
               ( lexicalBlock.indexOf( "extension" ) != -1 ) ) ) {
            return true;
        } else {
            return false;
        }
    }

}
