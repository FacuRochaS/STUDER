package facu.studer.entities.blocks;

import facu.studer.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "blocks_versions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BlockVersion extends BaseEntity {
    /**
     * Parent block.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block Block;


    /**
     * Content of the block.
     */
    @Column(columnDefinition = "jsonb")
    private String content;

    /**
     * Version number.
     */
    @Column(nullable = false)
    private Long versionNumber;

    /**
     * Description log.
     */
    @Column(nullable = false)
    private String changeDescription;

    /**
     * Published status.
     */
    @Column(nullable = false)
    private Boolean published;

}
