package ru.pushkin.mmb.data.model.library;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "user_track_data")
public class UserTrackData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "track_id")
    private Integer trackId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "favorite")
    private Boolean favorite;

    @Column(name = "listen_count")
    private Long listenCount;

}
