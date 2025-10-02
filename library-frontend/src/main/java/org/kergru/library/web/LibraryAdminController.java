package org.kergru.library.web;

import org.kergru.library.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/library/ui/admin")
public class LibraryAdminController {

  private final LibraryService libraryService;

  public LibraryAdminController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping("/users")
  public String listAllUsers(Model model) {
    model.addAttribute("users", libraryService.getAllUsers());
    return "users/list";
  }

  @GetMapping("/users/{userName}")
  public String getUser(@PathVariable String userName, Model model) {
    var userDto = libraryService.getUserWithLoans(userName);
    if (userDto.isPresent()) {
      model.addAttribute("userWithLoans", userDto.get());
      return "users/detail";
    } else {
      model.addAttribute("userName", userName);
      return "error/404";
    }
  }
}
