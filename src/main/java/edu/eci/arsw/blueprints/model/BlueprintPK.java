package edu.eci.arsw.blueprints.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlueprintPK implements Serializable {
    private String author;
    private String name;
}