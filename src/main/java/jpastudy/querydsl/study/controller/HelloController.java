package jpastudy.querydsl.study.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<String> helloQueryDSL() {
        log.debug("Hello QueryDSL");
        return ResponseEntity.ok("Hello QueryDSL");
    }
}