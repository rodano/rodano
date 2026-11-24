<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html" version="1.1" omit-xml-declaration="yes" encoding="utf-8" indent="yes" />

	<xsl:template match="testsuites">
		<html>
			<head>
				<meta charset="UTF-8" />
				<title>
					<xsl:value-of select="@name" />-<xsl:value-of select="@version" />
				</title>
				<link href="https://fonts.googleapis.com/css?family=Inconsolata|Open+Sans:400,700" rel="stylesheet" />
				<style type="text/css">
					html {
						font-family: 'Open Sans';
						font-size: 80%;
					}
					h1, h2 {
						margin: 5px 0;
						padding: 2px;
						border: 1px solid #ddd;
					}
					h1 > span, h2 > span {
						margin-left: 20px;
						font-size: 0.9rem;
					}
					ul {
						list-style-type: none;
						border: 1px solid #ddd;
						padding: 0;
						margin: 2px 0 0 0;
					}
					ul:empty {
						display: none;
					}
					li {
						padding: 2px;
					}
					li:before {
						vertical-align: top;
						margin-right: 5px;
					}
					li.success:before {
						content: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAABFFBMVEX///8ydS0zdi40dy41eC81eTA2ejE3ezE4fDI5fTM6fjRltFo2ejFAhTlJkkJTnktcqVNqul9qu19puV5nt1xWoU1frFVJkUE/hDhSnUpRnElSnUpTnktUn0tVoExWoU1Xok5mtVtmtlw3ezE7gDU8gTY9gjdaplE6fzQ4fDI5fTM6fjQ9gzdAhTlHjkBRmklXn09ZoFFaolJaplFco1Nco1RdpFRfpVZgpldhp1hjsFlkrFtnrVxorl5pr19qsGBqsmBssmJts2NttGNttWN2vWt5wG1+yHSByXaBynaCyneCy3iDyXiFyHqFyXqFy3mGyXmGzHyHzHuHzXyJzX2Jzn6Kzn+Lz3+NzoCNz4GP0YKQ0oQEHHwSAAAALHRSTlMABwcICAgICAgICAkKCgoKCjxDRmVvb3l7f4uRkZGRkZHk5vv8/Pz8/f7+/nNpYg8AAACHSURBVHgBYyANKEii8uWd7MSR+XKOUb4WEgi+rH1kuKUuF5AlJALiy9iFhVqocQJZgkpuYgwM0rYhweaqHEC+sGJAhLqolE2gv5kKO0gpt5VrtJ+Ds4+3iQYbxDABY5fQIE8PI21WmPH8+tZe7oZaLAgL+fRMDTSZkZ3Eq6PMhOpoHkbcHgQAwj0RJsiLRogAAAAASUVORK5CYII=);
					}
					li.fail:before {
						content: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABGdBTUEAAK/INwWK6QAAABl0RVh0U29mdHdhcmUAQWRvYmUgSW1hZ2VSZWFkeXHJZTwAAAIsSURBVDjLpVNLSJQBEP7+h6uu62vLVAJDW1KQTMrINQ1vPQzq1GOpa9EppGOHLh0kCEKL7JBEhVCHihAsESyJiE4FWShGRmauu7KYiv6Pma+DGoFrBQ7MzGFmPr5vmDFIYj1mr1WYfrHPovA9VVOqbC7e/1rS9ZlrAVDYHig5WB0oPtBI0TNrUiC5yhP9jeF4X8NPcWfopoY48XT39PjjXeF0vWkZqOjd7LJYrmGasHPCCJbHwhS9/F8M4s8baid764Xi0Ilfp5voorpJfn2wwx/r3l77TwZUvR+qajXVn8PnvocYfXYH6k2ioOaCpaIdf11ivDcayyiMVudsOYqFb60gARJYHG9DbqQFmSVNjaO3K2NpAeK90ZCqtgcrjkP9aUCXp0moetDFEeRXnYCKXhm+uTW0CkBFu4JlxzZkFlbASz4CQGQVBFeEwZm8geyiMuRVntzsL3oXV+YMkvjRsydC1U+lhwZsWXgHb+oWVAEzIwvzyVlk5igsi7DymmHlHsFQR50rjl+981Jy1Fw6Gu0ObTtnU+cgs28AKgDiy+Awpj5OACBAhZ/qh2HOo6i+NeA73jUAML4/qWux8mt6NjW1w599CS9xb0mSEqQBEDAtwqALUmBaG5FV3oYPnTHMjAwetlWksyByaukxQg2wQ9FlccaK/OXA3/uAEUDp3rNIDQ1ctSk6kHh1/jRFoaL4M4snEMeD73gQx4M4PsT1IZ5AfYH68tZY7zv/ApRMY9mnuVMvAAAAAElFTkSuQmCC);
					}
					.description {
						padding-left: 20px;
						font-weight: bold;
					}
					.failure {
						margin-left: 50px;
						font-size: 0.9rem;
					}
					pre {
						font-size: 0.8rem;
					}
					code {
						font-family: 'Inconsolata';
					}
					.success {
						background-color: #d1eed1;
						border-color: #bfe7bf;
					}
					.fail {
						background-color: #fddfde;
						border-color: #fbc7c6;
					}
				</style>
			</head>
			<body>
				<h1>
					<xsl:if test="@failures = 0">
						<xsl:attribute name="class">success</xsl:attribute>
					</xsl:if>
					<xsl:if test="@failures != 0">
						<xsl:attribute name="class">fail</xsl:attribute>
					</xsl:if>
					Report <xsl:value-of select="@name" />
					<span>v<xsl:value-of select="@version" /></span>
					<span><xsl:value-of select="@tests - @failures" />/<xsl:value-of select="@tests" /> tests</span>
					<span><xsl:value-of select="@time" />s</span>
				</h1>
				<xsl:if test="count(./properties) != 0">
					<p>
						Run on <xsl:value-of select="@hostname" /> <span style="font-size: 9px;">(<xsl:value-of select="@hostname" />)</span> at <xsl:value-of select="report/date" /> UTC
					</p>
				</xsl:if>
				<xsl:apply-templates select="testsuite" />
			</body>
		</html>
	</xsl:template>

	<xsl:template match="testsuite">
		<div>
			<h2>
				<xsl:if test="count(./testcase/failure) = 0">
					<xsl:attribute name="class">success</xsl:attribute>
				</xsl:if>
				<xsl:if test="count(./testcase/failure) != 0">
					<xsl:attribute name="class">fail</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="@name" />
				<span><xsl:value-of select="@tests - @failures" />/<xsl:value-of select="@tests" /> tests</span>
				<span><xsl:value-of select="@timestamp" /></span>
				<span><xsl:value-of select="@time" />s</span>
			</h2>
			<ul>
				<xsl:apply-templates select="testcase"/>
			</ul>
		</div>
	</xsl:template>

	<xsl:template match="testcase">
		<li>
			<xsl:if test="count(./failure) = 0">
				<xsl:attribute name="class">success</xsl:attribute>
			</xsl:if>
			<xsl:if test="count(./failure) != 0">
				<xsl:attribute name="class">fail</xsl:attribute>
			</xsl:if>
			<xsl:value-of select="@name" />
			<span class="description">
				<xsl:value-of select="@description" />
			</span>
			<xsl:apply-templates select="failure" />
		</li>
	</xsl:template>

	<xsl:template match="failure">
		<div class="failure">
			<xsl:value-of select="@message" />
			<xsl:if test="../system-err">
				<pre>
					System error output:
					<code><xsl:value-of select="../system-err" /></code>
				</pre>
			</xsl:if>
			<xsl:if test="../system-out">
				<pre>
					System standard output:
					<code><xsl:value-of select="../system-out" /></code>
				</pre>
			</xsl:if>
		</div>
	</xsl:template>

</xsl:stylesheet>
