package facu.studer.services;

import facu.studer.DTOs.blocks.*;
import jakarta.validation.Valid;

import java.util.List;

public interface BlockService {
    BlockResponseDTO create(String username, @Valid BlockCreateRequestDTO request);
    BlockResponseDTO fork(String username, @Valid BlockForkCreateRequestDTO request);
    BlockResponseDTO newVersion(String username, BlockVersionCreateRequestDTO request);

    BlockResponseDTO getBlock(Long id, String username);

    BlockResponseDTO getBlockBySlug(String slug, String username);

    BlockResponseDTO getBlockByVersion(Long id, String username);

    BlockPageResponseDTO getBlocksBySearch(String username, int page, List<String> tags, Boolean orderByLikes, String difficulty,String user,String name);

    BlockCompleteTreeResponseDTO getBlockTree(Long id, String username);

    BlockCompleteResponseDTO getBlockVersion(Long id, String username);

    BlockPageResponseDTO getBlockByUser(Long id, String username, int page);

    BlockPageResponseDTO getMyBlock(String username, int page);
}
