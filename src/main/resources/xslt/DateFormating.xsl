<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" />

	<!-- DATE TEMPLATE, should be extrated and added as a library -->
	<!-- Expected format for dateWithTimezone : yyyy-mm-dd hh:mm:ss.SSS UTC -->
	<xsl:template name="formatDateWithTimeZone">
		<xsl:param name="dateWithTimezone" />

		<xsl:variable name="date">
			<xsl:value-of select="substring($dateWithTimezone,1,10)" />
		</xsl:variable>

		<xsl:variable name="time">
			<xsl:value-of
				select="substring($dateWithTimezone,12,12)" />
		</xsl:variable>

		<xsl:value-of select="concat($date,'T', $time,'000Z')" />
	</xsl:template>

</xsl:stylesheet>