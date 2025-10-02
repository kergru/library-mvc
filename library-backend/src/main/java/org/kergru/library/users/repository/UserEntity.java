package org.kergru.library.users.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "username", nullable = false, unique = true)
  private String username;

  @Column(name = "firstname", nullable = false)
  private String firstname
      ;
  @Column(name = "lastname", nullable = false)
  private String lastname;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  protected UserEntity() { }

  public UserEntity(Long id, String username, String firstname, String lastname, String email) {
    this.id = id;
    this.username = username;
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getFirstname() { return firstname; }
  public String getLastname() { return lastname; }
  public String getEmail() { return email; }

  public void setId(Long id) { this.id = id; }
/* <<<<<<<<<<<<<<  ✨ Windsurf Command ⭐ >>>>>>>>>>>>>>>> */
  /**
   * Sets the userName of the user.
   * @param username the new userName of the user
   */
/* <<<<<<<<<<  65a3122b-26f3-4a56-95f0-3bea84b2118b  >>>>>>>>>>> */
  public void setUsername(String username) { this.username = username; }
  public void setFirstname(String firstname) { this.firstname = firstname; }
  public void setLastname(String lastname) { this.lastname = lastname; }
  public void setEmail(String email) { this.email = email; }
}