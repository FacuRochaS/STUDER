package facu.studer.controllers;

import facu.studer.DTOs.MessageDTO;
import facu.studer.DTOs.blocks.*;

import facu.studer.DTOs.discussions.DiscussionMessagePageResponseDTO;
import facu.studer.security.SecurityUtils;
import facu.studer.services.BlockService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
public class BlockController {

    private final BlockService blockService;
    private final SecurityUtils securityUtils;

    public BlockController(BlockService blockService, SecurityUtils securityUtils) {
        this.blockService = blockService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<BlockResponseDTO> createBlock(
            @Valid @RequestBody BlockCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        BlockResponseDTO response = blockService.create(username, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/fork")
    public ResponseEntity<BlockResponseDTO> forkBlock(
            @Valid @RequestBody BlockForkCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        BlockResponseDTO response = blockService.fork(username, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/version")
    public ResponseEntity<BlockResponseDTO> versionBlock(
            @Valid @RequestBody BlockVersionCreateRequestDTO request) {
        String username = securityUtils.requireCurrentUsername();
        BlockResponseDTO response = blockService.newVersion(username, request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<BlockResponseDTO> getBlock(
            @PathVariable Long id){

        String username = securityUtils.requireCurrentUsername();
        BlockResponseDTO response = blockService
                .getBlock(id, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlockResponseDTO> getBlock(
            @PathVariable String slug){
        String username = securityUtils.requireCurrentUsername();
        BlockResponseDTO response = blockService
                .getBlockBySlug(slug, username);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/version/{id}")
    public ResponseEntity<BlockResponseDTO> getBlockByVersion(
            @PathVariable Long id){

        String username = securityUtils.requireCurrentUsername();
        BlockResponseDTO response = blockService
                .getBlockByVersion(id, username);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/search")
    public ResponseEntity<BlockPageResponseDTO> getBlocksBySearch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Boolean orderByLikes,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String name) {

        String username = securityUtils.requireCurrentUsername();
        BlockPageResponseDTO response = blockService
                .getBlocksBySearch(username, page,tags,orderByLikes,difficulty,user,name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tree/{id}")
    public ResponseEntity<BlockCompleteTreeResponseDTO> getBlockTree(
            @PathVariable Long id){

        String username = securityUtils.requireCurrentUsername();
        BlockCompleteTreeResponseDTO response = blockService
                .getBlockTree(id, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/versions/{id}")
    public ResponseEntity<BlockCompleteResponseDTO> getBlockVersion(
            @PathVariable Long id){

        String username = securityUtils.requireCurrentUsername();
        BlockCompleteResponseDTO response = blockService
                .getBlockVersion(id, username);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{id}")
    public ResponseEntity<BlockPageResponseDTO> getBlockByUser(
            @RequestParam(defaultValue = "0") int page,
            @PathVariable Long id){

        String username = securityUtils.requireCurrentUsername();
        BlockPageResponseDTO response = blockService
                .getBlockByUser(id, username,page);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<BlockPageResponseDTO> getBlockByUser(
            @RequestParam(defaultValue = "0") int page){

        String username = securityUtils.requireCurrentUsername();
        BlockPageResponseDTO response = blockService
                .getMyBlock(username,page);
        return ResponseEntity.ok(response);
    }


    // Likear, deslikear

    @PostMapping("/{id}/like")
    public ResponseEntity<MessageDTO> likeBlock(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = blockService.likeBlock(username, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<MessageDTO> unlikeBlock(@PathVariable Long id) {
        String username = securityUtils.requireCurrentUsername();
        MessageDTO response = blockService.unlikeBlock(username, id);
        return ResponseEntity.ok(response);
    }

}
