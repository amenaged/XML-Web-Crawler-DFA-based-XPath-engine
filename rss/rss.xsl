<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" indent="yes" />
	<xsl:template match="/">
		<html>
			<head>
				<title>Display a Channel</title>
			</head>
			<body>
				<xsl:for-each select="documentcollection/document/rss/channel">
					<a>
						<xsl:attribute name="href">
									<xsl:value-of select="./link" />
								</xsl:attribute>
						<h4>
							<xsl:value-of select="./title"
								disable-output-escaping="yes" />
						</h4>
					</a>
					<table border="1" style="width:90%">
						<col width="30%" />
						<col width="70%" />
						<tr>
							<th>Title</th>
							<th>Description</th>
						</tr>
						<xsl:for-each select="./item">
							<tr>
								<td>
									<xsl:choose>
										<xsl:when test="(./title) and (./link)">
											<a>
												<xsl:attribute name="href"><xsl:value-of
													select="./link" />
										</xsl:attribute>
												<xsl:value-of select="./title"
													disable-output-escaping="yes" />
											</a>
										</xsl:when>
										<xsl:when test="(./title) and not (./link)">
											<xsl:value-of select="./title"
												disable-output-escaping="yes" />
										</xsl:when>
										<xsl:otherwise>
											Title Not Exist.
										</xsl:otherwise>
									</xsl:choose>
								</td>
								<td>
									<xsl:choose>
										<xsl:when test="./description">
											<xsl:value-of select="./description"
												disable-output-escaping="yes" />
										</xsl:when>
										<xsl:otherwise>
											Description Not Exist
										</xsl:otherwise>
									</xsl:choose>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>