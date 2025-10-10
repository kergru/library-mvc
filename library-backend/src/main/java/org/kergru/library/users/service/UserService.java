package org.kergru.library.users.service;

import java.util.Optional;
import java.util.stream.Collectors;
import org.kergru.library.model.PageResponseDto;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

  private UserDto toDto(UserEntity e) {
    return new UserDto(e.getUsername(), e.getFirstname(), e.getLastname(), e.getEmail());
  }
}
