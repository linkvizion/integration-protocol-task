package org.snebesnyi;

public record PlayerSession(String sessionId,
                            String playerId,
                            String platformId,
                            String gameId,
                            String currency
) {
}
