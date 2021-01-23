package ru.pushkin.mmb.data.model.library;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "tag_data")
public class TagData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(unique = true)
    private String name;

    @NotNull
    private String url;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "track_tag",
            joinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "track_id", referencedColumnName = "id")
    )
    private Set<TrackData> tracks;

    public TagData(@NotNull String name, @NotNull String url) {
        this.name = name;
        this.url = url;
    }
}
