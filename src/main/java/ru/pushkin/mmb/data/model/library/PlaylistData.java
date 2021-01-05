package ru.pushkin.mmb.data.model.library;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Boolean active;

    @OneToMany(fetch = FetchType.EAGER)
    private List<PlaylistTrack> tracks;
}
