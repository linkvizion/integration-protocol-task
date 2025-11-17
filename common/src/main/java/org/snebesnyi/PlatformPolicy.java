
package org.snebesnyi;

import java.util.Set;

public record PlatformPolicy(String platformId,
                             Set<String> allowedGames,
                             long updatedAt) {
}
