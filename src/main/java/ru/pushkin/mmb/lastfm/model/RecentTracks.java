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
public class RecentTracks implements Serializable {

	@XmlAttribute(name = "user")
	private String user;

	@XmlAttribute(name = "page")
	private long page;

	@XmlAttribute(name = "perPage")
	private long perPage;

	@XmlAttribute(name = "totalPages")
	private long totalPages;

	@XmlAttribute(name = "total")
	private long total;

	@XmlElement(name = "track")
	private List<Track> tracks;

}
