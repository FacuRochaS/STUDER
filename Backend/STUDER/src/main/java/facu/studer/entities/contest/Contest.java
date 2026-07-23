package facu.studer.entities.contest;

import com.fasterxml.jackson.databind.JsonNode;
import facu.studer.entities.BaseEntity;
import facu.studer.entities.Tag;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "contest")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Contest extends BaseEntity {

    private String title;

    private String banner;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String difficulty;

    private String theme;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode content;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "contest_tags",
            joinColumns = @JoinColumn(name = "contest_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    private LocalDateTime startDate;
    private LocalDateTime preparationEndDate;
    private LocalDateTime buildingEndDate;
    private LocalDateTime validationEndDate;
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode rewards;

    @Column(columnDefinition = "TEXT")
    private String externalLinks;

    @Column(columnDefinition = "TEXT")
    private String bibliography;

    @Column(columnDefinition = "TEXT")
    private String learningObjectives;

    private Integer minLevel;
    private Long minReputation;
    private Integer maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private Integer participantCount = 0;

    @Column(nullable = false)
    private Integer courseCount = 0;

    @Column(nullable = false)
    private Integer blockCount = 0;
}
