package org.kergru.library.users.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public Optional<UserDto> getUser(String userName) {
    return repository.findByUsername(userName).map(this::toDto);
  }

  public PageResponseDto<UserDto> searchUsers(String searchString, int page, int size, String sortBy) {

    var usersPage = repository.searchUsersPaged(
        StringUtils.hasText(searchString) ? searchString : null,
        PageRequest.of(page, size, Sort.by(sortBy)));
    return new PageResponseDto<>(
        usersPage.getContent().stream().map(this::toDto).collect(Collectors.toList()),
        usersPage.getNumber(),
        usersPage.getSize(),
        usersPage.getTotalPages(),
        usersPage.getTotalElements(),
        usersPage.isFirst(),
        usersPage.isLast(),
        usersPage.getNumberOfElements(),
        usersPage.isEmpty()
    );
  }

  public UserDto createUser(UserDto user) {
    Optional<UserEntity> existingUser = repository.findByUsernameOrEmail(user.userName(), user.email());
    if (existingUser.isPresent()) {
      throw new UserAlreadyExistsException(
          existingUser.get().getUsername().equals(user.userName()),
          existingUser.get().getEmail().equals(user.email()));
    }

    UserEntity userEntity = new UserEntity(null, user.userName(), user.firstName(), user.lastName(), user.email());
    repository.save(userEntity);
    return toDto(userEntity);
  }

  @Transactional
  public void deleteUser(String userName) {
    repository.deleteByUsername(userName);
  }

  private UserDto toDto(UserEntity e) {
    return new UserDto(e.getUsername(), e.getFirstname(), e.getLastname(), e.getEmail());
  }

  public static class UserAlreadyExistsException extends RuntimeException {
    private final boolean usernameExists;
    private final boolean emailExists;

    public UserAlreadyExistsException(boolean usernameExists, boolean emailExists) {
      super("User already exists");
      this.usernameExists = usernameExists;
      this.emailExists = emailExists;
    }

    public String getHints() {
      return Stream.of(
          usernameExists ? "username" : null,
          emailExists ? "email" : null)
          .filter(Objects::nonNull)
          .collect(Collectors.joining(", "));
    }
  }
}
