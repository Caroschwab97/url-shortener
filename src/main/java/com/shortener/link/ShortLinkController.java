package com.shortener.link;

import com.shortener.link.dto.CreateLinkRequest;
import com.shortener.link.dto.LinkResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class ShortLinkController {

    private final ShortLinkService service;

    public ShortLinkController(ShortLinkService service) {
        this.service = service;
    }

    /** Crea un link corto. */
    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
        ShortLink link = service.create(request.url());
        LinkResponse body = LinkResponse.from(link, baseUrl());
        return ResponseEntity
                .created(URI.create(baseUrl() + "/api/links/" + link.getCode()))
                .body(body);
    }

    /** Devuelve los datos y estadísticas de un link. */
    @GetMapping("/api/links/{code}")
    public LinkResponse get(@PathVariable String code) {
        return LinkResponse.from(service.get(code), baseUrl());
    }

    /** Redirige a la URL original. Solo matchea códigos alfanuméricos para no pisar otras rutas. */
    @GetMapping("/{code:[a-zA-Z0-9]+}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }

    private String baseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
    }
}
