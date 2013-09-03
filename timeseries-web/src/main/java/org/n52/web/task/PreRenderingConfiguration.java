package org.n52.web.task;

import org.n52.io.v1.data.StyleProperties;

public class PreRenderingConfiguration {
	
	private String timeseriesId;
	
	private String[] interval;
	
	private StyleProperties style;

	public String getTimeseriesId() {
		return timeseriesId;
	}

	public void setTimeseriesId(String timeseriesId) {
		this.timeseriesId = timeseriesId;
	}

	public String[] getInterval() {
		return interval;
	}

	public void setInterval(String[] interval) {
		this.interval = interval;
	}

	public StyleProperties getStyle() {
		return style;
	}

	public void setStyle(StyleProperties style) {
		this.style = style;
	}
}
