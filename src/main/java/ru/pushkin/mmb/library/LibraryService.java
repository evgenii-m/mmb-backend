package ru.pushkin.mmb.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.deezer.model.Track;
import ru.pushkin.mmb.mapper.TrackMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    @Autowired
    private DeezerApiService deezerApiService;
    @Autowired
    private TrackMapper trackMapper;

    public FavoriteTracksResponse findFavoriteTracks(int page, int size) {
        List<Track> deezerFavoriteTracks = deezerApiService.getFavoriteTracks(page, size);

        int totalSize = deezerFavoriteTracks.size();
        int startIndex = page * size;
        int endIndex = Math.min((startIndex + size), totalSize);
        List<TrackDto> trackDtos = deezerFavoriteTracks.subList(startIndex, endIndex).stream()
                .map(data -> trackMapper.map(data))
                .collect(Collectors.toList());
        return new FavoriteTracksResponse(page, size, totalSize, trackDtos);
    }
}