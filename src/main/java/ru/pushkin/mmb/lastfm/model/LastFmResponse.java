package ru.pushkin.mmb.lastfm.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "lfm")
public class LastFmResponse implements Serializable {

	@XmlAttribute(name = "status")
	private String status;

	@XmlElement(name = "recenttracks")
	private RecentTracks recentTracks;

	@XmlElement(name = "track")
	private TrackInfo trackInfo;

	@XmlElement(name = "nowplaying")
	private UpdateNowPlayingResult updateNowPlayingResult;

	@XmlElement(name = "scrobbles")
	private ScrobblesResult scrobblesResult;

	@XmlElement(name = "lovedtracks")
	private LovedTracks lovedTracks;

	@XmlElement(name = "user")
	private User user;


	public LastFmResponse() {
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public RecentTracks getRecentTracks() {
		return recentTracks;
	}

	public void setRecentTracks(RecentTracks recentTracks) {
		this.recentTracks = recentTracks;
	}

	public TrackInfo getTrackInfo() {
		return trackInfo;
	}

	public void setTrackInfo(TrackInfo trackInfo) {
		this.trackInfo = trackInfo;
	}

	public UpdateNowPlayingResult getUpdateNowPlayingResult() {
		return updateNowPlayingResult;
	}

	public void setUpdateNowPlayingResult(UpdateNowPlayingResult updateNowPlayingResult) {
		this.updateNowPlayingResult = updateNowPlayingResult;
	}

	public ScrobblesResult getScrobblesResult() {
		return scrobblesResult;
	}

	public void setScrobblesResult(ScrobblesResult scrobblesResult) {
		this.scrobblesResult = scrobblesResult;
	}

	public LovedTracks getLovedTracks() {
		return lovedTracks;
	}

	public void setLovedTracks(LovedTracks lovedTracks) {
		this.lovedTracks = lovedTracks;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "LastFmResponse{" +
				"status='" + status + '\'' +
				", recentTracks=" + recentTracks +
				", trackInfo=" + trackInfo +
				", updateNowPlayingResult=" + updateNowPlayingResult +
				", scrobblesResult=" + scrobblesResult +
				", lovedTracks=" + lovedTracks +
				", user=" + user +
				'}';
	}
}
