package ch.rodano.core.model.graph.highchart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class HighChartColumn {
	private int borderWidth = 0;
	private int pointPadding = 0;
	private String stacking;
	private boolean shadow = false;
	private boolean animation = false;

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(final int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public int getPointPadding() {
		return pointPadding;
	}

	public void setPointPadding(final int pointPadding) {
		this.pointPadding = pointPadding;
	}

	public String getStacking() {
		return stacking;
	}

	public void setStacking(final String stacking) {
		this.stacking = stacking;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(final boolean shadow) {
		this.shadow = shadow;
	}

	public boolean isAnimation() {
		return animation;
	}

	public void setAnimation(final boolean animation) {
		this.animation = animation;
	}
}
