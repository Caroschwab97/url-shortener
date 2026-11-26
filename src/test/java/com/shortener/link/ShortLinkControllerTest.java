package com.shortener.link;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShortLinkControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ShortLinkRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsShortLink() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"https://github.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.code").value(matchesPattern("[a-zA-Z0-9]{7}")))
                .andExpect(jsonPath("$.shortUrl").value(matchesPattern("http://localhost/[a-zA-Z0-9]{7}")))
                .andExpect(jsonPath("$.originalUrl").value("https://github.com"))
                .andExpect(jsonPath("$.clicks").value(0));
    }

    @Test
    void redirectsAndCountsClicks() throws Exception {
        String code = repository.save(new ShortLink("abc1234", "https://example.com/page")).getCode();

        mvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/page"));
        mvc.perform(get("/" + code)).andExpect(status().isFound());

        mvc.perform(get("/api/links/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clicks").value(2))
                .andExpect(jsonPath("$.shortUrl").value(endsWith("/abc1234")));
    }

    @Test
    void returns404ForUnknownCode() throws Exception {
        mvc.perform(get("/noexiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Link no encontrado"));

        mvc.perform(get("/api/links/noexiste")).andExpect(status().isNotFound());
    }

    @Test
    void rejectsBlankUrl() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("url: es obligatoria"));
    }

    @Test
    void rejectsNonHttpUrls() throws Exception {
        for (String bad : new String[] {"ftp://files.com", "javascript:alert(1)", "github.com", "http://"}) {
            mvc.perform(post("/api/links")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"url\": \"" + bad + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("URL inválida"));
        }
    }
}
