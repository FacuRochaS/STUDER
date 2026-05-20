package facu.studer.services.implementation;


import facu.studer.entities.BaseEntity;
import facu.studer.services.GenericService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract generic service implementation for CRUD operations.
 *
 * <p>This class provides basic persistence operations (create, update, soft delete,
 * retrieve by ID, retrieve all) for entities extending {@link BaseEntity}.
 * Concrete services should extend this class and implement the abstract methods
 * that use request and response DTOs.</p>
 *
 * @param <EY>  Entity type extending BaseEntity
 * @param <CRQ> Create request DTO type
 * @param <URQ> Update request DTO type
 * @param <DRQ> Delete request DTO type
 * @param <RSP> Response DTO type
 */

public abstract class GenericServiceImpl<
        EY extends BaseEntity,
        CRQ,
        URQ,
        DRQ,
        RSP
        > implements GenericService<CRQ, URQ, DRQ, RSP> {


    /**
     * JPA repository used for persistence operations.
     */
    private final JpaRepository<EY, Long> repository;

    /**
     * Constructor.
     *
     * @param entityRepository JPA repository for the entity type.
     */
    protected GenericServiceImpl(JpaRepository<EY, Long> entityRepository) {
        this.repository = entityRepository;
    }


    /**
     * Persists a newly created entity, setting audit fields and activating it.
     *
     * @param entity The entity to be created.
     * @return The persisted entity.
     */
    protected EY persistCreate(EY entity) {
        entity.setCreatedDatetime(LocalDateTime.now());
        entity.setLastUpdatedDatetime(LocalDateTime.now());
        entity.setIsActive(true);
        return repository.save(entity);
    }

    /**
     * Persists an updated entity, setting audit fields.
     *
     * @param entity The entity to be updated.
     * @return The persisted entity.
     */
    protected EY persistUpdate(EY entity) {
        entity.setLastUpdatedDatetime(LocalDateTime.now());
        entity.setIsActive(true);
        return repository.save(entity);
    }

    /**
     * Persists a soft-deleted entity, setting audit fields and marking as inactive.
     *
     * @param entity The entity to be soft-deleted.
     * @return The persisted entity.
     */
    protected EY persistSoftDelete(EY entity) {
        entity.setLastUpdatedDatetime(LocalDateTime.now());
        entity.setIsActive(false);
        return repository.save(entity);
    }

    /**
     * Retrieves an entity by its ID.
     *
     * @param id The ID of the entity.
     * @return The entity if found.
     * @throws EntityNotFoundException if the entity does not exist.
     */
    protected EY getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entidad no encontrada"));
    }

    /**
     * Retrieves all entities.
     *
     * @return List of all entities.
     * @throws EntityNotFoundException if no entities are found
     */
    protected List<EY> getAllEntities() {
        List<EY> all = repository.findAll(Sort.by(Sort.Direction.DESC, "lastUpdatedDatetime"));
        if (all.isEmpty()) {
            throw new EntityNotFoundException("No se encontraron entidades");
        }
        return all;
    }


    /**
     * Creates a new entity using the create request DTO.
     *
     * @param request DTO containing creation data
     * @return Response DTO representing the created entity
     */
    @Override
    public abstract RSP create(CRQ request);

    /**
     * Updates an existing entity using the update request DTO.
     *
     * @param request DTO containing update data
     * @return Response DTO representing the updated entity
     */
    @Override
    public abstract RSP update(URQ request);


    /**
     * Performs a soft delete using the delete request DTO.
     *
     * @param request DTO containing the ID of the entity to delete
     * @return Response DTO representing the deleted entity
     */
    @Override
    public abstract RSP softDelete(DRQ request);

    /**
     * Retrieves an entity by ID and returns its response DTO.
     *
     * @param id ID of the entity
     * @return Response DTO of the entity
     */
    @Override
    public abstract RSP getById(Long id);

}
