<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
  <xsl:param name="femtocms-engine"/>
  <xsl:param name="femtocms-base"/>
  <xsl:param name="femtocms-href"/>
  <xsl:param name="femtocms-sha1"/>
  <xsl:import href="femtocms:/media/html.xsl"/>
  <xsl:output method="html" doctype-public="-//W3C//DTD HTML 3.2 Final//EN" indent="yes"/>
  <xsl:template name="html-head">
    <xsl:text>
      <style type="text/css"><xsl:comment>
      #controls {
	position: absolute;
	overflow: visible;
	color: black;
	background: transparent;
	padding: 4px;
	border: 1px dotted black;
      }
      #fckeditor0 {
        display: none;
      }
      #controls_deco, IFRAME {
        z-index: 4;
      }
      </xsl:comment></style>
      <script language="JavaScript" type="text/javascript" src="/fckeditor/fckeditor.js"></script>
      <script language="JavaScript" type="text/javascript" src="/femtocms:/controls.js"></script>
    </xsl:text>
  </xsl:template>
  <xsl:template name="html-body">
    <xsl:apply-templates select="*"/>
    <div id="controls"
      onkeypress="narrow_controls()"
      onmouseover="keep_controls()"
      onmouseout="hide_controls()"
      onclick="open_editor()">
      <div id="controls_deco">
        <div align="right">
          <img src="/femtocms:x.png" title="Cancel edit" onclick="return close_editor(event);"/>
        </div>
        <form method="post" action="#">
          <input type="hidden" id="edit-id" name="edit-id" value=""/>
          <input type="hidden" id="edit-lastmodified" name="edit-lastmodified" value="0"/>
          <textarea id="fckeditor0" name="fckeditor0"></textarea>
        </form>
      </div>
    </div>
  </xsl:template>
  <xsl:template match="processing-instruction('femtocms-edit-location')">
    <xsl:variable name="system" select="substring-before(substring-after(., 'system=&quot;'), '&quot;')"/>
    <xsl:variable name="line" select="substring-before(substring-after(., 'line=&quot;'), '&quot;')"/>
    <xsl:variable name="column" select="substring-before(substring-after(., 'column=&quot;'), '&quot;')"/>
    <xsl:attribute name="femtocms:edit-id">
      <xsl:value-of select="$system"/>
      <xsl:text>:</xsl:text>
      <xsl:value-of select="$line"/>
      <xsl:text>:</xsl:text>
      <xsl:value-of select="$column"/>
    </xsl:attribute>
    <xsl:attribute name="femtocms:edit-lastmodified">
      <xsl:value-of select="femtocms:modified($femtocms-engine, string($system))"/>
    </xsl:attribute>
  </xsl:template>
  <xsl:template match="@id|@*[starts-with(name(), 'on')]" priority="-1">
    <xsl:attribute name="femtocms:save-{name()}">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
