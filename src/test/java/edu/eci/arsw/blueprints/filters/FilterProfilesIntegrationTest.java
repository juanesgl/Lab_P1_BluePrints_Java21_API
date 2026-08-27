package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FilterProfilesIntegrationTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("redundancy")
    class RedundancyProfileTest {
        @Autowired
        private BlueprintsFilter filter;

        @Test
        void shouldInjectRedundancyFilter() {
            assertNotNull(filter);
            assertInstanceOf(RedundancyFilter.class, filter);
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("undersampling")
    class UndersamplingProfileTest {
        @Autowired
        private BlueprintsFilter filter;

        @Test
        void shouldInjectUndersamplingFilter() {
            assertNotNull(filter);
            assertInstanceOf(UndersamplingFilter.class, filter);
        }
    }

    @Nested
    @SpringBootTest
    class DefaultProfileTest {
        @Autowired
        private BlueprintsFilter filter;

        @Test
        void shouldInjectIdentityFilterByDefault() {
            assertNotNull(filter);
            assertInstanceOf(IdentityFilter.class, filter);
        }
    }
}
