package ru.pushkin.mmb.data.model.library;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document("ListeningHistory")
public class HistoryTrackData extends TrackData {
    private LocalDateTime dateTime;
}
