package facu.studer.services.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.blocks.*;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockLike;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.blocks.Difficulty;
import facu.studer.entities.users.User;
import facu.studer.entities.users.Friend;
import facu.studer.exceptions.ResourceNotFoundException;
import facu.studer.mappers.BlockMapper;
import facu.studer.repositories.block.BlockLikeRepository;
import facu.studer.repositories.block.BlockRepository;
import facu.studer.repositories.block.BlockVersionRepository;
import facu.studer.repositories.courses.CourseBlockRepository;
import facu.studer.repositories.FriendRepository;
import facu.studer.services.BlockService;
import facu.studer.services.PointsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepository;
    private final BlockVersionRepository blockVersionRepository;
    private final BlockLikeRepository blockLikeRepository;
    private final PointsService pointsService;
    private final CourseBlockRepository courseBlockRepository;
    private final FriendRepository friendRepository;


    private static final int PAGE_SIZE = 15;

    @PersistenceContext
    private EntityManager entityManager;

    public BlockServiceImpl(BlockRepository br, BlockVersionRepository bvr,
                            BlockLikeRepository blr, PointsService ps,
                            CourseBlockRepository cbr, FriendRepository fr) {
        this.blockRepository = br; this.blockVersionRepository = bvr;
        this.blockLikeRepository = blr; this.pointsService = ps;
        this.courseBlockRepository = cbr; this.friendRepository = fr;
    }

    @Override
    @Transactional
    public BlockResponseDTO create(String username, BlockCreateRequestDTO request) {

        User owner = findUserByUsername(username);

        Set<Tag> managedTags = resolveTagsByName(request.getTags());

        String slug = generateUniqueSlug(request.getName(), null);

        Block block = Block.builder()
                .owner(owner)
                .parentBlock(null)
                .rootBlock(null)
                .name(request.getName())
                .slug(slug)
                .currentVersion(null)
                .difficulty(Difficulty.valueOf(request.getDifficulty()))
                .tags(managedTags)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        Block createdBlock = blockRepository.save(block);



        BlockVersion version = BlockVersion.builder()
                .block(createdBlock)
                .content(parseContent(request.getContent()))
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

        pointsService.addPoints(owner, 1L);

        return toResponseDTOWithLikes(createdBlock, createdVersion, username);

    }

    @Override
    @Transactional
    public BlockResponseDTO fork(String username, BlockForkCreateRequestDTO request) {

        User owner = findUserByUsername(username);

        Set<Tag> managedTags = resolveTagsByName(request.getTags());


    Block parent = blockRepository.findById(request.getBlockId())
            .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

    Block parentRoot = parent.getRootBlock() != null ? parent.getRootBlock() : parent;

    String slug = generateUniqueSlug(request.getName(), "-fork");

    Block block = Block.builder()
            .owner(owner)
            .parentBlock(parent)
            .rootBlock(parentRoot)
            .name(request.getName())
            .slug(slug)
            .currentVersion(null)
            .difficulty(Difficulty.valueOf(request.getDifficulty()))
            .tags(managedTags)
            .isActive(true)
            .createdDatetime(LocalDateTime.now())
            .lastUpdatedDatetime(LocalDateTime.now())
            .build();

        Block createdBlock = blockRepository.save(block);

        BlockVersion version = BlockVersion.builder()
                .block(createdBlock)
                .content(parseContent(request.getContent()))
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

        pointsService.addPoints(owner, 1L);

        return  this.toResponseDTOWithLikes(createdBlock, createdVersion,username);

    }


    @Override
    @Transactional
    public BlockResponseDTO newVersion(String username, BlockVersionCreateRequestDTO request) {

        User owner = findUserByUsername(username);

        Block parent = blockRepository.findById(request.getBlockId())
                .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));


        if(!Objects.equals(parent.getOwner().getId(), owner.getId())) {
            throw new ResourceNotFoundException("block_version.not_author");
        }


        BlockVersion version = BlockVersion.builder()
                .block(parent)
                .content(parseContent(request.getContent()))
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

        return toResponseDTOWithLikes(parent, createdVersion, username);

    }

    @Override
    public BlockResponseDTO getBlock(Long id, String username) {

        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

        if(!block.getCurrentVersion().getIsActive()) {
            throw new ResourceNotFoundException("block_version.not_found");
        }

        return toResponseDTOWithLikes(block, block.getCurrentVersion(), username);
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

        return toResponseDTOWithLikes(block.get(), block.get().getCurrentVersion(), username);
    }

    @Override
    public BlockResponseDTO getBlockByVersion(Long id, String username) {

        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

        BlockVersion version = blockVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

        if(!version.getIsActive()) {
            throw new ResourceNotFoundException("block_version.not_found");
        }

        return toResponseDTOWithLikes(block, version, username);
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

        List<BlockCompleteResponseDTO> parents = new ArrayList<>();
        BlockCompleteResponseDTO current = getBlockVersion(id, username);

        Long parentId = current.getParent() != null ? current.getParent().getId() : null;
        while (parentId != null) {
            BlockCompleteResponseDTO parent = getBlockVersion(parentId, username);
            parents.add(0, parent);
            parentId = parent.getParent() != null ? parent.getParent().getId() : null;
        }

        List<Block> sons = blockRepository.findByParentBlock_Id(current.getId());
        List<BlockResponseDTO> sonsDTO = new ArrayList<>();
        for(Block son : sons) {
            if(son.getCurrentVersion().getIsActive()) {
                sonsDTO.add(toResponseDTOWithLikes(son, son.getCurrentVersion(), username));
            }
        }

        return BlockCompleteTreeResponseDTO.builder()
                .parents(parents)
                .block(current)
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

            BlockVersion parentVersion = blockVersionRepository
                    .findBlockVersionsByBlockId(parentBlock.getId())
                    .stream()
                    .filter(v -> v.getIsActive())
                    .findFirst()
                    .orElse(null);

            if (parentVersion != null) {
                parent = toResponseDTOWithLikes(parentBlock, parentVersion, username);
            }
        }


        long likeCount = blockLikeRepository.countByBlockIdAndIsActiveTrue(block.getId());
        boolean likedByCurrentUser = blockLikeRepository.existsByUserUsernameAndBlockIdAndIsActiveTrue(username, block.getId());

        return BlockMapper.toCompleteResponseDTO(block, versions, parent, likeCount, likedByCurrentUser);


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

            BlockVersion version = blockVersionRepository.findById(block.getCurrentVersion().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("block_version.not_found"));

            responseDTOS.add(toResponseDTOWithLikes(block, version, username));

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

    @Override
    @Transactional
    public MessageDTO likeBlock(String username, Long blockId) {
        if (blockLikeRepository.existsByUserUsernameAndBlockIdAndIsActiveTrue(username, blockId)) {
            return MessageDTO.builder()
                    .success(true)
                    .message("discussion.message.like_added")
                    .build();
        }

        User user = findUserByUsername(username);
        Block block = blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("block.not_found"));

        BlockLike like = BlockLike.builder()
                .user(user)
                .block(block)
                .isActive(true)
                .createdDatetime(LocalDateTime.now())
                .lastUpdatedDatetime(LocalDateTime.now())
                .build();

        blockLikeRepository.save(like);

        pointsService.addPoints(block.getOwner(), 3L);

        return MessageDTO.builder()
                .success(true)
                .message("block.like_added")
                .build();
    }

    @Override
    @Transactional
    public MessageDTO unlikeBlock(String username, Long blockId) {
        var likeOpt = blockLikeRepository.findByUserUsernameAndBlockIdAndIsActiveTrue(username, blockId);

        if (likeOpt.isEmpty()) {
            throw new IllegalArgumentException("block.not_liked");
        }

        BlockLike like = likeOpt.get();
        User blockOwner = like.getBlock().getOwner();

        like.setIsActive(false);
        like.setLastUpdatedDatetime(LocalDateTime.now());
        blockLikeRepository.save(like);

        pointsService.deductPoints(blockOwner, 3L);

        return MessageDTO.builder()
                .success(true)
                .message("block.like_removed")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BlockStatsDTO getBlockStats(Long blockId) {
        long likeCount = blockLikeRepository.countByBlockIdAndIsActiveTrue(blockId);
        long forkCount = blockRepository.countForksByParentId(blockId);
        long versionCount = blockRepository.countVersionsByBlockId(blockId);
        long usedInCourses = courseBlockRepository.countByBlockId(blockId);
        return BlockStatsDTO.builder()
                .likeCount(likeCount).forkCount(forkCount)
                .versionCount(versionCount).usedInCourses(usedInCourses).build();
    }

    @Override
    @Transactional(readOnly = true)
    public BlockPageResponseDTO exploreBlocks(String username, int page, String query, List<String> tags, String difficulty, Boolean mine, Boolean following, Boolean liked) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT b FROM Block b WHERE b.isActive = true");
        Map<String, Object> params = new java.util.HashMap<>();

        if (query != null && !query.isBlank()) {
            jpql.append(" AND (LOWER(b.name) LIKE :q OR LOWER(b.slug) LIKE :q)");
            params.put("q", "%" + query.toLowerCase() + "%");
        }
        if (tags != null && !tags.isEmpty()) {
            jpql.append(" AND EXISTS (SELECT 1 FROM b.tags t WHERE LOWER(t.name) IN :tags)");
            params.put("tags", tags.stream().map(String::toLowerCase).collect(Collectors.toList()));
        }
        if (difficulty != null && !difficulty.isBlank()) {
            jpql.append(" AND b.difficulty = :diff");
            params.put("diff", Difficulty.valueOf(difficulty.toUpperCase()));
        }

        User currentUser = findUserByUsername(username);
        if (Boolean.TRUE.equals(mine)) {
            jpql.append(" AND b.owner.id = :ownerId");
            params.put("ownerId", currentUser.getId());
        }
        if (Boolean.TRUE.equals(following)) {
            List<Friend> friends = friendRepository.findConfirmedFriends(currentUser);
            Set<Long> friendIds = friends.stream().map(f ->
                f.getSender().getId().equals(currentUser.getId()) ? f.getReceiver().getId() : f.getSender().getId()
            ).collect(Collectors.toSet());
            friendIds.add(currentUser.getId());
            jpql.append(" AND b.owner.id IN :friendIds");
            params.put("friendIds", new ArrayList<>(friendIds));
        }
        if (Boolean.TRUE.equals(liked)) {
            jpql.append(" AND EXISTS (SELECT 1 FROM BlockLike bl WHERE bl.block = b AND bl.user.id = :uid AND bl.isActive = true)");
            params.put("uid", currentUser.getId());
        }

        jpql.append(" ORDER BY b.createdDatetime DESC");
        var q = entityManager.createQuery(jpql.toString(), Block.class);
        params.forEach(q::setParameter);
        q.setFirstResult(page * PAGE_SIZE);
        q.setMaxResults(PAGE_SIZE);

        List<Block> blocks = q.getResultList();
        List<BlockResponseDTO> dtos = blocks.stream().map(b -> toResponseDTOWithLikes(b, b.getCurrentVersion(), username)).collect(Collectors.toList());
        long total = blocks.size();
        return BlockPageResponseDTO.builder().blocks(dtos).totalElements(total).hasMore(total == PAGE_SIZE).currentPage(page).build();
    }

    private BlockResponseDTO toResponseDTOWithLikes(Block block, BlockVersion version, String username) {
        long likeCount = blockLikeRepository.countByBlockIdAndIsActiveTrue(block.getId());
        boolean likedByCurrentUser = blockLikeRepository.existsByUserUsernameAndBlockIdAndIsActiveTrue(username, block.getId());
        return BlockMapper.toResponseDTO(block, version, likeCount, likedByCurrentUser);
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

    private String generateUniqueSlug(String name, String suffix) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        if (suffix != null) {
            base = base + suffix;
        }
        String slug = base;
        int counter = 0;
        while (blockRepository.findBySlug(slug).isPresent()) {
            counter++;
            slug = base + "-" + counter;
        }
        return slug;
    }

    private JsonNode parseContent(String content) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid block content."
            );
        }
    }
}
