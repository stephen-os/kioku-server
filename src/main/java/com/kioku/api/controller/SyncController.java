package com.kioku.api.controller;

import com.kioku.api.dto.sync.SyncPayload;
import com.kioku.api.dto.sync.SyncPullResponse;
import com.kioku.api.dto.sync.SyncPushResponse;
import com.kioku.api.security.CurrentUser;
import com.kioku.api.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The content API.
 *
 * <p>Two endpoints carry every deck, tag and card, for every client. There is
 * no per-entity REST surface: a client owns a full local replica, so "add a
 * tag to this card" is a local edit that ships on the next push rather than a
 * round trip.
 *
 * <p>Ownership always comes from the authenticated caller. Nothing in a
 * payload can move data between users.
 */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Returns everything changed since the client's cursor.
     *
     * @param since the last mark this client received; omit for a full sync
     */
    @GetMapping
    public ResponseEntity<SyncPullResponse> pull(@CurrentUser UUID userId,
                                                 @RequestParam(defaultValue = "0") long since) {
        logger.debug("Pull request from user={} since={}", userId, since);
        return ResponseEntity.ok(syncService.pull(userId, Math.max(since, 0L)));
    }

    /**
     * Applies the client's local changes.
     *
     * <p>Idempotent: re-sending an entity whose {@code updatedAt} matches the
     * stored copy changes nothing, which is what makes retrying after a
     * timeout safe.
     */
    @PostMapping
    public ResponseEntity<SyncPushResponse> push(@CurrentUser UUID userId,
                                                 @RequestBody SyncPayload changes) {
        logger.debug("Push request from user={}", userId);
        return ResponseEntity.ok(syncService.push(userId, changes));
    }
}
