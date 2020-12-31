package ru.pushkin.mmb.lastfm.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class ArtistShort implements Serializable {

	@XmlAttribute(name = "mbid")
	private String mbid;

	@XmlValue
	private String name;


	public ArtistShort() {
	}

	public ArtistShort(String mbid, String name) {
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
		return "ArtistShort{" +
				"mbid='" + mbid + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
