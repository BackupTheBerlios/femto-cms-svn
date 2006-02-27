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
  <xsl:import href="html.xsl"/>
  <xsl:param name="femtocms-engine"/>
  <xsl:param name="femtocms-base"/>
  <xsl:param name="femtocms-href"/>
  <xsl:param name="femtocms-sha1"/>
  <xsl:output method="xml"
    media-type="text/vnd.wap.wml"
    doctype-public="-//WAPFORUM//DTD WML 1.1//EN"
    doctype-system="http://www.wapforum.org/DTD/wml_1.1.xml"/>
  <xsl:template match="/">
    <wml>
      <xsl:apply-templates select="//h1" mode="wml-cards"/>
    </wml>
  </xsl:template>
  <xsl:template match="h1" mode="wml-cards">
    <card title="{.}">
      <xsl:apply-templates
        select="following-sibling::*[preceding-sibling::h1[1] = current()]"/>
    </card>
  </xsl:template>
</xsl:stylesheet>
