<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:femtocms="http://xml.apache.org/xalan/java/de.mobizcorp.femtocms.engine.BaseEngine">
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
  <xsl:import href="copy.xsl"/>
  <xsl:output method="xml" indent="yes"
    cdata-section-elements="listing plaintext xmp"/>
  <!-- convert anchors back to femtocms:link -->
  <xsl:template match="a">
    <femtocms:link>
      <xsl:attribute name="href">
        <xsl:value-of select="@href"/>
      </xsl:attribute>
      <xsl:if test="@href != text()">
        <xsl:copy-of select="text()"/>
      </xsl:if>
    </femtocms:link>
  </xsl:template>
  <!--
   ! Verification templates. This is minimized to the elements that
   ! have the closing tag declared optional. We must keep these in
   ! proper shape here because the browser parser will insert implicit
   ! end nodes that will foul our processor later when the element
   ! was edited and saved back.
   !-->
  <!-- verify empty elements -->
  <xsl:template match="area|base|basefont|br|col|hr|img|input|isindex|link|meta|param">
    <xsl:if test="*|text()">
      <xsl:message terminate="yes">
        <xsl:value-of select="name(..)"/>
        <xsl:text> not EMPTY</xsl:text>
      </xsl:message>
    </xsl:if>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
  <!-- verify elements with content model %text -->
  <xsl:template match="dt|p">
    <xsl:for-each select="*[not(self::tt|self::i|self::b|self::u|self::s|self::strike|self::big|self::small|self::sub|self::sup|self::em|self::strong|self::dfn|self::code|self::samp|self::kbd|self::var|self::cite|self::abbr|self::acronym|self::a|self::femtocms:link|self::img|self::applet|self::object|self::font|self::basefont|self::br|self::script|self::map|self::q|self::span|self::bdo|self::iframe|self::input|self::select|self::textarea|self::label|self::button)]">
      <xsl:message terminate="yes">
        <xsl:value-of select="name()"/>
        <xsl:text> not allowed in </xsl:text>
        <xsl:value-of select="name(..)"/>
      </xsl:message>
    </xsl:for-each>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
  <!-- verify elements with content model %flow -->
  <xsl:template match="dd|li|th|td">
    <xsl:for-each select="*[not(self::tt|self::i|self::b|self::u|self::s|self::strike|self::big|self::small|self::sub|self::sup|self::em|self::strong|self::dfn|self::code|self::samp|self::kbd|self::var|self::cite|self::abbr|self::acronym|self::a|self::femtocms:link|self::img|self::applet|self::object|self::font|self::basefont|self::br|self::script|self::map|self::q|self::span|self::bdo|self::iframe|self::input|self::select|self::textarea|self::label|self::button|self::p|self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::ul|self::ol|self::dir|self::menu|self::pre|self::xmp|self::listing|self::dl|self::div|self::center|self::noscript|self::noframes|self::blockquote|self::form|self::isindex|self::hr|self::table|self::fieldset|self::address)]">
      <xsl:message terminate="yes">
        <xsl:value-of select="name()"/>
        <xsl:text> not allowed in </xsl:text>
        <xsl:value-of select="name(..)"/>
      </xsl:message>
    </xsl:for-each>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
  <!-- verify tr -->
  <xsl:template match="tr">
    <xsl:for-each select="*[not(self::td|self::th)]">
      <xsl:message terminate="yes">
        <xsl:value-of select="name()"/>
        <xsl:text> not allowed in </xsl:text>
        <xsl:value-of select="name(..)"/>
      </xsl:message>
    </xsl:for-each>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
  <!-- verify thead and tfoot -->
  <xsl:template match="thead|tfoot">
    <xsl:for-each select="*[not(self::tr)]">
      <xsl:message terminate="yes">
        <xsl:value-of select="name()"/>
        <xsl:text> not allowed in </xsl:text>
        <xsl:value-of select="name(..)"/>
      </xsl:message>
    </xsl:for-each>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
  <!-- verify colgroup -->
  <xsl:template match="colgroup">
    <xsl:for-each select="*[not(self::col)]">
      <xsl:message terminate="yes">
        <xsl:value-of select="name()"/>
        <xsl:text> not allowed in </xsl:text>
        <xsl:value-of select="name(..)"/>
      </xsl:message>
    </xsl:for-each>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
  <xsl:template match="listing|option|plaintext|xmp">
    <xsl:for-each select="*">
      <xsl:message terminate="yes">
        <xsl:value-of select="name(..)"/>
        <xsl:text> may only contain character data</xsl:text>
      </xsl:message>
    </xsl:for-each>
    <xsl:call-template name="femtocms-copy"/>
  </xsl:template>
</xsl:stylesheet>
