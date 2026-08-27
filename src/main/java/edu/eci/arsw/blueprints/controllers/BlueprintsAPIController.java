package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dto.ApiResponse;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints API", description = "Endpoints for managing architectural blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    @GetMapping
    @Operation(summary = "Get all blueprints", description = "Returns the complete set of registered blueprints")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAllBlueprints() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        return ResponseEntity.ok(ApiResponse.ok("execute ok", blueprints));
    }

    @GetMapping("/{author}")
    @Operation(summary = "Get blueprints by author", description = "Returns all blueprints authored by the specified author")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getBlueprintsByAuthor(@PathVariable String author)
            throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
        return ResponseEntity.ok(ApiResponse.ok("execute ok", blueprints));
    }

    @GetMapping("/{author}/{bpname}")
    @Operation(summary = "Get a blueprint by author and name", description = "Returns a specific blueprint filtered according to the active filter profile")
    public ResponseEntity<ApiResponse<Blueprint>> getBlueprint(
            @PathVariable String author,
            @PathVariable String bpname) throws BlueprintNotFoundException {
        Blueprint blueprint = services.getBlueprint(author, bpname);
        return ResponseEntity.ok(ApiResponse.ok("execute ok", blueprint));
    }

    @PostMapping
    @Operation(summary = "Create a new blueprint", description = "Registers a new blueprint in the system")
    public ResponseEntity<ApiResponse<Blueprint>> createBlueprint(@Valid @RequestBody Blueprint blueprint)
            throws BlueprintPersistenceException {
        if (blueprint.getAuthor() == null || blueprint.getAuthor().trim().isEmpty() ||
            blueprint.getName() == null || blueprint.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Author and name must not be blank");
        }
        services.addNewBlueprint(blueprint);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Blueprint created successfully", blueprint));
    }

    @PutMapping("/{author}/{bpname}/points")
    @Operation(summary = "Add a point to a blueprint", description = "Appends a new coordinate point to an existing blueprint")
    public ResponseEntity<ApiResponse<Point>> addPoint(
            @PathVariable String author,
            @PathVariable String bpname,
            @RequestBody Point point) throws BlueprintNotFoundException {
        if (point == null) {
            throw new IllegalArgumentException("Point coordinates must not be null");
        }
        services.addPoint(author, bpname, point.getX(), point.getY());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.accepted("Point added successfully", point));
    }
}
