package com.shortener.link.dto;

import com.shortener.link.ShortLink;
import java.time.Instant;

public record LinkResponse(
        String code,
        String shortUrl,
        String originalUrl,
        long clicks,
        Instant createdAt) {

    public static LinkResponse from(ShortLink link, String baseUrl) {
        return new LinkResponse(
                link.getCode(),
                baseUrl + "/" + link.getCode(),
                link.getOriginalUrl(),
                link.getClicks(),
                link.getCreatedAt());
    }
}
