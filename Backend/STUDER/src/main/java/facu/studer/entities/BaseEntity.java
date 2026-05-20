package facu.studer.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


/**
 * Abstract base entity for all domain entities.
 * Contains active flag for soft deletion.
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Auto-generated identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Creation timestamp (UTC).
     */
    @Column(nullable = false)
    private LocalDateTime createdDatetime;

    /**
     * Last update timestamp (UTC).
     */
    @Column(nullable = false)
    private LocalDateTime lastUpdatedDatetime;

    /**
     * Active flag (soft-delete).
     */
    @Column(nullable = false)
    private Boolean isActive;

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof org.hibernate.proxy.HibernateProxy
                ? ((org.hibernate.proxy.HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof org.hibernate.proxy.HibernateProxy
                ? ((org.hibernate.proxy.HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return getId() != null && java.util.Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof org.hibernate.proxy.HibernateProxy
                ? ((org.hibernate.proxy.HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
