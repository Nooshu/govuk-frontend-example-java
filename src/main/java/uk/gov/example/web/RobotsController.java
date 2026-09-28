package uk.gov.example.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RobotsController {

  @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> robots() {
    return ResponseEntity.ok("User-agent: *\nDisallow: /\n");
  }
}
