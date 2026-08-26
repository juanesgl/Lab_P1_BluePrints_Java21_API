package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

@Repository
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbcTemplate;

    public PostgresBlueprintPersistence(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        String insertBpSql = "INSERT INTO blueprints (author, name) VALUES (?, ?)";
        try {
            jdbcTemplate.update(insertBpSql, bp.getAuthor(), bp.getName());
        } catch (DuplicateKeyException e) {
            throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + "/" + bp.getName());
        }

        String insertPtSql = "INSERT INTO points (author, blueprint_name, x, y, point_order) VALUES (?, ?, ?, ?, ?)";
        List<Point> points = bp.getPoints();
        List<Object[]> batch = IntStream.range(0, points.size())
                .mapToObj(i -> new Object[]{
                        bp.getAuthor(),
                        bp.getName(),
                        points.get(i).x(),
                        points.get(i).y(),
                        i
                })
                .toList();
        jdbcTemplate.batchUpdate(insertPtSql, batch);
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        String sql = """
                SELECT b.author, b.name, p.x, p.y
                FROM blueprints b
                LEFT JOIN points p ON p.author = b.author AND p.blueprint_name = b.name
                WHERE b.author = ? AND b.name = ?
                ORDER BY p.point_order ASC
                """;
        Blueprint bp = jdbcTemplate.query(sql, SINGLE_BLUEPRINT_EXTRACTOR, author, name);
        if (bp == null) {
            throw new BlueprintNotFoundException("Blueprint not found: " + author + "/" + name);
        }
        return bp;
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        String sql = """
                SELECT b.author, b.name, p.x, p.y
                FROM blueprints b
                LEFT JOIN points p ON p.author = b.author AND p.blueprint_name = b.name
                WHERE b.author = ?
                ORDER BY b.name, p.point_order ASC
                """;
        Set<Blueprint> set = new HashSet<>(Objects.requireNonNull(jdbcTemplate.query(sql, LIST_EXTRACTOR, author)));
        if (set.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        return set;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        String sql = """
                SELECT b.author, b.name, p.x, p.y
                FROM blueprints b
                LEFT JOIN points p ON p.author = b.author AND p.blueprint_name = b.name
                ORDER BY b.author, b.name, p.point_order ASC
                """;
        return new HashSet<>(Objects.requireNonNull(jdbcTemplate.query(sql, LIST_EXTRACTOR)));
    }

    @Override
    @Transactional
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM blueprints WHERE author = ? AND name = ?)",
                Boolean.class, author, name);
        if (!Boolean.TRUE.equals(exists)) {
            throw new BlueprintNotFoundException("Blueprint not found: " + author + "/" + name);
        }
        Integer nextOrder = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(point_order), -1) + 1 FROM points WHERE author = ? AND blueprint_name = ?",
                Integer.class, author, name);
        jdbcTemplate.update(
                "INSERT INTO points (author, blueprint_name, x, y, point_order) VALUES (?, ?, ?, ?, ?)",
                author, name, x, y, nextOrder);
    }

    private final ResultSetExtractor<Blueprint> SINGLE_BLUEPRINT_EXTRACTOR = rs -> {
        if (!rs.next()) return null;
        Blueprint bp = new Blueprint(rs.getString("author"), rs.getString("name"), new ArrayList<>());
        do {
            addPointRow(bp, rs);
        } while (rs.next());
        return bp;
    };

    private final ResultSetExtractor<List<Blueprint>> LIST_EXTRACTOR = rs -> {
        Map<String, Blueprint> byKey = new LinkedHashMap<>();
        while (rs.next()) {
            String author = rs.getString("author");
            String name = rs.getString("name");
            Blueprint bp = byKey.computeIfAbsent(author + "/" + name,
                    k -> new Blueprint(author, name, new ArrayList<>()));
            addPointRow(bp, rs);
        }
        return new ArrayList<>(byKey.values());
    };

    private void addPointRow(Blueprint bp, ResultSet rs) throws SQLException {
        int x = rs.getInt("x");
        int y = rs.getInt("y");
        if (!rs.wasNull()) {
            bp.addPoint(new Point(x, y));
        }
    }
}
