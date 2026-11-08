package com.music.JunStudio.security;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercises the actual Spring Security filter chain with role-specific mock sessions. */
@SpringBootTest
@ActiveProfiles("test")
class AdminUserAuthorizationTests {
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter securityFilter;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
    }

    private MockHttpSession sessionFor(String role) {
        // MockMvc does not perform login here; seed the session with the authentication under test.
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                "user@example.invalid", null, List.of(new SimpleGrantedAuthority(role))));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        return session;
    }

    @Test
    void adminUserPathsRequireAdminRole() throws Exception {
        mvc.perform(get("/admin/users/not-a-route")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/users/not-a-route").session(sessionFor("ROLE_STUDENT")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/users/not-a-route").session(sessionFor("ROLE_TEACHER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin/users/not-a-route").session(sessionFor("ROLE_ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminUserMutationsRequireAdminRoleEvenWithCsrfToken() throws Exception {
        for (String role : List.of("ROLE_STUDENT", "ROLE_TEACHER", "ROLE_ADMIN")) {
            MockHttpSession session = sessionFor(role);
            var result = mvc.perform(get("/login").session(session)).andReturn();
            var token = (CsrfToken) result.getRequest().getAttribute("_csrf");
            mvc.perform(post("/admin/users/create").session(session)
                            .param(token.getParameterName(), token.getToken()))
                    .andExpect("ROLE_ADMIN".equals(role) ? status().is3xxRedirection() : status().isForbidden());
        }
    }
}
