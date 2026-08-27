package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedundancyFilterTest {

    private RedundancyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RedundancyFilter();
    }

    @Test
    void shouldEliminateConsecutiveDuplicatePoints() {
        List<Point> points = List.of(
                new Point(10, 10),
                new Point(10, 10),
                new Point(20, 20),
                new Point(20, 20),
                new Point(30, 30),
                new Point(30, 30)
        );
        Blueprint bp = new Blueprint("john", "house", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(3, filtered.getPoints().size());
        assertEquals(new Point(10, 10), filtered.getPoints().get(0));
        assertEquals(new Point(20, 20), filtered.getPoints().get(1));
        assertEquals(new Point(30, 30), filtered.getPoints().get(2));
    }

    @Test
    void shouldPreserveNonConsecutiveDuplicatePoints() {
        List<Point> points = List.of(
                new Point(10, 10),
                new Point(20, 20),
                new Point(10, 10),
                new Point(30, 30),
                new Point(10, 10)
        );
        Blueprint bp = new Blueprint("john", "polygon", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(5, filtered.getPoints().size());
        assertEquals(points, filtered.getPoints());
    }

    @Test
    void shouldHandleEmptyPoints() {
        Blueprint bp = new Blueprint("john", "empty", List.of());
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertTrue(filtered.getPoints().isEmpty());
    }

    @Test
    void shouldHandleSinglePoint() {
        Blueprint bp = new Blueprint("john", "dot", List.of(new Point(5, 5)));
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(1, filtered.getPoints().size());
        assertEquals(new Point(5, 5), filtered.getPoints().get(0));
    }

    @Test
    void shouldReduceAllIdenticalPointsToOne() {
        List<Point> points = List.of(
                new Point(1, 1),
                new Point(1, 1),
                new Point(1, 1),
                new Point(1, 1)
        );
        Blueprint bp = new Blueprint("john", "single-spot", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(1, filtered.getPoints().size());
        assertEquals(new Point(1, 1), filtered.getPoints().get(0));
    }
}
