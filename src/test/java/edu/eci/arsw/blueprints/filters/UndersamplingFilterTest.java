package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UndersamplingFilterTest {

    private UndersamplingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new UndersamplingFilter();
    }

    @Test
    void shouldKeepOneOutOfEveryTwoPoints() {
        List<Point> points = List.of(
                new Point(0, 0),   // index 0 - keep
                new Point(1, 1),   // index 1 - skip
                new Point(2, 2),   // index 2 - keep
                new Point(3, 3),   // index 3 - skip
                new Point(4, 4),   // index 4 - keep
                new Point(5, 5)    // index 5 - skip
        );
        Blueprint bp = new Blueprint("john", "sample", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(3, filtered.getPoints().size());
        assertEquals(new Point(0, 0), filtered.getPoints().get(0));
        assertEquals(new Point(2, 2), filtered.getPoints().get(1));
        assertEquals(new Point(4, 4), filtered.getPoints().get(2));
    }

    @Test
    void shouldHandleOddNumberOfPoints() {
        List<Point> points = List.of(
                new Point(10, 10), // index 0 - keep
                new Point(20, 20), // index 1 - skip
                new Point(30, 30), // index 2 - keep
                new Point(40, 40), // index 3 - skip
                new Point(50, 50)  // index 4 - keep
        );
        Blueprint bp = new Blueprint("john", "sample-odd", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(3, filtered.getPoints().size());
        assertEquals(new Point(10, 10), filtered.getPoints().get(0));
        assertEquals(new Point(30, 30), filtered.getPoints().get(1));
        assertEquals(new Point(50, 50), filtered.getPoints().get(2));
    }

    @Test
    void shouldHandleTwoPoints() {
        List<Point> points = List.of(
                new Point(1, 1),
                new Point(2, 2)
        );
        Blueprint bp = new Blueprint("john", "pair", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(1, filtered.getPoints().size());
        assertEquals(new Point(1, 1), filtered.getPoints().get(0));
    }

    @Test
    void shouldHandleSinglePoint() {
        List<Point> points = List.of(new Point(1, 1));
        Blueprint bp = new Blueprint("john", "single", points);
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertEquals(1, filtered.getPoints().size());
        assertEquals(new Point(1, 1), filtered.getPoints().get(0));
    }

    @Test
    void shouldHandleEmptyPoints() {
        Blueprint bp = new Blueprint("john", "empty", List.of());
        Blueprint filtered = filter.apply(bp);

        assertNotNull(filtered);
        assertTrue(filtered.getPoints().isEmpty());
    }
}
