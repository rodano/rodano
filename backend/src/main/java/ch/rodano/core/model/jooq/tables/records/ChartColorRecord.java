/*
 * This file is generated by jOOQ.
 */
package ch.rodano.core.model.jooq.tables.records;


import ch.rodano.core.model.jooq.tables.ChartColor;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ChartColorRecord extends UpdatableRecordImpl<ChartColorRecord> {

	private static final long serialVersionUID = 1L;

	/**
	 * Setter for <code>chart_color.pk</code>.
	 */
	public void setPk(Long value) {
		set(0, value);
	}

	/**
	 * Getter for <code>chart_color.pk</code>.
	 */
	public Long getPk() {
		return (Long) get(0);
	}

	/**
	 * Setter for <code>chart_color.chart_fk</code>.
	 */
	public void setChartFk(Long value) {
		set(1, value);
	}

	/**
	 * Getter for <code>chart_color.chart_fk</code>.
	 */
	public Long getChartFk() {
		return (Long) get(1);
	}

	/**
	 * Setter for <code>chart_color.color_order</code>.
	 */
	public void setColorOrder(Integer value) {
		set(2, value);
	}

	/**
	 * Getter for <code>chart_color.color_order</code>.
	 */
	public Integer getColorOrder() {
		return (Integer) get(2);
	}

	/**
	 * Setter for <code>chart_color.color</code>.
	 */
	public void setColor(String value) {
		set(3, value);
	}

	/**
	 * Getter for <code>chart_color.color</code>.
	 */
	public String getColor() {
		return (String) get(3);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	@Override
	public Record1<Long> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ChartColorRecord
	 */
	public ChartColorRecord() {
		super(ChartColor.CHART_COLOR);
	}

	/**
	 * Create a detached, initialised ChartColorRecord
	 */
	public ChartColorRecord(Long pk, Long chartFk, Integer colorOrder, String color) {
		super(ChartColor.CHART_COLOR);

		setPk(pk);
		setChartFk(chartFk);
		setColorOrder(colorOrder);
		setColor(color);
		resetChangedOnNotNull();
	}
}
