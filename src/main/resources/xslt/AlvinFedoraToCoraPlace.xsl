<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" />
	<xsl:template match="/">
		<xsl:apply-templates select="place" />
	</xsl:template>
		<xsl:template match="place">
		<authority type="place">
			<xsl:apply-templates select="recordInfo">
				<xsl:with-param name="authorityType" select="'place'"></xsl:with-param>
			</xsl:apply-templates>
			<xsl:apply-templates select="defaultPlaceName" />
			<xsl:apply-templates select="placeNameForms" />
			<xsl:if
				test="string-length(longitude) &gt; 0 or string-length(latitude) &gt; 0">
				<xsl:call-template name="coordinates" />
			</xsl:if>
			<xsl:apply-templates select="country" />
			<xsl:apply-templates select="localIdentifiers" />
		</authority>
	</xsl:template>
	<xsl:template match="recordInfo">
		<xsl:param name="authorityType"></xsl:param>
		<recordInfo>
			<id>
				<xsl:value-of select="../pid"></xsl:value-of>
			</id>
			<type>
				<linkedRecordType>recordType</linkedRecordType>
				<linkedRecordId>
					<xsl:value-of select="$authorityType"></xsl:value-of>
				</linkedRecordId>
			</type>
			<createdBy>
				<linkedRecordType>user</linkedRecordType>
				<linkedRecordId>
					<!-- Not implemented correctly yet. Currently hardcoded value 12345 -->
					<!-- <xsl:value-of select="created/user/userId"></xsl:value-of> -->
					12345
				</linkedRecordId>
			</createdBy>
			<tsCreated>
				<xsl:value-of select="created/date"></xsl:value-of>
			</tsCreated>
			<dataDivider>
				<linkedRecordType>system</linkedRecordType>
				<linkedRecordId>alvin</linkedRecordId>
			</dataDivider>
			<xsl:for-each
				select="updated/userAction[type = 'UPDATED']">
				<updated>
					<xsl:attribute name="repeatId">
                        <xsl:value-of
						select="position() - 1"></xsl:value-of>
                    </xsl:attribute>
					<updatedBy>
						<linkedRecordType>user</linkedRecordType>
						<linkedRecordId>
							<xsl:value-of select="user/userId"></xsl:value-of>
						</linkedRecordId>
					</updatedBy>
					<tsUpdated>
						<xsl:value-of select="date"></xsl:value-of>
						<!-- <xsl:value-of select="date:date-time()"></xsl:value-of> -->
						<!-- <xsl:value-of select="format-dateTime(current-date(),'[MNn] [D],[Y]')"></xsl:value-of> -->
					</tsUpdated>
				</updated>
			</xsl:for-each>
		</recordInfo>
	</xsl:template>
	<xsl:template match="defaultPlaceName">
		<name type="authorized">
			<namePart type="defaultName">
				<value>
					<xsl:value-of select="name"></xsl:value-of>
				</value>
			</namePart>
		</name>
	</xsl:template>
	<xsl:template match="placeNameForms">
		<xsl:for-each select="entry">
			<name type="alternative">
				<xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
				<xsl:for-each select="placeNameForm">
					<xsl:apply-templates select="language"></xsl:apply-templates>
					<namePart type="defaultName">
						<value>
							<xsl:value-of select="name"></xsl:value-of>
						</value>
					</namePart>
				</xsl:for-each>
			</name>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="coordinates">
		<coordinates>
			<xsl:for-each select="latitude">
				<latitude>
					<xsl:value-of select="."></xsl:value-of>
				</latitude>
			</xsl:for-each>
			<xsl:for-each select="longitude">
				<longitude>
					<xsl:value-of select="."></xsl:value-of>
				</longitude>
			</xsl:for-each>
		</coordinates>
	</xsl:template>
	<xsl:template match="country">
		<xsl:choose>
			<xsl:when test="@class = 'historicCountry'">
				<historicCountry>
					<xsl:value-of select="code"></xsl:value-of>
				</historicCountry>
			</xsl:when>
			<xsl:otherwise>
				<country>
					<xsl:value-of select="alpha2Code"></xsl:value-of>
				</country>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="language">
		<language>
			<xsl:value-of select="alpha3Code"></xsl:value-of>
		</language>
	</xsl:template>
	<xsl:template match="localIdentifiers">
		<xsl:for-each select="localIdentifier/type">
			<identifier>
				<xsl:attribute name="repeatId">
                    <xsl:value-of select="position() - 1"></xsl:value-of>
                </xsl:attribute>
				<identifierType>
					<xsl:value-of select="code"></xsl:value-of>
				</identifierType>
				<identifierValue>
					<xsl:value-of select="id"></xsl:value-of>
				</identifierValue>
			</identifier>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>