<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:femtocms="http://xml.apache.org/xalan/java/de.mobizcorp.femtocms.engine.BaseEngine"
  exclude-result-prefixes="femtocms">
  <!-- 
    femtocms minimalistic content management.
    Copyright(C) 2005-2006 mobizcorp Europe Ltd., all rights reserved.

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
  <xsl:template match="processing-instruction('femtocms-stylesheet')">
    <xsl:value-of select="femtocms:push($femtocms-engine, .)" disable-output-escaping="yes"/>
    <xsl:text></xsl:text>
  </xsl:template>
  <xsl:template match="processing-instruction('femtocms-javascript')">
    <xsl:value-of select="femtocms:push($femtocms-engine, .)" disable-output-escaping="yes"/>
    <xsl:text></xsl:text>
  </xsl:template>
  <xsl:template match="/*">
    <html>
      <head>
        <title><xsl:call-template name="html-title"/></title>
        <meta name="generator" content="femtocms 0.3"/><xsl:text></xsl:text>
        <xsl:apply-templates select="processing-instruction()"/>
        <xsl:call-template name="html-head"/>
      </head>
      <body>
        <xsl:call-template name="html-body"/>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="femtocms:footer">
    <hr noshade="noshade" size="1" style="margin: 0px;"/>
    <p style="font: 8pt sans-serif; text-align: center; margin: 0px;">
      Powered by femtocms, version <xsl:value-of select="femtocms:version($femtocms-engine)"/>.
      Last modified: <xsl:value-of select="femtocms:modified($femtocms-engine)"/>
    </p>
  </xsl:template>
  <xsl:template match="femtocms:link">
    <a href="{@href}">
      <xsl:if test="contains(@href, '://')">
        <!-- nothing special -->
      </xsl:if>
      <xsl:value-of select="."/>
      <xsl:choose>
        <xsl:when test="@img">
          <img src="{@img}" alt="{@href}" border="0"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="string(.) = ''"><xsl:value-of select="@href"/></xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>
  <xsl:template name="html-head"/>
  <xsl:template name="html-title">
    <xsl:value-of select="*/@title"/>
  </xsl:template>
  <xsl:template name="html-body">
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>
