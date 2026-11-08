package com.music.JunStudio.security;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class NavigationLayoutTests {
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter securityFilter;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
    }

    @Test
    void navigationUsesBootstrapFiveOnAnonymousAndAuthenticatedPages() throws Exception {
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@example.invalid", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        for (MockHttpSession requestSession : List.of(new MockHttpSession(), session)) {
            mvc.perform(get("/about").session(requestSession))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("name=\"viewport\"")))
                    .andExpect(content().string(containsString("data-bs-toggle=\"collapse\"")))
                    .andExpect(content().string(containsString("data-bs-target=\"#navbarNav\"")))
                    .andExpect(content().string(containsString("aria-expanded=\"false\"")))
                    .andExpect(content().string(containsString("/css/bootstrap.css")))
                    .andExpect(content().string(containsString("/js/bootstrap.bundle.js")));
        }

        mvc.perform(get("/css/bootstrap.css")).andExpect(status().isOk());
        mvc.perform(get("/js/bootstrap.bundle.js")).andExpect(status().isOk());
    }
}
