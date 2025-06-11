package ch.rodano.core.model.graph.highchart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartLegend {
	private boolean enabled;
	private String layout = "horizontal";
	private String backgroundColor = "#FFFFFF";
	private String verticalAlign = "top";
	private String align = "center";
	private Integer y;
	private boolean floating = true;
	private boolean shadow = false;
	private short borderRadius = 0;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(final String layout) {
		this.layout = layout;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(final String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getVerticalAlign() {
		return verticalAlign;
	}

	public void setVerticalAlign(final String verticalAlign) {
		this.verticalAlign = verticalAlign;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(final String align) {
		this.align = align;
	}

	public Integer getY() {
		return y;
	}

	public void setY(final Integer y) {
		this.y = y;
	}

	public boolean isFloating() {
		return floating;
	}

	public void setFloating(final boolean floating) {
		this.floating = floating;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(final boolean shadow) {
		this.shadow = shadow;
	}

	public short getBorderRadius() {
		return borderRadius;
	}

	public void setBorderRadius(final short borderRadius) {
		this.borderRadius = borderRadius;
	}
}
