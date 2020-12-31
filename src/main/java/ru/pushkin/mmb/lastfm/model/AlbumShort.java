package ru.pushkin.mmb.lastfm.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class AlbumShort implements Serializable {

	@XmlAttribute(name = "mbid")
	private String mbid;

	@XmlValue
	private String name;


	public AlbumShort() {
	}

	public AlbumShort(String mbid, String name) {
		this.mbid = mbid;
		this.name = name;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "AlbumShort{" +
				"mbid='" + mbid + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
