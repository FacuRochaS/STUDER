package facu.studer.mappers;

import facu.studer.DTOs.blocks.BlockCompleteResponseDTO;
import facu.studer.DTOs.blocks.BlockCompleteTreeResponseDTO;
import facu.studer.DTOs.blocks.BlockResponseDTO;
import facu.studer.DTOs.blocks.BlockVersionResponseDTO;
import facu.studer.DTOs.discussions.DiscussionResponseDTO;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.Block;
import facu.studer.entities.blocks.BlockVersion;

import java.util.List;

public final class BlockMapper {
    private BlockMapper() {}

    public static BlockResponseDTO toResponseDTO(
            Block block,
            BlockVersion blockVersion) {

        Boolean isFork = !(block.getParentBlock() == null);

        BlockVersionResponseDTO versionResponseDTO = BlockVersionResponseDTO.builder()
                .id(blockVersion.getId())
                .lastUpdatedDatetime(blockVersion.getLastUpdatedDatetime())
                .createdDatetime(blockVersion.getCreatedDatetime())
                .content(blockVersion.getContent())
                .versionNumber(blockVersion.getVersionNumber())
                .build();;

        return BlockResponseDTO.builder()
                .owner(UserMapper.toPublicResponseDTO(block.getOwner()))
                .isFork(isFork)
                .name(block.getName())
                .slug(block.getSlug())
                .difficulty(String.valueOf(block.getDifficulty()))
                .tags(block.getTags().stream().map(Tag::getName).toList())
                .version(versionResponseDTO)
                .build();



    }

    public static BlockCompleteResponseDTO toCompleteResponseDTO(
            Block block,
            List<BlockVersion> blockVersions,
            BlockResponseDTO parent) {

        Boolean isFork = !(block.getParentBlock() == null);

        List<BlockVersionResponseDTO> versionResponseDTO = blockVersions.stream().map(blockVersion -> BlockVersionResponseDTO.builder()
                .id(blockVersion.getId())
                .lastUpdatedDatetime(blockVersion.getLastUpdatedDatetime())
                .createdDatetime(blockVersion.getCreatedDatetime())
                .content(blockVersion.getContent())
                .versionNumber(blockVersion.getVersionNumber())
                .build()).toList();



        return BlockCompleteResponseDTO.builder()
                .owner(UserMapper.toPublicResponseDTO(block.getOwner()))
                .isFork(isFork)
                .name(block.getName())
                .slug(block.getSlug())
                .difficulty(String.valueOf(block.getDifficulty()))
                .tags(block.getTags().stream().map(Tag::getName).toList())
                .versions(versionResponseDTO)
                .parent(parent)
                .build();



    }

    public static BlockCompleteTreeResponseDTO toTreeResponseDTO(
            BlockCompleteResponseDTO parent,
            BlockCompleteResponseDTO block,
            List<BlockResponseDTO> sons) {

        return BlockCompleteTreeResponseDTO.builder()
                .parent(parent)
                .block(block)
                .sons(sons)
                .build();

    }


}
