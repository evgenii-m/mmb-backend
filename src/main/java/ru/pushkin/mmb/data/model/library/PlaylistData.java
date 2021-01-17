package ru.pushkin.mmb.data.model.library;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.pushkin.mmb.data.enumeration.PlaylistType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlist_data")
public class PlaylistData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String title;

    private String description;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @NotNull
    private boolean active;

    @NotNull
    private boolean sync;

    @Enumerated(value = EnumType.STRING)
    private PlaylistType type;

    @Column(name = "source_url")
    private String sourceUrl;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "playlistData", cascade = CascadeType.ALL)
    private List<PlaylistTrack> tracks;
}
