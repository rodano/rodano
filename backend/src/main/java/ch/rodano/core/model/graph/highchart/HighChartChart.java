package ch.rodano.core.model.graph.highchart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartChart {
	private String renderTo;
	private String type;
	private boolean inverted;
	private boolean showAxes;
	private boolean animation;

	private int marginTop;
	private int marginBottom;
	private int marginLeft;
	private int marginRight;

	private String className;
	private String backgroundColor;
	private int plotBorderWidth;
	private String plotBackgroundColor;
	private String plotBorderColor;

	public HighChartChart() {
		showAxes = true;
		animation = false;

		className = "chartArea";
		plotBorderWidth = 1;
		plotBackgroundColor = "#FFFFFF";
		plotBorderColor = "#999999";
	}

	public String getRenderTo() {
		return renderTo;
	}

	public void setRenderTo(final String renderTo) {
		this.renderTo = renderTo;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(final boolean inverted) {
		this.inverted = inverted;
	}

	public boolean isShowAxes() {
		return showAxes;
	}

	public void setShowAxes(final boolean showAxes) {
		this.showAxes = showAxes;
	}

	public boolean isAnimation() {
		return animation;
	}

	public void setAnimation(final boolean animation) {
		this.animation = animation;
	}

	public int getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(final int marginTop) {
		this.marginTop = marginTop;
	}

	public int getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(final int marginBottom) {
		this.marginBottom = marginBottom;
	}

	public int getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(final int marginLeft) {
		this.marginLeft = marginLeft;
	}

	public int getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(final int marginRight) {
		this.marginRight = marginRight;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(final String className) {
		this.className = className;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getPlotBorderWidth() {
		return plotBorderWidth;
	}

	public void setPlotBorderWidth(final int plotBorderWidth) {
		this.plotBorderWidth = plotBorderWidth;
	}

	public String getPlotBackgroundColor() {
		return plotBackgroundColor;
	}

	public void setPlotBackgroundColor(final String plotBackgroundColor) {
		this.plotBackgroundColor = plotBackgroundColor;
	}

	public String getPlotBorderColor() {
		return plotBorderColor;
	}

	public void setPlotBorderColor(final String plotBorderColor) {
		this.plotBorderColor = plotBorderColor;
	}
}
