<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:n52="http://www.n52.org/oxf" xmlns:xdt="http://www.w3.org/2005/xpath-datatypes" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output version="1.0" method="xml" encoding="UTF-8" indent="no"/>
	<xsl:param name="SV_OutputFormat" select="'PDF'"/>
	<xsl:variable name="XML" select="/"/>
	<xsl:variable name="fo:layout-master-set">
		<fo:layout-master-set>
			<fo:simple-page-master master-name="default-page" page-height="11in" page-width="8.5in" margin-left="0.6in" margin-right="0.6in">
				<fo:region-body margin-top="0.79in" margin-bottom="0.79in"/>
			</fo:simple-page-master>
		</fo:layout-master-set>
	</xsl:variable>
	<xsl:template match="/">
		<fo:root>
			<xsl:copy-of select="$fo:layout-master-set"/>
			<fo:page-sequence master-reference="default-page" initial-page-number="1" format="1">
				<fo:flow flow-name="xsl-region-body">
					<fo:block>
						<xsl:for-each select="$XML">
							<xsl:for-each select="n52:DocumentStructure">
								<fo:block>
									<fo:leader leader-pattern="space"/>
								</fo:block>
								<fo:inline-container>
									<fo:block>
										<xsl:text>&#x2029;</xsl:text>
									</fo:block>
								</fo:inline-container>
								<fo:block font-size="x-large" font-weight="bold" margin="0pt">
									<fo:block>
										<fo:inline font-weight="bold">
											<xsl:text>Diagram</xsl:text>
										</fo:inline>
									</fo:block>
								</fo:block>
								<fo:external-graphic content-width="scale-to-fit"
										content-height="100%"
										width="100%"
										scaling="uniform">
									<xsl:attribute name="src">
										<xsl:text>url(</xsl:text>
										<xsl:call-template name="double-backslash">
											<xsl:with-param name="text">
												<xsl:value-of select="string(n52:DiagramURL)"/>
											</xsl:with-param>
											<xsl:with-param name="text-length">
												<xsl:value-of select="string-length(string(n52:DiagramURL))"/>
											</xsl:with-param>
										</xsl:call-template>
										<xsl:text>)</xsl:text>
									</xsl:attribute>
								</fo:external-graphic>
								<fo:block>
									<fo:leader leader-pattern="space"/>
								</fo:block>
								<fo:inline-container>
									<fo:block>
										<xsl:text>&#x2029;</xsl:text>
									</fo:block>
								</fo:inline-container>
								<fo:block font-size="x-large" font-weight="bold" margin="0pt">
									<fo:block>
										<fo:inline font-weight="bold">
											<xsl:text>Legenda</xsl:text>
										</fo:inline>
								
									</fo:block>
								</fo:block>
								<fo:external-graphic>
									<xsl:attribute name="src">
										<xsl:text>url(</xsl:text>
										<xsl:call-template name="double-backslash">
											<xsl:with-param name="text">
												<xsl:value-of select="string(n52:LegendURL)"/>
											</xsl:with-param>
											<xsl:with-param name="text-length">
												<xsl:value-of select="string-length(string(n52:LegendURL))"/>
											</xsl:with-param>
										</xsl:call-template>
										<xsl:text>)</xsl:text>
									</xsl:attribute>
								</fo:external-graphic>
								<fo:block>
									<fo:leader leader-pattern="space"/>
								</fo:block>
								<xsl:for-each select="n52:TimeSeries">
									<fo:block>
										<fo:leader leader-pattern="space"/>
									</fo:block>
									<fo:inline-container>
										<fo:block>
											<xsl:text>&#x2029;</xsl:text>
										</fo:block>
									</fo:inline-container>
									<fo:block font-size="x-large" font-weight="bold" margin="0pt">
										<fo:block>
											<fo:inline>
												<xsl:text>Tijdreeks:</xsl:text>
											</fo:inline>
										</fo:block>
									</fo:block>
									<fo:inline-container>
										<fo:block>
											<xsl:text>&#x2029;</xsl:text>
										</fo:block>
									</fo:inline-container>
									<fo:table table-layout="fixed" width="100%" border="solid 1pt gray" border-spacing="2pt">
										<fo:table-column column-width="50%"/>
										<fo:table-column column-width="50%"/>
										<fo:table-body start-indent="0pt">
											<fo:table-row>
												<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
													<fo:block>
														<fo:inline>
															<xsl:text>Sensor Locatie</xsl:text>
														</fo:inline>
													</fo:block>
												</fo:table-cell>
												<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
													<fo:block>
														<xsl:for-each select="@featureOfInterestID">
															<fo:inline>
																<xsl:value-of select="string(.)"/>
															</fo:inline>
														</xsl:for-each>
													</fo:block>
												</fo:table-cell>
											</fo:table-row>
											<fo:table-row>
												<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
													<fo:block>
														<fo:inline>
															<xsl:text>Sensor Fenomeen</xsl:text>
														</fo:inline>
													</fo:block>
												</fo:table-cell>
												<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
													<fo:block>
														<xsl:for-each select="@phenomenID">
															<fo:inline>
																<xsl:value-of select="string(.)"/>
															</fo:inline>
														</xsl:for-each>
													</fo:block>
												</fo:table-cell>
											</fo:table-row>
											<fo:table-row>
												<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
													<fo:block>
														<fo:inline>
															<xsl:text>Sensor Type</xsl:text>
														</fo:inline>
													</fo:block>
												</fo:table-cell>
												<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
													<fo:block>
														<xsl:for-each select="@procedureID">
															<fo:inline>
																<xsl:value-of select="string(.)"/>
															</fo:inline>
														</xsl:for-each>
													</fo:block>
												</fo:table-cell>
											</fo:table-row>
										</fo:table-body>
									</fo:table>
									<fo:block>
										<fo:leader leader-pattern="space"/>
									</fo:block>
									<fo:inline font-weight="bold">
										<xsl:text>Metadata van het tijdreeks:</xsl:text>
									</fo:inline>
									<fo:inline-container>
										<fo:block>
											<xsl:text>&#x2029;</xsl:text>
										</fo:block>
									</fo:inline-container>
									<xsl:if test="n52:Metadata/n52:genericMetadataPair">
										<fo:table table-layout="fixed" width="100%" border="solid 1pt gray" border-spacing="2pt">
											<fo:table-column column-width="proportional-column-width(1)"/>
											<fo:table-column column-width="proportional-column-width(1)"/>
											<fo:table-header start-indent="0pt">
												<fo:table-row>
													<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
														<fo:block>
															<fo:inline>
																<xsl:text>Eigenschaap</xsl:text>
															</fo:inline>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
														<fo:block>
															<fo:inline>
																<xsl:text>Waard</xsl:text>
															</fo:inline>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-header>
											<fo:table-body start-indent="0pt">
												<xsl:for-each select="n52:Metadata">
													<xsl:for-each select="n52:genericMetadataPair">
														<fo:table-row>
															<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
																<fo:block>
																	<xsl:for-each select="@name">
																		<fo:inline>
																			<xsl:value-of select="string(.)"/>
																		</fo:inline>
																	</xsl:for-each>
																</fo:block>
															</fo:table-cell>
															<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
																<fo:block>
																	<xsl:for-each select="@value">
																		<fo:inline>
																			<xsl:value-of select="string(.)"/>
																		</fo:inline>
																	</xsl:for-each>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</xsl:for-each>
												</xsl:for-each>
											</fo:table-body>
										</fo:table>
									</xsl:if>
									<fo:block>
										<fo:leader leader-pattern="space"/>
									</fo:block>
									<fo:inline font-weight="bold">
										<xsl:text>Sensor Data:</xsl:text>
									</fo:inline>
									<fo:inline-container>
										<fo:block>
											<xsl:text>&#x2029;</xsl:text>
										</fo:block>
									</fo:inline-container>
									<xsl:if test="n52:Table/n52:entry">
										<fo:table table-layout="fixed" width="100%" border="solid 1pt gray" border-spacing="2pt">
											<fo:table-column column-width="200"/>
											<fo:table-column column-width="proportional-column-width(1)"/>
											<fo:table-header start-indent="0pt">
												<fo:table-row>
													<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
														<fo:block>
															<xsl:for-each select="n52:Table">
																<xsl:for-each select="n52:leftColHeader">
																	<xsl:variable name="value-of-template">
																		<xsl:apply-templates/>
																	</xsl:variable>
																	<xsl:choose>
																		<xsl:when test="contains(string($value-of-template),'&#x2029;')">
																			<fo:block>
																				<xsl:copy-of select="$value-of-template"/>
																			</fo:block>
																		</xsl:when>
																		<xsl:otherwise>
																			<fo:inline>
																				<xsl:copy-of select="$value-of-template"/>
																			</fo:inline>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:for-each>
															</xsl:for-each>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
														<fo:block>
															<xsl:for-each select="n52:Table">
																<xsl:for-each select="n52:rightColHeader">
																	<xsl:variable name="value-of-template">
																		<xsl:apply-templates/>
																	</xsl:variable>
																	<xsl:choose>
																		<xsl:when test="contains(string($value-of-template),'&#x2029;')">
																			<fo:block>
																				<xsl:copy-of select="$value-of-template"/>
																			</fo:block>
																		</xsl:when>
																		<xsl:otherwise>
																			<fo:inline>
																				<xsl:copy-of select="$value-of-template"/>
																			</fo:inline>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:for-each>
															</xsl:for-each>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-header>
											<fo:table-body start-indent="0pt">
												<xsl:for-each select="n52:Table">
													<xsl:for-each select="n52:entry">
														<fo:table-row>
															<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
																<fo:block>
																	<xsl:for-each select="@time">
																		<fo:inline>
																			<xsl:value-of select="string(.)"/>
																		</fo:inline>
																	</xsl:for-each>
																</fo:block>
															</fo:table-cell>
															<fo:table-cell border="solid 1pt gray" padding="2pt" display-align="center">
																<fo:block>
																	<xsl:for-each select="@value">
																		<fo:inline>
																			<xsl:value-of select="string(.)"/>
																		</fo:inline>
																	</xsl:for-each>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</xsl:for-each>
												</xsl:for-each>
											</fo:table-body>
										</fo:table>
									</xsl:if>
								</xsl:for-each>
							</xsl:for-each>
						</xsl:for-each>
					</fo:block>
					<fo:block id="SV_RefID_PageTotal"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	<xsl:template name="double-backslash">
		<xsl:param name="text"/>
		<xsl:param name="text-length"/>
		<xsl:variable name="text-after-bs" select="substring-after($text, '\')"/>
		<xsl:variable name="text-after-bs-length" select="string-length($text-after-bs)"/>
		<xsl:choose>
			<xsl:when test="$text-after-bs-length = 0">
				<xsl:choose>
					<xsl:when test="substring($text, $text-length) = '\'">
						<xsl:value-of select="concat(substring($text,1,$text-length - 1), '\\')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$text"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat(substring($text,1,$text-length - $text-after-bs-length - 1), '\\')"/>
				<xsl:call-template name="double-backslash">
					<xsl:with-param name="text" select="$text-after-bs"/>
					<xsl:with-param name="text-length" select="$text-after-bs-length"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
