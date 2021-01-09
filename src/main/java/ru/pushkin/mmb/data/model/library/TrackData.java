package ru.pushkin.mmb.data.model.library;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "track_data")
public class TrackData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String mbid;

    @NotNull
    @Column(unique = true)
    private String title;

    @NotNull
    @Column(name = "track_name")
    private String trackName;

    @NotNull
    private String artist;

    private String album;

    private Long length;

    @Column(name = "lastfm_url")
    private String lastFmUrl;

    @Column(name = "total_play_count")
    private Long totalPlayCount;

    @Column(name = "total_listeners_count")
    private Long totalListenersCount;

    @ManyToMany
    @JoinTable(
            name = "track_tag",
            joinColumns = @JoinColumn(name = "track_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id")
    )
    private Set<TagData> tags;

    @Transient
    private LocalDateTime dateTime;

    @Transient
    private UserTrackInfo userTrackInfo;


    public void setTitle(String artist, String trackName) {
        this.title = String.format("%s - %s", artist, trackName);
    }
}
