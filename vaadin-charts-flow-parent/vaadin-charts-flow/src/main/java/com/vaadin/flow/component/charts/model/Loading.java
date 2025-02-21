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
 * The loading options control the appearance of the loading screen that covers
 * the plot area on chart operations. This screen only appears after an explicit
 * call to <code>chart.showLoading()</code>. It is a utility for developers to
 * communicate to the end user that something is going on, for example while
 * retrieving new data via an XHR connection. The "Loading..." text itself is
 * not part of this configuration object, but part of the <code>lang</code>
 * object.
 */
public class Loading extends AbstractConfigurationObject {

    private Number hideDuration;
    private Style labelStyle;
    private Number showDuration;
    private Style style;

    public Loading() {
    }

    /**
     * @see #setHideDuration(Number)
     */
    public Number getHideDuration() {
        return hideDuration;
    }

    /**
     * The duration in milliseconds of the fade out effect.
     * <p>
     * Defaults to: 100
     */
    public void setHideDuration(Number hideDuration) {
        this.hideDuration = hideDuration;
    }

    /**
     * @see #setLabelStyle(Style)
     */
    public Style getLabelStyle() {
        if (labelStyle == null) {
            labelStyle = new Style();
        }
        return labelStyle;
    }

    /**
     * CSS styles for the loading label <code>span</code>.
     * <p>
     * Defaults to: { "fontWeight": "bold", "position": "relative", "top": "45%"
     * }
     */
    public void setLabelStyle(Style labelStyle) {
        this.labelStyle = labelStyle;
    }

    /**
     * @see #setShowDuration(Number)
     */
    public Number getShowDuration() {
        return showDuration;
    }

    /**
     * The duration in milliseconds of the fade in effect.
     * <p>
     * Defaults to: 100
     */
    public void setShowDuration(Number showDuration) {
        this.showDuration = showDuration;
    }

    /**
     * @see #setStyle(Style)
     */
    public Style getStyle() {
        if (style == null) {
            style = new Style();
        }
        return style;
    }

    /**
     * CSS styles for the loading screen that covers the plot area.
     * <p>
     * Defaults to: { "position": "absolute", "backgroundColor": "#ffffff",
     * "opacity": 0.5, "textAlign": "center" }
     */
    public void setStyle(Style style) {
        this.style = style;
    }
}
