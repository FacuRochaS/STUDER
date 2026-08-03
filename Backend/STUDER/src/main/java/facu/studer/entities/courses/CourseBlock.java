package facu.studer.entities.courses;

import facu.studer.entities.BaseEntity;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "course_blocks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CourseBlock extends BaseEntity {
    /**
     * Course.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * block.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    /**
     * Parent block.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_version_id")
    private BlockVersion version;

    private Integer blockOrder;




}
