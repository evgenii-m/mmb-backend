package ru.pushkin.mmb.data.model.library;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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

    public TagData(@NotNull String name, @NotNull String url) {
        this.name = name;
        this.url = url;
    }
}
