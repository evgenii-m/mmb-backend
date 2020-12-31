package ru.pushkin.mmb.lastfm.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class Image implements Serializable {

	@XmlAttribute
	private String size;

	@XmlValue
	private String url;

	public Image() {
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Image{" +
				"size='" + size + '\'' +
				", url='" + url + '\'' +
				'}';
	}
}
