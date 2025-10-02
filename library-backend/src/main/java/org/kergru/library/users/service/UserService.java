package org.kergru.library.users.service;

import java.util.List;
import org.kergru.library.model.UserDto;
import org.kergru.library.users.repository.UserEntity;
import org.kergru.library.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public Optional<UserDto> findUserByUserName(String userName) {
    return repository.findByUsername(userName).map(this::toDto);
  }

  public List<UserDto> findAll() {
    return repository.findAll().stream().map(this::toDto).toList();
  }

  private UserDto toDto(UserEntity e) {
    return new UserDto(e.getUsername(), e.getFirstname(), e.getLastname(), e.getEmail());
  }
}
