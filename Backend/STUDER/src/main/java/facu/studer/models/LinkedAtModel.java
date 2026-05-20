package facu.studer.models;


import facu.studer.entities.LinkedType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Generic model representing linked entity information.
 * Used by notifications and calendar to resolve linked entities.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAtModel {

    private Long linkedId;

    private LinkedType linkedType;

    private String title;

    private String content;

    private LocalDateTime dateTime;

    private Boolean active;

}
