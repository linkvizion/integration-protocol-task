package org.snebesnyi.auth;

public record PlayerSession(String sessionId,
                            String playerId,
                            String platformId,
                            String gameId,
                            String currency
) {
}
