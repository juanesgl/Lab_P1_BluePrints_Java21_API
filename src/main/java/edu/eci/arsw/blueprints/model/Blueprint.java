package edu.eci.arsw.blueprints.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "blueprints")
@IdClass(BlueprintPK.class)
public class Blueprint {

    @Id
    private String author;

    @Id
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "points",
            joinColumns = {
                    @JoinColumn(name = "blueprint_author", referencedColumnName = "author"),
                    @JoinColumn(name = "blueprint_name", referencedColumnName = "name")
            }
    )
    @OrderColumn(name = "point_order")
    private List<Point> points = new ArrayList<>();

    public Blueprint(String author, String name, List<Point> pts) {
        this.author = author;
        this.name = name;
        if (pts != null) {
            this.points.addAll(pts);
        }
    }


    public List<Point> getPoints() { 
        return Collections.unmodifiableList(points); 
    }

    public void addPoint(Point p) { 
        this.points.add(p); 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blueprint bp)) return false;
        return Objects.equals(author, bp.author) && Objects.equals(name, bp.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, name);
    }
}