<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- Copyright (C) 2005 mobizcorp Europe Ltd., all rights reserved. -->
  <xsl:template name="femtocms-copy" match="@*|node()|comment()|processing-instruction()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|comment()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
