package ru.pushkin.mmb.library;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.lastfm.LastFmService;
import ru.pushkin.mmb.mapper.TrackMapper;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LibraryService {

    private final DeezerApiService deezerApiService;
    private final LastFmService lastFmService;
    private final TrackMapper trackMapper;

    public FavoriteTracksResponse findFavoriteTracks(int page, int size) {
//        List<Track> deezerFavoriteTracks = deezerApiService.getFavoriteTracks(page, size);
        List<ru.pushkin.mmb.lastfm.model.TrackInfo> favoriteTracks = lastFmService.getFavoriteTracks(page, size);

        int totalSize = favoriteTracks.size();
        List<TrackDto> trackDtos = favoriteTracks.stream()
                .map(data -> trackMapper.map(data))
                .collect(Collectors.toList());
        return new FavoriteTracksResponse(page, size, totalSize, trackDtos);
    }
}
