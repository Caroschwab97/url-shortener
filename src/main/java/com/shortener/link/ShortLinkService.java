package com.shortener.link;

import com.shortener.link.exception.InvalidUrlException;
import com.shortener.link.exception.LinkNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortLinkService {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final int MAX_ATTEMPTS = 5;

    private final ShortLinkRepository repository;
    private final CodeGenerator codeGenerator;

    public ShortLinkService(ShortLinkRepository repository, CodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    @Transactional
    public ShortLink create(String rawUrl) {
        String url = normalize(rawUrl);
        return repository.save(new ShortLink(uniqueCode(), url));
    }

    @Transactional(readOnly = true)
    public ShortLink get(String code) {
        return repository.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
    }

    /** Devuelve la URL original y registra el clic. */
    @Transactional
    public String resolve(String code) {
        ShortLink link = get(code);
        repository.incrementClicks(code);
        return link.getOriginalUrl();
    }

    private String uniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = codeGenerator.generate();
            if (!repository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("No se pudo generar un código único");
    }

    private String normalize(String rawUrl) {
        String url = rawUrl.trim();
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
                throw new InvalidUrlException("La URL debe empezar con http:// o https://");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidUrlException("La URL no tiene un dominio válido");
            }
            return url;
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("La URL no tiene un formato válido");
        }
    }
}
