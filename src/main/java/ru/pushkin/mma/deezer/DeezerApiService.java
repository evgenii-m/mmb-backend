package ru.pushkin.mma.deezer;


import ru.pushkin.mma.deezer.model.Playlist;
import ru.pushkin.mma.deezer.model.Track;

import java.util.List;

public interface DeezerApiService {

	/**
	 * Return user authorization page URL as https://connect.deezer.com/oauth/auth.php with parameters
	 */
	String getUserAuthorizationPageUrl();

	/**
	 * Method for checking authorization code in location URI
	 *
	 * @return authorization code if detected
	 */
	String checkAuthorizationCode(String locationUri) throws DeezerApiErrorException;

	/**
	 * Get access token by received authorization code
	 *
	 * @return currentAccessToken (if receiving access token failed, previous access Token value will be returned)
	 */
	String obtainNewAccessToken(String code);

	String getAccessToken() throws DeezerApiErrorException;

	/**
	 * Get track object
	 * See https://developers.deezer.com/api/track
	 */
	void getTrack(long trackId) throws DeezerApiErrorException;

	/**
	 * Get current user favorite playlists
	 * See https://developers.deezer.com/api/user/playlists
	 */
	List<Playlist> getPlaylists() throws DeezerApiErrorException;

	Playlist getPlaylist(long playlistId) throws DeezerApiErrorException;

	List<Track> getPlaylistTracks(long playlistId) throws DeezerApiErrorException;

	Long createPlaylist(String title) throws DeezerApiErrorException;

	boolean deletePlaylist(long playlistId) throws DeezerApiErrorException;

	boolean renamePlaylist(long playlistId, String newTitle) throws DeezerApiErrorException;

	boolean addTrackToPlaylist(long playlistId, long trackId) throws DeezerApiErrorException;

	boolean addTracksToPlaylist(long playlistId, List<Long> trackIds) throws DeezerApiErrorException;

	boolean removeTrackFromPlaylist(long playlistId, long trackId) throws DeezerApiErrorException;

	boolean removeTracksFromPlaylist(long playlistId, List<Long> trackIds) throws DeezerApiErrorException;

	List<Track> searchTracksQuery(String shortQuery, String extendedQuery) throws DeezerApiErrorException;

	boolean addTrackToFavorites(long trackId) throws DeezerApiErrorException;

	boolean removeTrackFromFavorites(long trackId) throws DeezerApiErrorException;

	String getDeezerPlaylistWebPageUrl(long playlistId);
}
