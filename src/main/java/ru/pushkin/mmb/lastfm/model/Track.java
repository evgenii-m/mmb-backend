package ru.pushkin.mmb.lastfm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class Track implements Serializable {

	@XmlAttribute(name = "nowplaying")
	private boolean nowPlaying;

	@XmlElement(name = "artist")
	private Artist artist;

	@XmlElement(name = "name")
	private String name;

	@XmlElement(name = "mbid")
	private String mbid;

	@XmlElement(name = "album")
	private AlbumShort album;

	@XmlElement(name = "url")
	private String url;

	@XmlElement(name = "date")
	private Date date;

	@XmlElement(name = "streamable")
	private String streamable;

	@XmlElement(name = "image")
	private List<Image> images;

}