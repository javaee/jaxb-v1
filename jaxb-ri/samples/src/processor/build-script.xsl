<?xml version="1.0" encoding="us-ascii" ?>
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
 Reads sample.meta file and generate a build script.
 
 NOTE:
  XSLT recognizes "{exp}" as a special construct, and an XSLT variable
  reference is "{$var}", whereas Ant uses ${var} syntax.
  So it's quite confusing. To escape ${var}, write it as: ${{var}}.
 
 $Id: build-script.xsl,v 1.2 2010-10-18 14:21:24 snajper Exp $
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  
  <xsl:output
    encoding="UTF-8"
    indent="yes"
    cdata-section-elements="description"
  />
  
  <xsl:param name="target" select="'workspace'"/>
  
  <!-- directory structure constants -->
    <!-- directory to put .class files -->
    <xsl:variable name="dir.classes">classes</xsl:variable>
    <!-- directory to put JAXB-generated artifacts -->
    <xsl:variable name="dir.jaxb">gen-src</xsl:variable>
    <!-- javadoc directory -->
    <xsl:variable name="dir.javadoc">docs/api</xsl:variable>
  <!-- until here -->
  
  <xsl:variable name="javadoc" select="/sample/project/javadoc" />
  <xsl:variable name="hasDependencyJar" select="/sample/project/depends" />
  <xsl:variable name="hasDriver" select="/sample/project/java" />
  <xsl:variable name="hasDatatypeConverter" select="/sample/project/datatypeConverterSrc"/>
  <!-- true for resource-consuming samples -->
  <xsl:variable name="isHog" select="/sample/@hog" />
  
  <xsl:template match="/">
    <project basedir="." default="run">
      
      <description><xsl:value-of select="sample/description"/></description>
      
      <xsl:choose>
        <!-- for JWSDP release -->
        <xsl:when test="$target='JWSDP'">
          <xsl:comment>
            if you are not running from $JWSDP_HOME/jaxb/samples AND you
            are using your own version of Ant, then you need to specify
            "ant -Djwsdp.home=..."
          </xsl:comment>
      
          <property name="jwsdp.home" value="../../.." />
        </xsl:when>
        <!-- for the workspace -->
        <xsl:when test="$target='workspace'">
            <!-- no op -->
        </xsl:when>
        <!-- for the RI dist -->
        <xsl:when test="$target='RI'">
          <property name="jaxb.home" value="../.." />
        </xsl:when>
      </xsl:choose>
      
      <path id="classpath">
        <pathelement path="src" />
        <pathelement path="{$dir.classes}" />
        <xsl:if test="$hasDependencyJar">
          <!--
            if the project has more dependency jar files, assume 
            the we have the lib directory
          -->
          <xsl:comment>additional jar files for this sample</xsl:comment>
          <fileset dir="lib" includes="*.jar" />
        </xsl:if>
        <xsl:call-template name="classpath" />
      </path>
      
      <taskdef name="xjc" classname="com.sun.tools.xjc.XJCTask">
        <classpath refid="classpath" />
      </taskdef>
      
      <xsl:if test="$hasDependencyJar">
        <xsl:comment>
          Check if the necessary jar files are properly installed.
        </xsl:comment>
        <target name="jar-check">
          <xsl:for-each select="sample/project/depends/jar">
            <available file="lib/{@name}" property="{@name}-present" />
            <fail unless="{@name}-present">
              Please download {@name} from the web and place it in the lib directory.
            </fail>
          </xsl:for-each>
        </target>
      </xsl:if>
      
      
      <xsl:comment>compile Java source files</xsl:comment>
      <target name="compile" description="Compile all Java source files">
        <xsl:if test="$hasDependencyJar">
          <xsl:attribute name="depends">jar-check</xsl:attribute>
        </xsl:if>
        
        <!-- compile datatype converter first -->
        <xsl:if test="$hasDatatypeConverter">
          <mkdir dir="{$dir.classes}" />
          <javac destdir="{$dir.classes}" debug="on" srcdir="src">
            <xsl:copy-of select="sample/project/datatypeConverterSrc/src/*" />
            <classpath refid="classpath" />
          </javac>
        </xsl:if>
        
        <echo message="Compiling the schema..."/>
        <mkdir dir="{$dir.jaxb}" />
        
        <!-- produce xjc tasks -->
        <xsl:for-each select="sample/project/xjc">
          <!-- add @target and copy the rest -->
          <xsl:copy>
            <xsl:copy-of select="./@*"/>
            <xsl:attribute name="target">
              <xsl:value-of select="$dir.jaxb"/>
            </xsl:attribute>
            <xsl:copy-of select="./*"/>
            
            <!-- compute the directory where files are produced -->
            <xsl:variable name="produceDir">
              <xsl:choose>
                <xsl:when test="./@package">
                  <xsl:value-of select="concat(concat( $dir.jaxb , '/' ) , string(@package) )"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$dir.jaxb"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <produces dir="{$produceDir}" includes="**/*.java" />
            
            <xsl:if test="$hasDatatypeConverter">
              <classpath>
                <xsl:comment>XJC needs to read compiled datatype converters</xsl:comment>
                <pathelement path="classes"/>
              </classpath>
            </xsl:if>
          </xsl:copy>
        </xsl:for-each>
        
        <echo message="Compiling the java source files..."/>
        <mkdir dir="{$dir.classes}" />
        <javac destdir="{$dir.classes}" debug="on">
          <xsl:if test="$isHog">
            <xsl:attribute name="fork">true</xsl:attribute>
            <xsl:attribute name="memoryInitialSize">100m</xsl:attribute>
            <xsl:attribute name="memoryMaximumSize">1000m</xsl:attribute>
          </xsl:if>
          <xsl:if test="$hasDriver">
            <src path="src" />
          </xsl:if>
          <src path="{$dir.jaxb}" />
          <classpath refid="classpath" />
        </javac>
        
        <copy todir="{$dir.classes}">
          <fileset dir="{$dir.jaxb}">
            <include name="**/*.properties" />
            <include name="**/bgm.ser" />
          </fileset>
        </copy>
      </target>
      
      <target name="run" depends="compile" description="Run the sample app">
        <xsl:choose>
          <xsl:when test="$hasDriver">
            <!-- build script to run the specified command -->
            <echo message="Running the sample application..."/>
            <java classname="{sample/project/java/@mainClass}" fork="true">
              <classpath refid="classpath" />
              <xsl:copy-of select="sample/project/java/arg" />
            </java>
          </xsl:when>
          <xsl:otherwise>
            <!-- otherwise noop -->
            <echo message="done" />
          </xsl:otherwise>
        </xsl:choose>
      </target>
      
      <xsl:if test="$javadoc">
        <!-- generate the javadoc target if so specified -->
        <target name="javadoc" description="Generates javadoc" depends="compile">
          <echo message="Generating javadoc..." />
          <mkdir dir="{$dir.javadoc}"/>
          <javadoc sourcepath="gen-src" destdir="{$dir.javadoc}" windowtitle="{sample/title}" useexternalfile="yes">
            <fileset dir="." includes="gen-src/**/*.java" excludes="**/impl/**/*.java" />
          </javadoc>
        </target>
      </xsl:if>
      
      <target name="clean" description="Deletes all the generated artifacts.">
        <xsl:if test="$javadoc">
          <delete dir="{$dir.javadoc}"/>
        </xsl:if>
        <delete dir="{$dir.jaxb}"/>
        <delete dir="{$dir.classes}"/>
      </target>
    </project>
  </xsl:template>
  
  <xsl:template name="classpath">
    <xsl:choose>
      <!-- for JWSDP release -->
      <xsl:when test="$target='JWSDP'">
        <xsl:comment>for use with bundled ant</xsl:comment>
        <fileset dir="${{jwsdp.home}}" includes="jaxb/lib/*.jar" />
        <fileset dir="${{jwsdp.home}}" includes="jwsdp-shared/lib/*.jar" />
        <fileset dir="${{jwsdp.home}}" includes="jaxp/lib/**/*.jar" />
      </xsl:when>
      <!-- for the workspace test -->
      <xsl:when test="$target='workspace'">
        <xsl:comment>for use with bundled ant</xsl:comment>
        <fileset dir="../../..">
          <include name="tools/lib/**/*.jar" />
        </fileset>
        <pathelement path="../../../xjc/build/classes" />
        <pathelement path="../../../xjc/src" />
        <pathelement path="../../../codemodel/build/classes" />
        <pathelement path="../../../runtime/build/classes" />
      </xsl:when>
      <!-- for the RI stand-alone distribution -->
      <xsl:when test="$target='RI'">
        <fileset dir="${{jaxb.home}}" includes="lib/*.jar" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Unknown target ${target}.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
