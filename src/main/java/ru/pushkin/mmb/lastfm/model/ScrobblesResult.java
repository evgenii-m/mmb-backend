package ru.pushkin.mmb.lastfm.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class ScrobblesResult implements Serializable {

	@XmlAttribute
	private Integer accepted;

	@XmlAttribute
	private Integer ignored;

	@XmlElement(name = "scrobble")
	private List<TrackScrobbleResult> trackScrobbleResults;

	public ScrobblesResult() {
	}

	public Integer getAccepted() {
		return accepted;
	}

	public void setAccepted(Integer accepted) {
		this.accepted = accepted;
	}

	public Integer getIgnored() {
		return ignored;
	}

	public void setIgnored(Integer ignored) {
		this.ignored = ignored;
	}

	public List<TrackScrobbleResult> getTrackScrobbleResults() {
		return trackScrobbleResults;
	}

	public void setTrackScrobbleResults(List<TrackScrobbleResult> trackScrobbleResults) {
		this.trackScrobbleResults = trackScrobbleResults;
	}

	@Override
	public String toString() {
		return "ScrobblesResult{" +
				"accepted=" + accepted +
				", ignored=" + ignored +
				", trackScrobbleResults=" + trackScrobbleResults +
				'}';
	}
}
