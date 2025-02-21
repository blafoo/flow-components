/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Style;

/**
 * HTML labels that can be positioned anywhere in the chart area.
 */
@SuppressWarnings("serial")
public class HTMLLabels extends AbstractConfigurationObject {
    private HTMLLabelItem[] items;
    private Style style;

    public HTMLLabels() {
    }

    /**
     * Constructs a HTMLLabels instance with the given label items
     *
     * @param items
     */
    public HTMLLabels(HTMLLabelItem... items) {
        setItems(items);
    }

    /**
     * Constructs a HTMLLabels instance with a style shared for all the labels
     * and the given label items
     *
     * @param items
     */
    public HTMLLabels(Style style, HTMLLabelItem... items) {
        setItems(items);
        setStyle(style);
    }

    /**
     * @return An array of the label items
     */
    public HTMLLabelItem[] getItems() {
        return items;
    }

    /**
     * Sets the label items
     *
     * @param items
     */
    public void setItems(HTMLLabelItem... items) {
        this.items = items;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the CSS style shared for all the labels.
     *
     * @param style
     */
    public void setStyle(Style style) {
        this.style = style;
    }
}
