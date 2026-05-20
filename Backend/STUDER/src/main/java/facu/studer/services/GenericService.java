package facu.studer.services;

/**
 * Generic interface for CRUD services.
 * Defines basic operations for create, update,
 * logical delete, get by ID, and list all items.
 * @param <CRQ> Type of the Create Request DTO.
 * @param <URQ> Type of the Update Request DTO.
 * @param <DRQ> Type of the Delete Request DTO.
 * @param <RSP> Type of the Response DTO.
 */

public interface GenericService<
        CRQ,  // Create Request DTO
        URQ,  // Update Request DTO
        DRQ,  // Delete Request DTO
        RSP   // Response DTO
        > {

    /**
     * Creates a new entity.
     * @param request DTO with creation data.
     * @return Response DTO with the created entity.
     */
    RSP create(CRQ request);

    /**
     * Updates an existing entity.
     * @param request DTO with update data.
     * @return Response DTO with the updated entity.
     */
    RSP update (URQ request);

    /**
     * Logically deletes an entity.
     * @param request DTO with delete data.
     * @return Response DTO with the deleted entity.
     */
    RSP softDelete(DRQ request);

    /**
     * Gets an entity by its ID.
     * @param id ID of the entity.
     * @return Response DTO with the found entity.
     */
    RSP getById(Long id);

}
