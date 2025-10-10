package org.kergru.library.users.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  @Query("SELECT u FROM UserEntity u WHERE u.username = :userName")
  Optional<UserEntity> findByUsername(String userName);

  @Query("""
    SELECT u
    FROM UserEntity u
    WHERE 
        (:#{#searchString} IS NULL OR 
         u.username LIKE %:#{#searchString}% OR 
         u.firstname LIKE %:#{#searchString}% OR 
         u.lastname LIKE %:#{#searchString}% OR 
         u.email LIKE %:#{#searchString}%)
  """)
  Page<UserEntity> searchUsersPaged(String searchString, Pageable pageable);
}
