package com.durhol.minesweeper.arena;

import com.durhol.minesweeper.game.GameTier;
import com.durhol.minesweeper.game.MinesweeperGame;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages active player sessions, arena allocations, and inventory stash records.
 * Portable across server platforms and loaders.
 */
public class CommonArenaManager {
    private static final CommonArenaManager INSTANCE = new CommonArenaManager();

    private final AtomicLong nextSessionId = new AtomicLong(0);
    private final Map<UUID, ArenaSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerStash> playerStashes = new ConcurrentHashMap<>();

    private CommonArenaManager() {}

    public static CommonArenaManager getInstance() {
        return INSTANCE;
    }

    public static class ArenaSession {
        private final long sessionId;
        private final UUID playerId;
        private final ArenaPos origin;
        private final MinesweeperGame game;

        public ArenaSession(long sessionId, UUID playerId, ArenaPos origin, GameTier tier) {
            this.sessionId = sessionId;
            this.playerId = playerId;
            this.origin = origin;
            this.game = new MinesweeperGame(tier);
        }

        public long getSessionId() {
            return sessionId;
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public ArenaPos getOrigin() {
            return origin;
        }

        public MinesweeperGame getGame() {
            return game;
        }
    }

    /**
     * Creates a new arena session for the given player.
     */
    public ArenaSession createSession(UUID playerId, GameTier tier) {
        long sessionId = nextSessionId.getAndIncrement();
        ArenaPos origin = ArenaGridCalculator.calculateOffset(sessionId);
        ArenaSession session = new ArenaSession(sessionId, playerId, origin, tier);
        activeSessions.put(playerId, session);
        return session;
    }

    public ArenaSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public ArenaSession removeSession(UUID playerId) {
        return activeSessions.remove(playerId);
    }

    public void stashPlayer(UUID playerId, PlayerStash stash) {
        playerStashes.put(playerId, stash);
    }

    public PlayerStash retrieveStash(UUID playerId) {
        return playerStashes.remove(playerId);
    }

    public boolean hasStash(UUID playerId) {
        return playerStashes.containsKey(playerId);
    }
}
