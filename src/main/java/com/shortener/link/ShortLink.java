package com.shortener.link;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "short_links")
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private long clicks;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected ShortLink() {
        // requerido por JPA
    }

    public ShortLink(String code, String originalUrl) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.clicks = 0;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getClicks() {
        return clicks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
