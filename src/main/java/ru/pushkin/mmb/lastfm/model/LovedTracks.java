package ru.pushkin.mmb.lastfm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class LovedTracks implements Serializable {

    @XmlAttribute(name = "page")
    private long page;

    @XmlAttribute(name = "perPage")
    private long perPage;

    @XmlAttribute(name = "totalPages")
    private long totalPages;

    @XmlAttribute(name = "total")
    private long total;

    @XmlElement(name = "track")
    private List<TrackInfo> tracks;

}
