/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.xjc;

import java.util.List;

import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIXPluginCustomization;

/**
 * Extended add-on interface capable of binding customizations.
 * 
 * @author
 *     Aleksei Valikov (valikov@gmx.net)
 */
public interface CodeAugmenterEx extends CodeAugmenter {
  
  /**
   * Returns the list of namespace URIs that are supported by this add-on
   * as schema annotations.
   *
   * <p>If a plug-in returns a non-empty list, the JAXB RI will recognize
   * these namespace URIs as vendor extensions
   * (much like "http://java.sun.com/xml/ns/jaxb/xjc"). This allows users
   * to write those annotations inside a schema, or in external binding files,
   * and later plug-ins can access those annotations as DOM nodes.</p>
   *
   * @return List of strings. Can be empty, be never be null.
   */
  public List getCustomizationURIs();

}
