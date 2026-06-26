package facu.studer.services.implementation;

import facu.studer.DTOs.blocks.*;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.blocks.Difficulty;
import facu.studer.entities.users.User;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.BlockMapper;
import facu.studer.repositories.block.BlockRepository;
import facu.studer.repositories.block.BlockVersionRepository;
import facu.studer.services.BlockService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final BlockVersionRepository blockVersionRepository;

    private static final int PAGE_SIZE = 15;

    @PersistenceContext
    private EntityManager entityManager;

    public BlockServiceImpl(BlockRepository blockRepository, BlockVersionRepository blockVersionRepository) {
        this.blockRepository = blockRepository;
        this.blockVersionRepository = blockVersionRepository;
    }

    @Override
    public BlockResponseDTO create(String username, BlockCreateRequestDTO request) {

        User owner = findUserByUsername(username);

        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        Block block = Block.builder()
                .owner(owner)
                .parentBlock(null)
                .rootBlock(null)
                .name(request.getName())
                .slug(request.getSlug())
                .currentVersion(null)
                .difficulty(Difficulty.valueOf(request.getDifficulty()))
                .tags(managedTags)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Block createdBlock = blockRepository.save(block);

        BlockVersion version = BlockVersion.builder()
                .Block(createdBlock)
                .content(request.getContent())
                .versionNumber(1L)
                .changeDescription("Original")
                .published(request.getPublished())
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        BlockVersion createdVersion = blockVersionRepository.save(version);

        createdBlock.setCurrentVersion(createdVersion);
        blockRepository.save(createdBlock);

        entityManager.flush();

        return BlockMapper.toResponseDTO(createdBlock, createdVersion);

    }

    @Override
    public BlockResponseDTO fork(String username, BlockForkCreateRequestDTO request) {

        User owner = findUserByUsername(username);

        Set<Tag> managedTags = resolveTagsByName(request.getTags());


        Block parent = blockRepository.findById(request.getBlockId())
                .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

        Block parentRoot = parent.getRootBlock() != null ? parent.getRootBlock() : parent;

        Block block = Block.builder()
                .owner(owner)
                .parentBlock(parent)
                .rootBlock(parentRoot)
                .name(request.getName())
                .slug(request.getSlug())
                .currentVersion(null)
                .difficulty(Difficulty.valueOf(request.getDifficulty()))
                .tags(managedTags)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Block createdBlock = blockRepository.save(block);

        BlockVersion version = BlockVersion.builder()
                .Block(createdBlock)
                .content(request.getContent())
                .versionNumber(1L)
                .changeDescription("Forked")
                .published(request.getPublished())
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        BlockVersion createdVersion = blockVersionRepository.save(version);

        createdBlock.setCurrentVersion(createdVersion);
        blockRepository.save(createdBlock);

        entityManager.flush();

        return BlockMapper.toResponseDTO(createdBlock, createdVersion);

    }


    @Override
    public BlockResponseDTO newVersion(String username, BlockVersionCreateRequestDTO request) {

        User owner = findUserByUsername(username);

        Block parent = blockRepository.findById(request.getBlockId())
                .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));


        if(!Objects.equals(parent.getOwner().getId(), owner.getId())) {
            throw new ResourceNotFoundException("block_version.not_author");
        }


        BlockVersion version = BlockVersion.builder()
                .Block(parent)
                .content(request.getContent())
                .versionNumber(parent.getCurrentVersion().getVersionNumber() + 1)
                .changeDescription(request.getChangeDescription())
                .published(request.getPublished())
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        BlockVersion createdVersion = blockVersionRepository.save(version);

        parent.setCurrentVersion(createdVersion);
        blockRepository.save(parent);

        entityManager.flush();

        return BlockMapper.toResponseDTO(parent, createdVersion);

    }

    @Override
    public BlockResponseDTO getBlock(Long id, String username) {

        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

        if(!block.getCurrentVersion().getIsActive()) {
            throw new ResourceNotFoundException("block_version.not_found");
        }

        return BlockMapper.toResponseDTO(block, block.getCurrentVersion());
    }

    @Override
    public BlockResponseDTO getBlockBySlug(String slug, String username) {

        Optional<Block> block = blockRepository.findBySlug(slug);

        if(block.isEmpty()) {
            throw new ResourceNotFoundException("block.not_found");
        }

        if(!block.get().getCurrentVersion().getIsActive()) {
            throw new ResourceNotFoundException("block_version.not_found");
        }

        return BlockMapper.toResponseDTO(block.get(), block.get().getCurrentVersion());
    }

    @Override
    public BlockResponseDTO getBlockByVersion(Long id, String username) {

        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

        BlockVersion version = blockVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

        if(version.getIsActive()) {
            throw new ResourceNotFoundException("block_version.not_found");
        }

        return BlockMapper.toResponseDTO(block, version);
    }


    @Override
    public BlockPageResponseDTO getBlocksBySearch(
            String username,
            int page,
            List<String> tags,
            Boolean orderByLikes,
            String difficulty,
            String user,
            String name){
        return null; //TODO
    }

    @Override
    public BlockCompleteTreeResponseDTO getBlockTree(Long id, String username) {

        BlockCompleteResponseDTO blockDTO = getBlockVersion(id, username);
        BlockCompleteResponseDTO parentDTO = null;

        if(blockDTO.getParent() != null) {
            parentDTO =  getBlockVersion(blockDTO.getParent().getId(), username);
        }

        List<Block> sons = blockRepository.findByParentBlock_Id(blockDTO.getId());
        List<BlockResponseDTO> sonsDTO = new ArrayList<>();
        for(Block son : sons) {
            if(son.getCurrentVersion().getIsActive()) {
                sonsDTO.add(BlockMapper.toResponseDTO(son, son.getCurrentVersion()));
            }
        }

        return BlockCompleteTreeResponseDTO.builder()
                .block(blockDTO)
                .parent(parentDTO)
                .sons(sonsDTO)
                .build();
    }

    @Override
    public BlockCompleteResponseDTO getBlockVersion(Long id, String username) {

        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

        List<BlockVersion> versions = blockVersionRepository.findBlockVersionsByBlockId(id);

        BlockResponseDTO parent = null;

        if(block.getParentBlock() != null) {
            Block parentBlock = block.getParentBlock();

            BlockVersion parentVersion = blockVersionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

            parent = BlockMapper.toResponseDTO(parentBlock, parentVersion);
        }


        return BlockMapper.toCompleteResponseDTO(block, versions, parent);


    }

    @Override
    public BlockPageResponseDTO getBlockByUser(Long id, String username, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<Block> blocksPage = blockRepository.findByOwnerId(id, pageable);

        List<BlockResponseDTO> responseDTOS = new ArrayList<>();

        for (Block block : blocksPage.getContent()) {
            if (!block.getCurrentVersion().getIsActive()) {
                throw new ResourceNotFoundException("block_version.not_found");
            }

            BlockVersion version = blockVersionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

            responseDTOS.add(BlockMapper.toResponseDTO(block, version));

        }

        return BlockPageResponseDTO.builder()
                .blocks(responseDTOS)
                .totalElements(blocksPage.getTotalElements())
                .hasMore(blocksPage.hasNext())
                .currentPage(page)
                .build();
    }

    @Override
    public BlockPageResponseDTO getMyBlock(String username, int page) {
        User user = findUserByUsername(username);
        return getBlockByUser(user.getId(), username, page);
    }


    private User findUserByUsername(String username) {
        var query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username AND u.isActive = true", User.class);
        query.setParameter("username", username);
        var results = query.getResultList();
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("user.not_found");
        }
        return results.get(0);
    }

    /**
     * Resolves tag names to managed Tag entities within the current persistence context.
     * Creates any tags that don't exist yet.
     */
    private Set<Tag> resolveTagsByName(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> normalized = tagNames.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(n -> !n.isBlank())
                .collect(Collectors.toSet());

        if (normalized.isEmpty()) {
            return new HashSet<>();
        }

        // Find existing tags in the current persistence context
        List<Tag> existing = entityManager.createQuery(
                        "SELECT t FROM Tag t WHERE t.name IN :names AND t.isActive = true", Tag.class)
                .setParameter("names", normalized)
                .getResultList();

        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> result = new HashSet<>(existing);

        // Create missing tags
        for (String name : normalized) {
            if (!existingNames.contains(name)) {
                Tag newTag = Tag.builder()
                        .name(name)
                        .isActive(true)
                        .createdDatetime(LocalDateTime.now())
                        .lastUpdatedDatetime(LocalDateTime.now())
                        .build();
                entityManager.persist(newTag);
                result.add(newTag);
            }
        }

        return result;
    }
}
