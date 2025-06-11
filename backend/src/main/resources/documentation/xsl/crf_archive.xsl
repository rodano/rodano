<?xml version="1.1" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:java="http://xml.apache.org/xslt/java"
				exclude-result-prefixes="java">
	<xsl:output indent="yes"/>
	<xsl:decimal-format name="european" decimal-separator="." grouping-separator="&#xa0;" NaN="0.00"/>

	<xsl:param name="audit_trail"/>
	<xsl:param name="generation_time"/>

	<!-- global variables -->
	<xsl:variable name="page_width">297</xsl:variable>
	<xsl:variable name="page_height">210</xsl:variable>

	<xsl:variable name="font_name">Helvetica</xsl:variable>
	<xsl:variable name="font_size">9</xsl:variable>

	<xsl:variable name="value_color">blue</xsl:variable>
	<xsl:variable name="deleted_color">#efefef</xsl:variable>

	<xsl:template name="image-checkbox">
		<xsl:param name="checked"/>
		<fo:instream-foreign-object xmlns:svg="http://www.w3.org/2000/svg">
			<svg:svg width="10" height="10">
				<svg:rect x="0.5" y="0.5" width="9" height="9" style="stroke: {$value_color}; fill: #fff;"/>
				<xsl:if test="$checked">
					<svg:path d="M 1 6 L 2 5 L 4 7 L 8 2 L 9 3 L 4 9" style="fill: {$value_color};"/>
				</xsl:if>
			</svg:svg>
		</fo:instream-foreign-object>
	</xsl:template>

	<xsl:template name="header">
		<fo:static-content flow-name="xsl-region-before">
			<fo:block font-size="{$font_size -1}pt" font-family="{$font_name}" border-bottom="1px solid #ccc">
				<fo:block text-align-last="justify">
					Sponsor: <xsl:value-of select="/study/client"/>
					<fo:leader leader-pattern="space"/>
					Rodano
				</fo:block>
				<fo:block font-size="{$font_size -1}pt" font-family="{$font_name}">
					Study: <xsl:value-of select="/study/@shortname"/>
				</fo:block>
			</fo:block>
		</fo:static-content>
	</xsl:template>

	<xsl:template name="footer">
		<fo:static-content flow-name="xsl-region-after">
			<fo:block text-align-last="justify" font-size="{$font_size -1}pt" font-family="{$font_name}" border-top="1px solid #ccc">
				Document generated on <xsl:value-of select="$generation_time"/>
				<fo:leader leader-pattern="space"/>
				Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
			</fo:block>
			<fo:block font-size="{$font_size -1}pt" font-family="{$font_name}">
				<xsl:value-of select="/study/url"/>&#160;-&#160;<xsl:value-of select="/study/scope/@scopeModelLabel"/>&#160;<xsl:value-of select="/study/scope/@code"/>
			</fo:block>
		</fo:static-content>
	</xsl:template>

	<xsl:attribute-set name="audit-trail-cell">
		<xsl:attribute name="wrap-option">wrap</xsl:attribute>
		<xsl:attribute name="padding">2pt</xsl:attribute>
		<xsl:attribute name="border">1pt solid #666</xsl:attribute>
	</xsl:attribute-set>

	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" language="en">
			<!-- master pages -->
			<fo:layout-master-set>
				<fo:simple-page-master master-name="main"
						page-height="{$page_height}mm"
						page-width="{$page_width}mm"
						margin-top="5mm"
						margin-bottom="5mm"
						margin-left="5mm"
						margin-right="5mm">

					<fo:region-body margin-top="10mm" margin-bottom="10mm" margin-left="0" margin-right="0"/>
					<fo:region-before extent="0"/>
					<fo:region-after extent="5mm"/>
					<fo:region-start extent="0"/>
					<fo:region-end extent="0"/>
				</fo:simple-page-master>
			</fo:layout-master-set>

			<fo:bookmark-tree>
				<xsl:for-each select="/study/scope/event">
					<fo:bookmark internal-destination="{generate-id()}">
						<!-- keep this in one line -->
						<fo:bookmark-title font-weight="bold">
							<xsl:value-of select="../@code"/> - <xsl:value-of select="@shortname"/><xsl:if test="@deleted = 'true'">&#160;(Removed)</xsl:if>
						</fo:bookmark-title>
						<xsl:if test="@deleted = 'false' or @expected = 'false'">
							<xsl:for-each select="form">
								<fo:bookmark internal-destination="{generate-id()}">
									<!-- keep this in one line -->
									<fo:bookmark-title>
										<xsl:number level="multiple" value="position()" format="1. "/><xsl:value-of select="@shortname"/>
									</fo:bookmark-title>
								</fo:bookmark>
							</xsl:for-each>
						</xsl:if>
					</fo:bookmark>
				</xsl:for-each>
				<xsl:for-each select="/study/scope/workflow">
					<fo:bookmark internal-destination="{generate-id()}">
						<!-- keep this in one line -->
						<fo:bookmark-title>
							<xsl:value-of select="../@code"/> - Signature
						</fo:bookmark-title>
					</fo:bookmark>
				</xsl:for-each>
			</fo:bookmark-tree>

			<!-- pages content -->
			<!-- first page -->
			<fo:page-sequence master-reference="main" initial-page-number="auto">
				<xsl:call-template name="header" />
				<xsl:call-template name="footer" />

				<fo:flow flow-name="xsl-region-body">
					<fo:block-container position="absolute" top="40mm" left="10mm" height="100mm" width="287mm">
						<fo:block font-size="22pt" font-family="{$font_name}" text-align="center">
							<xsl:value-of select="/study/@longname"/>
						</fo:block>
						<fo:block font-size="22pt" font-family="{$font_name}" text-align="center">
							<xsl:value-of select="/study/client"/>
						</fo:block>

						<fo:block margin-top="20mm" font-size="22pt" font-family="{$font_name}" text-align="center">
							<xsl:value-of select="/study/scope/@scopeModelLabel"/>:&#160;<xsl:value-of select="/study/scope/@code"/>
						</fo:block>

						<fo:block margin-top="140mm" font-size="16pt" font-family="{$font_name}" text-align="left">
							<fo:block>
								<xsl:value-of select="/study/scope/@parentScopeModelLabel"/>&#160;code:&#160;<xsl:value-of select="/study/scope/@parentCode"/>
							</fo:block>
							<fo:block margin-top="5mm">
								<xsl:value-of select="/study/scope/@parentScopeModelLabel"/>&#160;name:&#160;<xsl:value-of select="/study/scope/@parentShortname"/>
							</fo:block>
						</fo:block>
					</fo:block-container>
				</fo:flow>
			</fo:page-sequence>

			<!-- table of content page -->
			<fo:page-sequence master-reference="main" initial-page-number="auto">
				<xsl:call-template name="header" />
				<xsl:call-template name="footer" />

				<fo:flow flow-name="xsl-region-body">
					<fo:block-container top="0" left="0" height="20mm" width="{$page_width}mm" position="absolute">
						<fo:block font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 2}pt">Table of contents</fo:block>
					</fo:block-container>
					<fo:block space-before="1mm" space-after="3mm">
						<fo:leader leader-length="100%" leader-pattern="rule" leader-pattern-width="2pt"/>
					</fo:block>
					<fo:block text-align="justify" font-size="{$font_size}pt" font-family="{$font_name}">
						<xsl:apply-templates select="/" mode="toc"/>
					</fo:block>
					<fo:block/>
				</fo:flow>
			</fo:page-sequence>

			<!-- ecrf page -->
			<fo:page-sequence master-reference="main" initial-page-number="auto">
				<xsl:call-template name="header" />
				<xsl:call-template name="footer" />

				<fo:flow flow-name="xsl-region-body">
					<xsl:apply-templates select="study"/>
					<fo:block id="last-page"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<!-- scope template -->
	<xsl:template match="study">
		<xsl:for-each select="scope">
			<!-- events -->
			<xsl:for-each select="event">
				<fo:block font-size="{$font_size}pt" font-family="{$font_name}" id="{generate-id()}">
					<xsl:apply-templates select="."/>
				</fo:block>
			</xsl:for-each>
			<!-- workflows -->
			<xsl:for-each select="workflow">
				<fo:block font-size="{$font_size}pt" font-family="{$font_name}" id="{generate-id()}">
					<xsl:apply-templates select="."/>
				</fo:block>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

	<!-- workflow template -->
	<xsl:template match="workflow">
		<fo:block break-before="page">&#160;</fo:block>
		<fo:block text-align-last="justify" font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 2}pt">
			<xsl:value-of select="../@code"/>
			<fo:leader leader-pattern="space"/>Signature
		</fo:block>
		<fo:block space-before="-3mm" space-after="3mm">
			<fo:leader leader-length="100%" leader-pattern="rule" leader-pattern-width="2pt"/>
		</fo:block>

		<xsl:apply-templates select="signatureText"/>

		<fo:table table-layout="fixed" width="100%" space-before="5mm" space-after="5mm">
			<fo:table-column column-number="1" column-width="40mm"/>
			<fo:table-column column-number="2" column-width="40mm"/>
			<fo:table-column column-number="3" column-width="30mm"/>
			<fo:table-column column-number="4"/>

			<fo:table-body font-family="{$font_name}" font-size="{$font_size - 1}pt">
				<fo:table-row font-weight="bold" background-color="#efefef">
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>User</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Status</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Date (UTC)</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block >Message</fo:block></fo:table-cell>
				</fo:table-row>
				<xsl:for-each select="trails/trail">
					<fo:table-row>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="user"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="status"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="date"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="message"/></fo:block></fo:table-cell>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<!-- event template -->
	<xsl:template match="event">
		<fo:block text-align-last="justify" font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 2}pt">
			<xsl:value-of select="../@code"/>
			<fo:leader leader-pattern="space"/>
			<xsl:value-of select="@shortname"/>
			<xsl:if test="@deleted = 'true'">&#160;(Removed)</xsl:if>
		</fo:block>
		<fo:block space-before="-3mm" space-after="3mm">
			<fo:leader leader-length="100%" leader-pattern="rule" leader-pattern-width="2pt"/>
		</fo:block>

		<xsl:if test="$audit_trail='true'">
			<fo:block font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 1}pt">Event audit trails</fo:block>
			<xsl:apply-templates select="trails" />
		</xsl:if>

		<xsl:if test="@deleted = 'false' or @expected = 'false'">
			<xsl:for-each select="form">
				<xsl:if test="position() > 1">
					<fo:block break-before="page">&#160;</fo:block>
				</xsl:if>

				<fo:block id="{generate-id()}">
					<xsl:variable name="pageId">
						<xsl:value-of select="."/>
					</xsl:variable>

					<!--<fo:block text-align-last="justify" font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 2}pt">
						<xsl:value-of select="../../@code"/>
						<fo:leader leader-pattern="space"/>
						<xsl:value-of select="../@shortname"/>
						<xsl:if test="../@deleted = 'true'">Removed</xsl:if>
					</fo:block>
					<fo:block space-before="-3mm" space-after="3mm">
						<fo:leader leader-length="100%" leader-pattern="rule" leader-pattern-width="2pt"/>
					</fo:block>-->

					<fo:block text-align-last="justify" padding="2pt" font-family="{$font_name}" font-weight="bold" font-size="{$font_size}pt" background-color="#dedede" space-after="5mm">
						<xsl:number value="position()" format="1. "/>
						<xsl:value-of select="@shortname"/>
						<xsl:if test="@deleted = 'true'">&#160;(Removed)</xsl:if>
						<!-- repeat event title before each forms -->
						<fo:leader leader-pattern="space"/>
						<xsl:value-of select="../@shortname"/>
						<xsl:if test="../@deleted = 'true'">&#160;(Removed)</xsl:if>
					</fo:block>

					<xsl:if test="$audit_trail='true'">
						<fo:block font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 1}pt">Form audit trails</fo:block>
						<xsl:apply-templates select="trails" />
					</xsl:if>

					<fo:block>
						<!-- change background for deleted events or deleted forms -->
						<xsl:if test="../@deleted = 'true' or @deleted = 'true'">
							<xsl:attribute name="background-color"><xsl:value-of select="$deleted_color"/></xsl:attribute>
						</xsl:if>
						<xsl:apply-templates select="layoutGroup"/>
					</fo:block>

					<xsl:if test="$audit_trail='true'">
						<xsl:if test="count(layoutGroup[@type='MULTIPLE']) > 0">
							<xsl:apply-templates select="layoutGroup/layouts/layout/trails" />
						</xsl:if>

						<fo:block font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 1}pt">Fields audit trails</fo:block>
						<xsl:apply-templates select="layoutGroup/layouts/layout/lines/line/cell/field/trails" />
					</xsl:if>
				</fo:block>

				<xsl:if test="position() = last()">
					<fo:block break-after="page">&#160;</fo:block>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<!-- audit trails template -->
	<xsl:template match="trails">
		<fo:table table-layout="fixed" width="100%" space-after="5mm">
			<!-- change background for deleted elements -->
			<xsl:if test="../@deleted = 'true'">
				<xsl:attribute name="background-color"><xsl:value-of select="$deleted_color"/></xsl:attribute>
			</xsl:if>
			<fo:table-column column-width="30mm"/>
			<fo:table-column column-width="55mm"/>
			<fo:table-column />
			<fo:table-body font-family="{$font_name}" font-weight="bold" font-size="{$font_size - 1}pt">
				<fo:table-row font-weight="bold" background-color="#efefef">
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Date (UTC)</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>By</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Message</fo:block></fo:table-cell>
				</fo:table-row>
				<xsl:for-each select="trail">
					<fo:table-row>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="date"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="user"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="message"/></fo:block></fo:table-cell>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<!-- layout audit trail template -->
	<xsl:template match="layout/trails">
		<fo:block font-family="{$font_name}" font-weight="bold" font-size="{$font_size + 1}pt">Multiple records audit trails</fo:block>
		<fo:table table-layout="fixed" width="100%" space-after="5mm">
			<!-- change background for deleted layout -->
			<xsl:if test="../@deleted = 'true'">
				<xsl:attribute name="background-color"><xsl:value-of select="$deleted_color"/></xsl:attribute>
			</xsl:if>
			<fo:table-column column-width="8mm"/>
			<fo:table-column column-width="30mm"/>
			<fo:table-column column-width="55mm"/>
			<fo:table-column />
			<fo:table-body font-family="{$font_name}" font-size="{$font_size - 1}pt">
				<fo:table-row font-weight="bold" background-color="#efefef">
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Id</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Date (UTC)</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>By</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Message</fo:block></fo:table-cell>
				</fo:table-row>
				<xsl:for-each select="trail">
					<fo:table-row>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="../../@index"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="date"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="user"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="message"/></fo:block></fo:table-cell>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<!-- layout group template -->
	<xsl:template match="layoutGroup">
		<!-- normal layout group -->
		<xsl:if test="not(@type = 'GRID')">
			<xsl:for-each select="layouts/layout">
				<xsl:if test="../../@type = 'MULTIPLE'">
					<fo:block border-bottom="1pt solid #666" font-weight="bold">
						Id
						<xsl:value-of select="@index"/>
						<xsl:if test="@deleted = 'true'">&#160;Removed</xsl:if>
					</fo:block>
				</xsl:if>
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<!-- layout template -->
	<xsl:template match="layout">
		<fo:table table-layout="fixed" width="100%" space-after="5mm">
			<!-- change background for deleted layouts -->
			<xsl:if test="@deleted = 'true'">
				<xsl:attribute name="background-color"><xsl:value-of select="$deleted_color"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each select="columns/column">
				<xsl:variable name="index" select='position()'/>
				<xsl:choose>
					<xsl:when test="position() = last()">
						<fo:table-column column-number="{$index}" column-width="proportional-column-width(1)"/>
					</xsl:when>
					<xsl:otherwise>
						<fo:table-column column-number="{$index}">
							<xsl:attribute name="column-width"><xsl:value-of select="width"/>mm</xsl:attribute>
						</fo:table-column>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>

			<fo:table-body>
				<fo:table-row>
					<xsl:for-each select="columns/column">
						<fo:table-cell margin-right="5pt" wrap-option="wrap">
							<xsl:choose>
								<xsl:when test="label != ''">
									<fo:block font-weight="bold" font-family="{$font_name}" font-size="{$font_size - 1}pt">
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
								<fo:block font-family="{$font_name}" font-size="{$font_size - 1}pt" padding="2pt">
									<xsl:if test="@visible = 'true'">
										<xsl:apply-templates select="textBefore"/>
										<fo:block/>
										<xsl:apply-templates select="field"/>
										<fo:block/>
										<xsl:apply-templates select="textAfter"/>
									</xsl:if>
								</fo:block>
							</fo:table-cell>
						</xsl:for-each>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<!-- field template -->
	<xsl:template match="field">
		<fo:table table-layout="fixed" width="100%" space-after="0">
			<fo:table-column column-number="1">
				<xsl:attribute name="column-width">
					<xsl:choose>
						<xsl:when test="../displayLabel = 'true'"><xsl:value-of select="../width"/>mm</xsl:when>
						<xsl:otherwise>0</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</fo:table-column>

			<fo:table-column column-number="2" column-width="proportional-column-width(1)"/>
			<fo:table-body>
				<fo:table-row>
					<xsl:if test="@type = 'CHECKBOX'">
						<fo:table-cell padding="2pt" margin-right="5pt" wrap-option="wrap">
							<fo:block>
								<!-- value -->
								<xsl:call-template name="image-checkbox">
									<xsl:with-param name="checked" select="@value = 'true'"/>
								</xsl:call-template>
								<!-- label -->
								<xsl:if test="../displayLabel = 'true'">
									&#160;<xsl:value-of select="@label"/>
								</xsl:if>
							</fo:block>
						</fo:table-cell>
					</xsl:if>
					<xsl:if test="@type != 'CHECKBOX'">
						<!-- label -->
						<fo:table-cell padding="2pt" margin-right="5pt" wrap-option="wrap">
							<fo:block>
								<xsl:if test="../displayLabel = 'true'">
									<xsl:value-of select="@label"/>
								</xsl:if>
							</fo:block>
						</fo:table-cell>
						<!-- value -->
						<fo:table-cell padding="2pt" wrap-option="wrap">
							<fo:block>
								<fo:inline font-weight="bold" color="{$value_color}">
									<xsl:choose>
										<xsl:when test="not(@value) or @value = ''">
											_______
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="@value"/>
										</xsl:otherwise>
									</xsl:choose>
								</fo:inline>
								<xsl:if test="@inlineHelp != ''">
									&#160;<xsl:value-of select="@inlineHelp"/>
								</xsl:if>
							</fo:block>
						</fo:table-cell>
					</xsl:if>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<!-- field trail template -->
	<xsl:template match="field/trails">
		<fo:block font-family="{$font_name}" font-size="{$font_size}pt"><xsl:value-of select="../@label"/></fo:block>
		<fo:table table-layout="fixed" width="100%" space-after="5mm">
			<!-- change background for deleted events -->
			<xsl:if test="../../../../@deleted = 'true' or ../../../../../../../../@deleted = 'true'">
				<xsl:attribute name="background-color"><xsl:value-of select="$deleted_color"/></xsl:attribute>
			</xsl:if>
			<fo:table-column column-width="8mm"/>
			<fo:table-column column-width="30mm"/>
			<fo:table-column column-width="30mm"/>
			<fo:table-column column-width="30mm"/>
			<fo:table-column column-width="30mm"/>
			<fo:table-column column-width="55mm"/>
			<fo:table-column />
			<fo:table-body font-family="{$font_name}" font-size="{$font_size - 1}pt">
				<fo:table-row font-weight="bold" background-color="#efefef">
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Id</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Date (UTC)</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Value</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Workflow</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Status</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>By</fo:block></fo:table-cell>
					<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block>Message</fo:block></fo:table-cell>
				</fo:table-row>
				<xsl:for-each select="trail">
					<fo:table-row>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="../../../../../@index"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="date"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="value"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="workflow"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="state"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="user"/></fo:block></fo:table-cell>
						<fo:table-cell xsl:use-attribute-sets="audit-trail-cell"><fo:block><xsl:value-of select="message"/></fo:block></fo:table-cell>
					</fo:table-row>
				</xsl:for-each>
			</fo:table-body>
		</fo:table>
	</xsl:template>

	<xsl:template match="/" mode="toc">
		<xsl:for-each select="/study/scope">
			<xsl:for-each select="event">
				<fo:block space-before.optimum="6pt" text-align-last="justify" font-family="{$font_name}" font-size="{$font_size - 1}pt">
					<fo:basic-link internal-destination="{generate-id()}">
						<xsl:value-of select="../@code"/> - <xsl:value-of select="@shortname"/><xsl:if test="@deleted = 'true'">&#160;(Removed)</xsl:if>
						<fo:leader leader-pattern="dots" leader-pattern-width="1mm"/>
						<fo:page-number-citation ref-id="{generate-id()}"/>
					</fo:basic-link>
				</fo:block>

				<xsl:if test="@deleted = 'false' or @expected = 'false'">
					<xsl:for-each select="form">
						<fo:block margin-left="5mm" space-before.optimum="6pt" text-align-last="justify" font-family="{$font_name}" font-size="{$font_size - 1}pt">
							<fo:basic-link internal-destination="{generate-id()}">
								<xsl:number level="multiple" value="position()" format="1. "/><xsl:value-of select="@shortname"/><xsl:if test="@deleted = 'true'">&#160;(Removed)</xsl:if>
								<fo:leader leader-pattern="dots" leader-pattern-width="1mm"/>
								<fo:page-number-citation ref-id="{generate-id()}"/>
							</fo:basic-link>
						</fo:block>
					</xsl:for-each>
				</xsl:if>
			</xsl:for-each>

			<xsl:for-each select="workflow">
				<fo:block space-before.optimum="6pt" text-align-last="justify" font-family="{$font_name}" font-size="{$font_size - 1}pt">
					<fo:basic-link internal-destination="{generate-id()}">
						<xsl:value-of select="../@code"/> - Signature
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
		<fo:block font-weight="bold" border-bottom="1pt solid #dedede" space-after="5mm" space-before="5mm">
			&#160;
			<xsl:apply-templates select="*|text()"/>&#160;
		</fo:block>
	</xsl:template>

	<xsl:template match="strong">
		<fo:inline font-weight="bold">
			<xsl:apply-templates select="*|text()"/>
		</fo:inline>
	</xsl:template>

	<xsl:template match="p">
		<fo:block>
			<xsl:apply-templates select="*|text()"/>
		</fo:block>
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
