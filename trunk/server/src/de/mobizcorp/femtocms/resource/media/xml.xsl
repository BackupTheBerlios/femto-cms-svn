<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:femtocms="http://xml.apache.org/xalan/java/de.mobizcorp.femtocms.engine.BaseEngine"
  exclude-result-prefixes="femtocms">
  <!-- 
    femtocms minimalistic content management.
    Copyright(C) 2005 mobizcorp Europe Ltd., all rights reserved.

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  -->
  <xsl:import href="femtocms:/copy.xsl"/>
  <xsl:param name="femtocms-engine"/>
  <xsl:param name="femtocms-base"/>
  <xsl:param name="femtocms-href"/>
  <xsl:param name="femtocms-sha1"/>
  <xsl:output method="html" doctype-public="-//W3C//DTD HTML 3.2 Final//EN" indent="yes"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta name="generator" content="femtocms 0.3"/>
        <br/>
      </head>
      <body>
        <xsl:text>&lt;?xml version="1.0"?&gt;</xsl:text>
        <br/>
        <xsl:apply-templates select="@*|node()"/>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="*">
    <xsl:apply-templates select="." mode="xml-indent"/>
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:apply-templates select="@*"/>
    <xsl:choose>
      <xsl:when test="node()">
        <xsl:text>&gt;</xsl:text>
        <xsl:apply-templates select="node()"/>
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>/</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&gt;</xsl:text>
  </xsl:template>
  <xsl:template match="processing-instruction()">
    <xsl:text>&lt;?</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>&#160;</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>?&gt;</xsl:text>
    <br/>
  </xsl:template>
  <xsl:template match="comment()">
    <xsl:text>&lt;-- </xsl:text>
    <xsl:value-of select="."/>
    <xsl:text> --&gt;</xsl:text>
    <br/>
  </xsl:template>
  <xsl:template match="@*">
    <xsl:text>&#160;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text>
    <code><xsl:value-of select="."/></code>
    <xsl:text>"</xsl:text>
  </xsl:template>
  <xsl:template match="text()">
    <code><xsl:value-of select="."/></code>
  </xsl:template>
  <xsl:template match="node()" mode="xml-indent">
    <br/>
    <xsl:value-of select="substring('&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;', 1, count(ancestor::*))"/>
  </xsl:template>
</xsl:stylesheet>