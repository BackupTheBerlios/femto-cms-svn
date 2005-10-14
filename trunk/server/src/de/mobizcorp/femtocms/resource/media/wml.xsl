<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:femtocms="http://xml.apache.org/xalan/java/de.mobizcorp.femtocms.engine.BaseEngine"
  exclude-result-prefixes="femtocms">
  <!-- Copyright (C) 2005 mobizcorp Europe Ltd., all rights reserved. -->
  <xsl:param name="femtocms-engine"/>
  <xsl:param name="femtocms-base"/>
  <xsl:param name="femtocms-href"/>
  <xsl:param name="femtocms-sha1"/>
  <xsl:import href="html.xsl"/>
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
