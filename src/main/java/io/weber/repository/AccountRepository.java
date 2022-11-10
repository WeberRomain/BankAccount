package io.weber.repository;

import java.util.UUID;

public interface AccountRepository {
    boolean ifAccountExist(UUID id);
}
