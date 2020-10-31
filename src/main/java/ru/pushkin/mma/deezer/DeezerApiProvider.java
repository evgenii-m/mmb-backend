package ru.pushkin.mma.deezer;


import ru.pushkin.mma.deezer.model.Playlist;
import ru.pushkin.mma.deezer.model.Playlists;
import ru.pushkin.mma.deezer.model.Track;
import ru.pushkin.mma.deezer.model.Tracks;
import ru.pushkin.mma.deezer.model.internal.PlaylistId;

import java.util.List;

public interface DeezerApiProvider {

	String getUserAuthorizationPageUrl();

	String getAccessToken(String code);

	Track getTrack(long trackId, String accessToken) throws DeezerApiErrorException;

	Playlists getPlaylists(String accessToken, Integer index, Integer limit) throws DeezerApiErrorException;

	Playlist getPlaylist(long playlistId, String accessToken) throws DeezerApiErrorException;

	Tracks getPlaylistTracks(long playlistId, String accessToken, Integer index, Integer limit) throws DeezerApiErrorException;

	PlaylistId createPlaylist(String title, String accessToken) throws DeezerApiErrorException;

	boolean deletePlaylist(long playlistId, String accessToken) throws DeezerApiErrorException;

	boolean renamePlaylist(long playlistId, String newTitle, String accessToken) throws DeezerApiErrorException;

	boolean addTracksToPlaylist(long playlistId, List<Long> trackIds, String accessToken) throws DeezerApiErrorException;

	boolean removeTracksFromPlaylist(long playlistId, List<Long> trackIds, String accessToken) throws DeezerApiErrorException;

	Tracks searchTracksQuery(String query, String accessToken) throws DeezerApiErrorException;

	boolean addTrackToFavorites(long trackId, String accessToken) throws DeezerApiErrorException;

	boolean removeTrackFromFavorites(long trackId, String accessToken) throws DeezerApiErrorException;
}
