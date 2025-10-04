package org.kergru.library.users.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  @Query("SELECT u FROM UserEntity u WHERE u.username = :userName")
  Optional<UserEntity> findByUsername(String userName);
}

