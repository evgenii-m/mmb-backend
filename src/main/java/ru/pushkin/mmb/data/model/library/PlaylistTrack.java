package ru.pushkin.mmb.data.model.library;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlist_track")
public class PlaylistTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private PlaylistData playlistData;

    @ManyToOne
    @JoinColumn(name = "track_id", insertable = false, updatable = false)
    private TrackData trackData;

    @NotNull
    private Integer position;

    private LocalDateTime added;

}
