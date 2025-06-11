<?xml version="1.1" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:java="http://xml.apache.org/xslt/java"
				exclude-result-prefixes="java">
	<xsl:output indent="yes"/>
	<xsl:decimal-format name="european" decimal-separator="." grouping-separator="&#xa0;" NaN="0.00"/>

	<xsl:param name="annotated"/>
	<xsl:param name="scopeModelId"/>

	<!-- global variables -->
	<xsl:variable name="page_width">297</xsl:variable>
	<xsl:variable name="page_height">210</xsl:variable>

	<xsl:variable name="font_name">Helvetica</xsl:variable>
	<xsl:variable name="font_size">9</xsl:variable>

	<!-- document name -->
	<xsl:variable name="document_name">
		<xsl:if test="$annotated='true'">Annotated blank CRF</xsl:if>
		<xsl:if test="$annotated='false'">Blank CRF</xsl:if>
	</xsl:variable>

	<xsl:template name="image-select">
		<fo:instream-foreign-object xmlns:svg="http://www.w3.org/2000/svg">
			<svg:svg width="30" height="10">
				<svg:rect x="0.5" y="0.5" width="29" height="9" style="stroke: black; stroke-width: 0.5; fill: white"/>
				<svg:polygon points="22 3, 28 3, 25 7" style="fill: black;"/>
			</svg:svg>
		</fo:instream-foreign-object>
	</xsl:template>

	<xsl:template name="image-checkbox">
		<fo:instream-foreign-object xmlns:svg="http://www.w3.org/2000/svg">
			<svg:svg width="10" height="10">
				<svg:rect x="0.5" y="0.5" width="9" height="9" style="stroke: black; stroke-width: 0.5; fill: white"/>
			</svg:svg>
		</fo:instream-foreign-object>
	</xsl:template>

	<xsl:template name="image-radio">
		<fo:instream-foreign-object xmlns:svg="http://www.w3.org/2000/svg">
			<svg:svg width="10" height="10">
				<svg:circle cx="5.5" cy="5.5" r="4" style="stroke: black; stroke-width: 0.5; fill: white"/>
			</svg:svg>
		</fo:instream-foreign-object>
	</xsl:template>

	<xsl:template name="image-field">
		<xsl:param name="length"/>
		<fo:instream-foreign-object xmlns:svg="http://www.w3.org/2000/svg">
			<svg:svg width="{$length}" height="10">
				<svg:rect x="0.5" y="0.5" width="{$length - 1}" height="9" style="stroke: black; stroke-width: 0.5; fill: white"/>
			</svg:svg>
		</fo:instream-foreign-object>
	</xsl:template>

	<xsl:template name="image-file">
		<fo:instream-foreign-object xmlns:svg="http://www.w3.org/2000/svg">
			<svg:svg width="10" height="12">
				<svg:polyline points="0.5 0.5, 6.5 0.5, 6.5 3.5, 9.5 3.5, 9.5 11.5, 0.5 11.5, 0.5 0.5" fill="none" style="stroke: black; stroke-width: 0.5;"/>
				<svg:line x1="6.5" y1="0.5" x2="9.5" y2="3.5" style="stroke: black; stroke-width: 0.5;"/>
				<svg:polyline points="2.5 8.5, 2.5 9.5, 7.5 9.5, 7.5 8.5" fill="none" style="stroke: black; stroke-width: 0.5;"/>
				<svg:line x1="5" y1="4.5" x2="5" y2="8.5" style="stroke: black; stroke-width: 0.5;"/>
				<svg:polyline points="3.5 6, 5 4.5, 6.5 6" fill="none" style="stroke: black; stroke-width: 0.5;"/>
			</svg:svg>
		</fo:instream-foreign-object>
	</xsl:template>

	<xsl:attribute-set name="page-title">
		<xsl:attribute name="font-family"><xsl:value-of select="$font_name" /></xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$font_size + 2" />pt</xsl:attribute>
		<xsl:attribute name="font-weight">bold</xsl:attribute>
		<xsl:attribute name="border-bottom">2px solid black</xsl:attribute>
		<xsl:attribute name="space-after">3mm</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="toc-item">
		<xsl:attribute name="font-family"><xsl:value-of select="$font_name" /></xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$font_size" />pt</xsl:attribute>
		<xsl:attribute name="text-align-last">justify</xsl:attribute>
		<xsl:attribute name="space-before.optimum">2mm</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="block-condition">
		<xsl:attribute name="font-family"><xsl:value-of select="$font_name" /></xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$font_size" />pt</xsl:attribute>
		<xsl:attribute name="color">#999</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="annotation">
		<xsl:attribute name="font-family"><xsl:value-of select="$font_name" /></xsl:attribute>
		<xsl:attribute name="font-size"><xsl:value-of select="$font_size - 3" />pt</xsl:attribute>
		<xsl:attribute name="color">red</xsl:attribute>
	</xsl:attribute-set>

	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<!-- master pages -->
			<fo:layout-master-set>
				<fo:simple-page-master master-name="standard_page" page-height="{$page_height}mm" page-width="{$page_width}mm" margin="5mm">
					<fo:region-body margin-top="0" margin-bottom="5mm" margin-left="5mm" margin-right="5mm" />
					<fo:region-before extent="5mm"/>
					<fo:region-after extent="5mm"/>
					<fo:region-start extent="5mm"/>
					<fo:region-end extent="5mm"/>
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:bookmark-tree>
				<xsl:for-each select="/study/scopeModels/scopeModel[@id=$scopeModelId]/eventModels/eventModel">
					<fo:bookmark internal-destination="{generate-id()}">
						<fo:bookmark-title font-weight="bold">
							<xsl:value-of select="@shortname"/>
						</fo:bookmark-title>
						<xsl:for-each select="formModels/formModel">
							<fo:bookmark internal-destination="{generate-id()}">
								<fo:bookmark-title>
									<xsl:value-of select="@shortname"/>
								</fo:bookmark-title>
							</fo:bookmark>
						</xsl:for-each>
					</fo:bookmark>
				</xsl:for-each>
			</fo:bookmark-tree>

			<!-- pages content -->
			<!-- first page -->
			<fo:page-sequence master-reference="standard_page" initial-page-number="auto">
				<fo:static-content flow-name="xsl-region-before">
					<fo:block text-align="justify" font-size="{$font_size}pt" font-family="{$font_name}" border-bottom="1px solid #cccccc">
						<fo:block>Client:
							<xsl:value-of select="/study/client"/>
						</fo:block>
						<fo:block text-align-last="justify">
							Study: <xsl:value-of select="/study/@shortname"/> - Protocol number: <xsl:value-of select="/study/protocolNo"/>
							<fo:leader leader-pattern="space"/>
							Rodano
						</fo:block>
					</fo:block>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align-last="justify" font-size="{$font_size}pt" font-family="{$font_name}" border-top="1px solid #cccccc">
						<xsl:value-of select="/study/@shortname"/>&#160;<xsl:value-of select="$document_name" /> - Version <xsl:value-of select="/study/versionNumber"/> - <xsl:value-of select="/study/versionDate"/>
						<fo:leader leader-pattern="space"/>
						Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<fo:block-container position="absolute" top="40mm" left="0" height="150mm" width="100%">
						<fo:block font-size="22pt" font-family="{$font_name}" text-align="center" font-weight="bold" space-after="5mm">
							<xsl:value-of select="/study/@shortname"/>&#160;Study
						</fo:block>
						<fo:block font-size="14pt" font-family="{$font_name}" text-align="center" font-weight="bold">
							<xsl:value-of select="$document_name" />
						</fo:block>
					</fo:block-container>
				</fo:flow>
			</fo:page-sequence>

			<!-- table of content page -->
			<fo:page-sequence master-reference="standard_page" initial-page-number="auto">
				<fo:static-content flow-name="xsl-region-before">
					<fo:block/>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align-last="justify" font-size="{$font_size}pt" font-family="{$font_name}" border-top="1px solid #cccccc">
						<xsl:value-of select="/study/@shortname"/>&#160;<xsl:value-of select="$document_name" /> - Version <xsl:value-of select="/study/versionNumber"/> - <xsl:value-of select="/study/versionDate"/>
						<fo:leader leader-pattern="space"/>
						Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<fo:block xsl:use-attribute-sets="page-title">Table of contents</fo:block>
					<xsl:apply-templates select="/" mode="toc"/>
				</fo:flow>
			</fo:page-sequence>

			<!-- matrix page -->
			<!--<fo:page-sequence master-reference="standard_page" force-page-count="no-force">
				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align-last="justify" font-size="{$font_size}pt" font-family="{$font_name}" border-top="1px solid #cccccc">
						<xsl:value-of select="/study/@shortname"/>&#160;<xsl:value-of select="$document_name" /> - Version <xsl:value-of select="/study/versionNumber"/> - <xsl:value-of select="/study/versionDate"/>
						<fo:leader leader-pattern="space"/>
						Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<fo:block xsl:use-attribute-sets="page-title">Tests and Assessments</fo:block>

					<fo:block id="matrix">
						<fo:table table-layout="fixed" width="100%" space-after="10mm" border="1pt solid #333333">
							<fo:table-column column-number="1" column-width="40mm"/>
							<xsl:for-each select="/scope[1]/event">
								<xsl:variable name="cur" select='position()'/>
								<fo:table-column column-number="{$cur + 1}"
												 column-width="proportional-column-width(1)"/>
							</xsl:for-each>

							<fo:table-body>
								<fo:table-row>
									<fo:table-cell border="1pt solid #333333" padding="3pt" wrap-option="wrap">
										<fo:block/>
									</fo:table-cell>
									<xsl:for-each select="/scope[1]/event">
										<fo:table-cell border="1pt solid #333333" padding="3pt" wrap-option="wrap">
											<fo:block font-size="{$font_size}pt" font-family="{$font_name}"
													  text-align="center">
												<xsl:value-of select="@shortname"/>
											</fo:block>
										</fo:table-cell>
									</xsl:for-each>
								</fo:table-row>

								<xsl:for-each select="/scope[1]/page_matrix">
									<xsl:variable name="top_page" select='@shortname'/>
									<xsl:choose>
										<xsl:when test="not(position() mod 2)">
											<fo:table-row background-color="#EEEEEE">
												<fo:table-cell border="1pt solid #333333" padding="3pt"
															   wrap-option="wrap">
													<fo:block font-size="{$font_size}pt" font-family="{$font_name}">
														<xsl:value-of select="@shortname"/>
													</fo:block>
												</fo:table-cell>

												<xsl:for-each select="/scope[1]/event">
													<fo:table-cell border="1pt solid #333333" padding="3pt"
																   wrap-option="wrap">
														<xsl:for-each select="page">
															<fo:block font-size="{$font_size}pt"
																	  font-family="{$font_name}"
																	  text-align="center"
																	  font-weight="bold">
																<xsl:if test="@shortname = $top_page">X</xsl:if>
															</fo:block>
														</xsl:for-each>
													</fo:table-cell>
												</xsl:for-each>
											</fo:table-row>
										</xsl:when>
										<xsl:otherwise>
											<fo:table-row background-color="#FFFFFF">
												<fo:table-cell border="1pt solid #333333" padding="3pt"
															   wrap-option="wrap">
													<fo:block font-size="{$font_size}pt" font-family="{$font_name}">
														<xsl:value-of select="@shortname"/>
													</fo:block>
												</fo:table-cell>

												<xsl:for-each select="/scope[1]/event">
													<fo:table-cell border="1pt solid #333333" padding="3pt"
																   wrap-option="wrap">
														<xsl:for-each select="page">
															<fo:block font-size="{$font_size}pt"
																	  font-family="{$font_name}"
																	  text-align="center"
																	  font-weight="bold">
																<xsl:if test="@shortname = $top_page">X</xsl:if>
															</fo:block>
														</xsl:for-each>
													</fo:table-cell>
												</xsl:for-each>
											</fo:table-row>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</fo:table-body>
						</fo:table>
					</fo:block>

					<fo:block/>
				</fo:flow>
			</fo:page-sequence>-->

			<!-- legend page -->
			<fo:page-sequence master-reference="standard_page" force-page-count="no-force">
				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align-last="justify" font-size="{$font_size}pt" font-family="{$font_name}" border-top="1px solid #cccccc">
						<xsl:value-of select="/study/@shortname"/>&#160;<xsl:value-of select="$document_name" /> - Version <xsl:value-of select="/study/versionNumber"/> - <xsl:value-of select="/study/versionDate"/>
						<fo:leader leader-pattern="space"/>
						Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<fo:block xsl:use-attribute-sets="page-title">Legend</fo:block>

					<fo:block id="legend">
						<fo:table table-layout="fixed" width="100%" border-collapse="separate" border-separation="3mm">
							<fo:table-column column-number="1" column-width="60mm"/>
							<fo:table-column column-number="2" column-width="120mm"/>

							<fo:table-body font-family="{$font_name}" font-size="{$font_size}pt">
								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-select" />
											[a | b | c]
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Combo box, single choice from a list of options</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-radio" />
											[a | b | c]
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Radio button, single choice from a list of options</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-checkbox" />
											[a | b | c]
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Checkboxes, multiple choices from a list of options</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-field">
												<xsl:with-param name="length" select="40"/>
											</xsl:call-template>
											(yyyy-mm-dd)
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Date field (format)</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell >
										<fo:block>
											<xsl:call-template name="image-select" />&#160;
											<xsl:call-template name="image-select" />&#160;
											<xsl:call-template name="image-select" />
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Date with selection for day, month, year</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-field">
												<xsl:with-param name="length" select="20"/>
											</xsl:call-template>
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Numeric field</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-field">
												<xsl:with-param name="length" select="40"/>
											</xsl:call-template>
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>Text field</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<fo:table-row>
									<fo:table-cell>
										<fo:block>
											<xsl:call-template name="image-file" />
										</fo:block>
									</fo:table-cell>
									<fo:table-cell>
										<fo:block>File field</fo:block>
									</fo:table-cell>
								</fo:table-row>
							</fo:table-body>
						</fo:table>
					</fo:block>
				</fo:flow>
			</fo:page-sequence>

			<!-- forms -->
			<fo:page-sequence master-reference="standard_page" initial-page-number="auto">
				<fo:static-content flow-name="xsl-region-before">
					<fo:block/>
				</fo:static-content>

				<fo:static-content flow-name="xsl-region-after">
					<fo:block text-align-last="justify" font-size="{$font_size}pt" font-family="{$font_name}" border-top="1px solid #cccccc">
						<xsl:value-of select="/study/@shortname"/>&#160;<xsl:value-of select="$document_name" /> - Version <xsl:value-of select="/study/versionNumber"/> - <xsl:value-of select="/study/versionDate"/>
						<fo:leader leader-pattern="space"/>
						Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="study"/>
					<fo:block id="last-page"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<xsl:template match="study">
		<xsl:for-each select="scopeModels/scopeModel[@id=$scopeModelId]/eventModels/eventModel">
			<fo:block font-size="{$font_size}pt" font-family="{$font_name}" id="{generate-id()}">
				<xsl:apply-templates select="."/>
			</fo:block>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="eventModel">
		<xsl:for-each select="formModels/formModel">
			<xsl:variable name="page_index" select='position()'/>
			<xsl:if test="$page_index > 1">
				<fo:block break-before="page">&#160;</fo:block>
			</xsl:if>

			<fo:block id="{generate-id()}">
				<fo:block text-align-last="justify" xsl:use-attribute-sets="page-title">
					<xsl:number format="1. " from="study/scopeModels/scopeModel[@id=$scopeModelId]" count="eventModels/eventModel" level="any"/>
					<xsl:number value="position()" format="1. "/>
					<xsl:value-of select="@shortname"/>
					<fo:leader leader-pattern="space"/>
					<xsl:value-of select="../../@shortname"/>
				</fo:block>

				<xsl:apply-templates select="layouts/layout"/>
			</fo:block>

			<xsl:if test="position() = last()">
				<fo:block break-after="page">&#160;</fo:block>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="layout">
		<fo:block space-after="5mm">
			<xsl:if test="condition">
				<fo:block xsl:use-attribute-sets="block-condition">
					<xsl:value-of select="condition"/>
				</fo:block>
			</xsl:if>

			<xsl:value-of select="textBefore"/>

			<fo:table table-layout="fixed" width="100%" space-after="5mm">
				<xsl:variable name="columns_number" select='count(columns/column)'/>
				<xsl:for-each select="columns/column">
					<xsl:variable name="index" select='position()'/>
					<fo:table-column column-number="{$index}">
						<xsl:attribute name="column-width">
							<xsl:if test="$columns_number &gt; 0">
								<xsl:value-of select="100 div $columns_number"/>%
							</xsl:if>
							<xsl:if test="$columns_number = 0">
								100%
							</xsl:if>
						</xsl:attribute>
					</fo:table-column>
				</xsl:for-each>

				<fo:table-body>
					<xsl:if test="repeatable = 'true' or condition">
						<xsl:attribute name="border">
							<xsl:value-of select="'1pt solid #333'" />
						</xsl:attribute>
					</xsl:if>

					<fo:table-row>
						<xsl:for-each select="columns/column">
							<fo:table-cell margin-right="5pt" wrap-option="wrap">
								<xsl:choose>
									<xsl:when test="label != ''">
										<fo:block padding="5pt 0 5pt 0" font-weight="bold" font-family="{$font_name}" font-size="{$font_size}pt">
											<xsl:value-of select="label"/>
										</fo:block>
									</xsl:when>
									<xsl:otherwise>
										<fo:block/>
									</xsl:otherwise>
								</xsl:choose>
								<fo:block/>
							</fo:table-cell>
						</xsl:for-each>
					</fo:table-row>

					<xsl:for-each select="lines/line">
						<fo:table-row>
							<xsl:for-each select="cell">
								<fo:table-cell margin-right="5pt" wrap-option="wrap">
									<xsl:attribute name="number-columns-spanned">
										<xsl:value-of select="colspan"/>
									</xsl:attribute>
									<xsl:if test="../../../repeatable = 'true' or ../../../condition">
										<xsl:attribute name="padding">
											<xsl:value-of select="'5pt 5pt 5pt 5pt'" />
										</xsl:attribute>
									</xsl:if>

									<fo:block padding="5pt 0 5pt 0">
										<!-- do not display the condition for empty cells -->
										<xsl:if test="condition and (fieldModelId or textBefore or textAfter)">
											<fo:block xsl:use-attribute-sets="block-condition">
												<xsl:value-of select="condition"/>
											</fo:block>
										</xsl:if>

										<xsl:apply-templates select="textBefore"/>

										<xsl:apply-templates select="fieldModel"/>

										<xsl:if test="$annotated='true' and fieldModelId">
											<fo:block xsl:use-attribute-sets="annotation">
												<xsl:value-of select="datasetModelId"/>.<xsl:value-of select="fieldModelId"/>
											</fo:block>
										</xsl:if>

										<xsl:apply-templates select="textAfter"/>
									</fo:block>
								</fo:table-cell>
							</xsl:for-each>
						</fo:table-row>
					</xsl:for-each>
				</fo:table-body>
			</fo:table>

			<xsl:if test="repeatable = 'true'">
				<fo:inline margin="5pt" padding="5pt" border="1pt solid #333">Add <xsl:value-of select="addLabel"/></fo:inline>
			</xsl:if>

			<xsl:value-of select="textAfter"/>
		</fo:block>
	</xsl:template>

	<xsl:template match="fieldModel">
		<fo:table table-layout="fixed">
			<fo:table-column column-number="1">
				<xsl:attribute name="column-width">
					<xsl:choose>
						<xsl:when test="type = 'CHECKBOX'">
							4mm
						</xsl:when>
						<xsl:when test="../displayLabel = 'true'">
							<xsl:value-of select="../labelWidth * 0.7"/>mm
						</xsl:when>
						<xsl:otherwise>0</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</fo:table-column>
			<fo:table-column column-number="2" column-width="proportional-column-width(1)"/>

			<fo:table-body>
				<fo:table-row>
					<!-- label for all attributes except checkboxes -->
					<xsl:if test="type != 'CHECKBOX'">
						<fo:table-cell margin-right="5pt" display-align="before" wrap-option="wrap">
							<fo:block font-family="{$font_name}" font-weight="normal" font-size="{$font_size}pt">
								<xsl:if test="../displayLabel = 'true'">
									<xsl:value-of select="label"/>
								</xsl:if>
							</fo:block>
						</fo:table-cell>
					</xsl:if>
					<!-- field -->
					<fo:table-cell margin-right="5pt" display-align="before" wrap-option="wrap">
						<fo:block font-family="{$font_name}" font-weight="normal" font-size="{$font_size}pt">
							<xsl:if test="type = 'DATE'">
								<xsl:call-template name="image-field">
									<xsl:with-param name="length" select="40"/>
								</xsl:call-template>
							</xsl:if>
							<xsl:if test="type = 'DATE_SELECT'">
								<xsl:if test="displayDays = 'true'">
									<xsl:call-template name="image-select" />&#160;
								</xsl:if>
								<xsl:if test="displayMonths = 'true'">
									<xsl:call-template name="image-select" />&#160;
								</xsl:if>
								<xsl:if test="displayYears = 'true'">
									<xsl:call-template name="image-select" />&#160;
								</xsl:if>

								<xsl:if test="displayDays = 'true'">dd.</xsl:if>
								<xsl:if test="displayMonths = 'true'">mm.</xsl:if>
								<xsl:if test="displayYears = 'true'">yyyy</xsl:if>
							</xsl:if>
							<xsl:if test="type = 'STRING' or type = 'AUTO_COMPLETION'">
								<xsl:call-template name="image-field">
									<xsl:with-param name="length" select="40"/>
								</xsl:call-template>
							</xsl:if>
							<xsl:if test="type = 'NUMBER'">
								<xsl:call-template name="image-field">
									<xsl:with-param name="length" select="20"/>
								</xsl:call-template>
							</xsl:if>
							<xsl:if test="type = 'TEXTAREA'">
								<xsl:call-template name="image-field">
									<xsl:with-param name="length" select="40"/>
								</xsl:call-template>
							</xsl:if>
							<xsl:if test="type = 'CHECKBOX'">
								<xsl:call-template name="image-checkbox" />
							</xsl:if>
							<xsl:if test="type = 'CHECKBOX_GROUP'">
								<xsl:call-template name="image-checkbox" />
								<xsl:if test="hasPossibleValuesProvider = 'true'">
									[<xsl:value-of select="possibleValuesProviderDescription" />]
								</xsl:if>
								<xsl:if test="hasPossibleValuesProvider = 'false'">
									[<xsl:for-each select="possibleValues/possibleValue/label">
										<xsl:if test="position() &gt; 1"> | </xsl:if>
										<xsl:apply-templates select="."/>
										<xsl:if test="../specify = 'true'">
											<xsl:call-template name="image-field">
												<xsl:with-param name="length" select="40"/>
											</xsl:call-template>
										</xsl:if>
									</xsl:for-each>]
								</xsl:if>
							</xsl:if>
							<xsl:if test="type = 'SELECT'">
								<xsl:call-template name="image-select" />&#160;
								<xsl:if test="hasPossibleValuesProvider = 'true'">
									[<xsl:value-of select="possibleValuesProviderDescription" />]
								</xsl:if>
								<xsl:if test="hasPossibleValuesProvider = 'false'">
									[<xsl:for-each select="possibleValues/possibleValue/label">
										<xsl:if test="position() &gt; 1"> | </xsl:if>
										<xsl:apply-templates select="."/>
									</xsl:for-each>]
								</xsl:if>
							</xsl:if>
							<xsl:if test="type = 'RADIO'">
								<xsl:call-template name="image-radio" />&#160;
								<xsl:if test="hasPossibleValuesProvider = 'true'">
									[<xsl:value-of select="possibleValuesProviderDescription" />]
								</xsl:if>
								<xsl:if test="hasPossibleValuesProvider = 'false'">
									[<xsl:for-each select="possibleValues/possibleValue/label">
										<xsl:if test="position() &gt; 1"> | </xsl:if>
										<xsl:apply-templates select="."/>
									</xsl:for-each>]
								</xsl:if>
							</xsl:if>
							<xsl:if test="type = 'FILE'">
								<xsl:call-template name="image-file" />
							</xsl:if>

							<xsl:if test="inlineHelp != ''">
								<fo:inline font-family="{$font_name}" font-size="{$font_size}pt">
									&#160;<xsl:value-of select="inlineHelp"/>
								</fo:inline>
							</xsl:if>
						</fo:block>
					</fo:table-cell>
					<!-- label for checkboxes -->
					<xsl:if test="type = 'CHECKBOX'">
						<fo:table-cell margin-right="5pt" display-align="before" wrap-option="wrap">
							<fo:block font-family="{$font_name}" font-weight="normal" font-size="{$font_size}pt">
								<xsl:if test="../displayLabel = 'true'">
									<xsl:value-of select="label"/>
								</xsl:if>
							</fo:block>
						</fo:table-cell>
					</xsl:if>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<xsl:template match="/" mode="toc">
		<!--<fo:block xsl:use-attribute-sets="toc-item">
			Tests and Assessments
			<fo:leader leader-pattern="dots" leader-pattern-width="1mm"/>
			<fo:page-number-citation ref-id="matrix"/>
		</fo:block>-->

		<fo:block xsl:use-attribute-sets="toc-item">
			Legend
			<fo:leader leader-pattern="dots" leader-pattern-width="1mm"/>
			<fo:page-number-citation ref-id="legend"/>
		</fo:block>

		<xsl:for-each select="study/scopeModels/scopeModel[@id=$scopeModelId]/eventModels/eventModel">
			<fo:block xsl:use-attribute-sets="toc-item">
				<fo:basic-link internal-destination="{generate-id()}">
					<xsl:number format="1. " from="study/scopeModels/scopeModel[@id=$scopeModelId]" count="eventModels/eventModel" level="any"/>
					<xsl:value-of select="@shortname"/>
					<fo:leader leader-pattern="dots" leader-pattern-width="1mm"/>
					<fo:page-number-citation ref-id="{generate-id()}"/>
				</fo:basic-link>
			</fo:block>

			<xsl:for-each select="formModels/formModel">
				<fo:block margin-left="5mm" xsl:use-attribute-sets="toc-item">
					<fo:basic-link internal-destination="{generate-id()}">
						<xsl:number format="1." from="study/scopeModels/scopeModel[@id=$scopeModelId]" count="eventModels/eventModel" level="any"/>
						<xsl:number level="multiple" value="position()" format="1. "/>
						<xsl:value-of select="@shortname"/>
						<fo:leader leader-pattern="dots" leader-pattern-width="1mm"/>
						<fo:page-number-citation ref-id="{generate-id()}"/>
					</fo:basic-link>
				</fo:block>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="fieldset">
		<xsl:apply-templates select="legend"/>
	</xsl:template>

	<xsl:template match="legend">
		<fo:block font-weight="bold" border-bottom="1pt solid #dedede">
			<xsl:apply-templates select="*|text()"/>&#160;
		</fo:block>
	</xsl:template>

	<xsl:template match="strong">
		<fo:inline font-weight="bold">
			<xsl:apply-templates select="*|text()"/>
		</fo:inline>
	</xsl:template>

	<xsl:template match="br">
		<fo:block />
	</xsl:template>

	<xsl:template match="li">
		<fo:block>
			• <xsl:apply-templates select="*|text()"/>
		</fo:block>
	</xsl:template>

</xsl:stylesheet>
