package rdhuht.github.playtimelimiter.exceptions;

import java.util.UUID;

public class UnknownPlayerException extends Exception {
    private static final long serialVersionUID = -5987543214085051018L;

    public UnknownPlayerException(UUID uuid) {
        super("Unknown player with uuid of " + uuid);
    }
}
