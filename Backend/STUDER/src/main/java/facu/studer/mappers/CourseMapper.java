package facu.studer.mappers;

import facu.studer.DTOs.courses.CourseBlockResponseDTO;
import facu.studer.DTOs.courses.CourseResponseDTO;
import facu.studer.DTOs.blocks.BlockVersionResponseDTO;
import facu.studer.entities.Tag;
import facu.studer.entities.blocks.BlockVersion;
import facu.studer.entities.courses.Course;
import facu.studer.entities.courses.CourseBlock;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CourseMapper {
    private CourseMapper() {}

    public static CourseResponseDTO toResponseDTO(Course course, boolean isFavourite, long favouriteCount) {
        return toResponseDTO(course, isFavourite, favouriteCount, null, Map.of());
    }

    public static CourseResponseDTO toResponseDTO(Course course, boolean isFavourite, long favouriteCount,
                                                   List<CourseBlock> blocks) {
        return toResponseDTO(course, isFavourite, favouriteCount, blocks, Map.of());
    }

    public static CourseResponseDTO toResponseDTO(Course course, boolean isFavourite, long favouriteCount,
                                                   List<CourseBlock> blocks,
                                                   Map<Long, Boolean> blockCompletedMap) {
        if (course == null) return null;

        List<String> tagNames = course.getTags() != null
                ? course.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                : List.of();

        List<CourseBlockResponseDTO> blockDTOs = null;
        if (blocks != null) {
            blockDTOs = blocks.stream()
                    .map(cb -> toCourseBlockDTO(cb, blockCompletedMap.getOrDefault(cb.getId(), false)))
                    .collect(Collectors.toList());
        }

        return CourseResponseDTO.builder()
                .id(course.getId())
                .owner(UserMapper.toPublicSimpleResponseDTO(course.getOwner()))
                .name(course.getName())
                .slug(course.getSlug())
                .tags(tagNames)
                .link(course.getLink())
                .published(course.getPublished())
                .createdDatetime(course.getCreatedDatetime())
                .lastUpdatedDatetime(course.getLastUpdatedDatetime())
                .favourite(isFavourite)
                .favouriteCount(favouriteCount)
                .ratingSum(course.getRatingSum())
                .ratingCount(course.getRatingCount())
                .blocks(blockDTOs)
                .build();
    }

    private static CourseBlockResponseDTO toCourseBlockDTO(CourseBlock courseBlock, boolean completed) {
        BlockVersion version = courseBlock.getVersion() != null
                ? courseBlock.getVersion()
                : courseBlock.getBlock().getCurrentVersion();

        BlockVersionResponseDTO versionDTO = BlockVersionResponseDTO.builder()
                .id(version.getId())
                .createdDatetime(version.getCreatedDatetime())
                .lastUpdatedDatetime(version.getLastUpdatedDatetime())
                .content(String.valueOf(version.getContent()))
                .versionNumber(version.getVersionNumber())
                .build();

        return CourseBlockResponseDTO.builder()
                .id(courseBlock.getId())
                .blockId(courseBlock.getBlock().getId())
                .blockName(courseBlock.getBlock().getName())
                .version(versionDTO)
                .order(courseBlock.getBlockOrder())
                .completed(completed)
                .build();
    }
}
