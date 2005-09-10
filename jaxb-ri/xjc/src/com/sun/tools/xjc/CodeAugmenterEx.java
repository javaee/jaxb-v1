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
