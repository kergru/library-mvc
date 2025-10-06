package org.kergru.library.web;

import org.kergru.library.service.LibraryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/library/ui")
public class LibraryController {

  private final LibraryService libraryService;

  public LibraryController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @ModelAttribute
  public void addCommonAttributes(Model model, @AuthenticationPrincipal OidcUser user) {
    if (user != null) {
      model.addAttribute("userFullName",
          user.getFullName() != null ? user.getFullName() : user.getPreferredUsername());
    }
  }

  @GetMapping("/me")
  public String me(Model model, @AuthenticationPrincipal OidcUser user) {
    var userDto = libraryService.getUserWithLoans(user.getPreferredUsername());
    if (userDto.isPresent()) {
      model.addAttribute("userWithLoans", userDto.get());
      return "users/detail";
    } else {
      model.addAttribute("userName", user.getSubject());
      return "error/404";
    }
  }

  @GetMapping("/books")
  public String listAllBooks(Model model) {
    model.addAttribute("books", libraryService.getAllBooks());
    return ("books/list");
  }

  @GetMapping("/books/{isbn}")
  public String showBook(@PathVariable String isbn, Model model) {
    var bookDto = libraryService.getBookByIsbn(isbn);
    if (bookDto.isPresent()) {
      model.addAttribute("book", bookDto.get());
      return "books/detail";
    } else {
      model.addAttribute("isbn", isbn);
      return "error/404";
    }
  }
}