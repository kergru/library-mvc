package org.kergru.library.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

  @GetMapping("/")
  public String slash() {
    return "redirect:/library/ui/books"; // landing page, get intercepted by OAuth2 if not authenticated
  }
}
