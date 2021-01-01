package ru.pushkin.mmb.lastfm;

import ru.pushkin.mmb.lastfm.model.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

public interface LastFmApiProvider {

    String authGetToken();

    Optional<String> authGetSession(String token);

    Optional<RecentTracks> userGetRecentTracks(Integer limit, @NotNull String username, Integer page,
                                               Date from, Boolean extended, Date to);

    Optional<TrackInfo> getTrackInfo(String mbid, String track, String artist, String username, Boolean autocorrect);

    Optional<UpdateNowPlayingResult> updateNowPlaying(@NotNull String sessionKey, @NotNull String artist,
                                                      @NotNull String track, String album, Long duration);

    Optional<ScrobblesResult> scrobbleTrack(@NotNull String sessionKey, @NotNull String artist, @NotNull String track,
                                            int timestamp, String album, Boolean chosenByUser, Long duration);

    Optional<User> userGetInfo(String user);

    Optional<LovedTracks> userGetLovedTracks(@NotNull String username, Integer page, Integer limit);
}
