package facu.studer.entities.blocks;

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

import java.util.Set;


@Entity
@Table(name = "blocks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Block extends BaseEntity {

    /**
     * Owner.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;


    /**
     * Parent block.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_block_id")
    private Block parentBlock;

    /**
     * Root block.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_block_id")
    private Block rootBlock;


    /**
     * Name of the block.
     */
    @Column(nullable = false)
    private String name;


    /**
     * Unique SLUG.
     */
    @Column(nullable = false, unique = true)
    private String slug;


    /**
     * Difficulty type.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Difficulty difficulty;

    /**
     * Current active version.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private BlockVersion currentVersionId;

    /**
     * Tags associated with the block.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "block_tags",
            joinColumns = @JoinColumn(name = "block_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

}
