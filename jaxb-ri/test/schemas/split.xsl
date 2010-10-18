<?xml version="1.0"?>
<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at packager/legal/LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

-->

<!--
  A stylesheet to split schema suite files into individual test files
  plus a metadata file.
  
  $Id: split.xsl,v 1.3 2010-10-18 14:22:16 snajper Exp $
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:redirect="xalan://splitter.Redirect"
  extension-element-prefixes="redirect"
  exclude-result-prefixes="redirect">

<xsl:output method="xml"/>
<xsl:param name="destdir"/>

<xsl:template match="/">
  <xsl:apply-templates select="//testCase"/>
</xsl:template>

<xsl:template match="testCase">
  <!-- base directory of the .ssuite file -->
  <!--xsl:param name="dir" select="u:getBaseDir()"
    xmlns:u="xalan://splitter.SplitterUtil" /-->
  <xsl:param name="dir" select="concat($destdir,'/',u:getBaseDir())"
    xmlns:u="xalan://splitter.SplitterUtil" />

  <!-- determines the directory to place files of this test case -->
  <xsl:variable name="b" select="concat($dir, '/', title)"/>
  <!-- set the ext variable to the correct extension for the schema language in this test. -->
  <xsl:variable name="ext">
    <xsl:choose>
      <xsl:when test="schema/@language='http://www.w3.org/2001/XMLSchema'">
        <xsl:text>xsd</xsl:text>
      </xsl:when>
      <xsl:when test="schema/@language='http://relaxng.org/ns/structure/1.0'">
        <xsl:text>rng</xsl:text>
      </xsl:when>
      <xsl:when test="schema/@language='DTD'">
        <xsl:text>dtd</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          unknown schema language
          <xsl:value-of select="schema/@language"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <!-- compute the schema file name -->
  <xsl:variable name="f" select="concat('test.', $ext)"/>
  
  <!-- output the schema file -->
  <xsl:choose>
    <xsl:when test="$ext='dtd'">
      <redirect:write file="{concat($b, '/', $f)}" method="text">
        <xsl:copy-of select="schema/node()"/>
      </redirect:write>
    </xsl:when>
    <xsl:otherwise>
      <redirect:write file="{concat($b, '/', $f)}">
        <xsl:copy-of select="schema/*"/>
      </redirect:write>
    </xsl:otherwise>
  </xsl:choose>
  
  <!-- output resource files -->
  <xsl:for-each select="resource">
    <redirect:write file="{concat($b, '/', @name)}">
      <xsl:copy-of select="node()"/>
    </redirect:write>
  </xsl:for-each>
  
  <!-- output binding files -->
  <xsl:for-each select="bindings">
    <xsl:variable name="fileName">
      <xsl:choose>
        <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
        <xsl:otherwise><xsl:text>test.jaxb</xsl:text></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <redirect:write file="{concat($b, '/', $fileName)}">
      <xsl:copy-of select="node()"/>
    </redirect:write>
  </xsl:for-each>
  
  <!-- produce meta file -->
  <redirect:write select="concat($b,'/testspec.meta')" indent="yes">
    <testCase>
      <!-- @since -->
      <xsl:if test="ancestor-or-self::*/@since">
        <xsl:copy-of select="ancestor-or-self::*/@since" />
      </xsl:if>
      
      <!-- @until -->
      <xsl:if test="ancestor-or-self::*/@until">
        <xsl:copy-of select="ancestor-or-self::*/@until" />
      </xsl:if>
      
      <!-- @excludeFrom -->
      <xsl:if test="ancestor-or-self::*/@excludeFrom">
        <xsl:copy-of select="ancestor-or-self::*/@excludeFrom" />
      </xsl:if>
      
      <!-- target package -->
      <package>
        <xsl:text>test.</xsl:text>
        <xsl:value-of select="/testSuite/package/text()"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="title/text()"/>
      </package>
      
      <!-- schema -->
      <schema href="{$f}" language="{$ext}">
        <xsl:copy-of select="schema/@*"/>
      </schema>
      
      <!-- external binding files -->
      <xsl:for-each select="bindings">
        <xsl:variable name="fileName">
          <xsl:choose>
            <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
            <xsl:otherwise><xsl:text>test.jaxb</xsl:text></xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <bindings href="{$fileName}"/>
      </xsl:for-each>
      
      
      <!-- test instances -->
      <xsl:for-each select="instance">
        <instance href="{position()}.xml">
          <xsl:for-each select="*">
            <xsl:if test="local-name(.)!='document'">
              <property name="{local-name(.)}">
              	<value><xsl:value-of select="text()" /></value>
              </property>
            </xsl:if>
          </xsl:for-each>
        </instance>
      </xsl:for-each>
      
      <!-- test scripts -->
      <xsl:for-each select="script">
        <script href="{position()}.js">
          <xsl:if test="@run">
            <xsl:copy-of select="@run"/>
          </xsl:if>
          <xsl:copy-of select="*" /><!-- copy any element -->
        </script>
      </xsl:for-each>
      
      <!-- performance -->
      <xsl:copy-of select="performance" />
    </testCase>
  </redirect:write>
  
  
  <!-- produce test instances -->
  <!-- Don't combine with others so that position() returns correct value. -->
  <xsl:apply-templates select="instance">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
  
  <!-- produce test scripts -->
  <!-- Don't combine with others so that position() returns correct value. -->
  <xsl:apply-templates select="script">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
  
</xsl:template>

<!--
  produce a test instance document
-->
<xsl:template match="instance">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', position(), '.xml')"/>
  <redirect:write file="{$d}">
    <xsl:copy-of select="document/node()"/>
  </redirect:write>
</xsl:template>

<!--
  produce a test script
-->
<xsl:template match="script">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', position(), '.js')"/>
  <redirect:write file="{$d}" method="text">
    <xsl:copy-of select="node()"/>
  </redirect:write>
</xsl:template>

</xsl:stylesheet>
