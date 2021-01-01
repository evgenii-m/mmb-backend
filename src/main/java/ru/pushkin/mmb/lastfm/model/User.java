package ru.pushkin.mmb.lastfm.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class User implements Serializable {

    @XmlElement
    private String name;
    @XmlElement
    private String url;
    @XmlElement
    private String realname;
    @XmlElement
    private String language;
    @XmlElement
    private String country;
    @XmlElement
    private int age = -1;
    @XmlElement
    private String gender;
    @XmlElement
    private boolean subscriber;
    @XmlElement
    private int numPlaylists;
    @XmlElement
    private int playcount;
    @XmlElement
    private Date registeredDate;

}
