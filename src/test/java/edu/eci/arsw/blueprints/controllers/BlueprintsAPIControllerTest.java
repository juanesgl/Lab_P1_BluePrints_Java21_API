package edu.eci.arsw.blueprints.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BlueprintsAPIController.class, GlobalExceptionHandler.class})
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlueprintsServices services;

    @Test
    void getAllBlueprints_ShouldReturn200AndApiResponse() throws Exception {
        Blueprint bp1 = new Blueprint("john", "house", List.of(new Point(10, 10), new Point(20, 20)));
        when(services.getAllBlueprints()).thenReturn(Set.of(bp1));

        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].author").value("john"))
                .andExpect(jsonPath("$.data[0].name").value("house"));
    }

    @Test
    void getBlueprintsByAuthor_WhenFound_ShouldReturn200AndApiResponse() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(10, 10)));
        when(services.getBlueprintsByAuthor("john")).thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data[0].author").value("john"));
    }

    @Test
    void getBlueprintsByAuthor_WhenNotFound_ShouldReturn404AndApiResponse() throws Exception {
        when(services.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("No blueprints for author: unknown"));

        mockMvc.perform(get("/api/v1/blueprints/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("No blueprints for author: unknown"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getBlueprint_WhenFound_ShouldReturn200AndApiResponse() throws Exception {
        Blueprint bp = new Blueprint("john", "house", List.of(new Point(10, 10), new Point(20, 20)));
        when(services.getBlueprint("john", "house")).thenReturn(bp);

        mockMvc.perform(get("/api/v1/blueprints/john/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("execute ok"))
                .andExpect(jsonPath("$.data.author").value("john"))
                .andExpect(jsonPath("$.data.name").value("house"))
                .andExpect(jsonPath("$.data.points", hasSize(2)));
    }

    @Test
    void getBlueprint_WhenNotFound_ShouldReturn404AndApiResponse() throws Exception {
        when(services.getBlueprint("john", "nonexistent"))
                .thenThrow(new BlueprintNotFoundException("Blueprint not found: john/nonexistent"));

        mockMvc.perform(get("/api/v1/blueprints/john/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Blueprint not found: john/nonexistent"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void createBlueprint_WhenValid_ShouldReturn201AndApiResponse() throws Exception {
        Blueprint bp = new Blueprint("john", "tower", List.of(new Point(5, 5)));
        doNothing().when(services).addNewBlueprint(any(Blueprint.class));

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bp)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Blueprint created successfully"))
                .andExpect(jsonPath("$.data.author").value("john"))
                .andExpect(jsonPath("$.data.name").value("tower"));
    }

    @Test
    void createBlueprint_WhenAlreadyExists_ShouldReturn400AndApiResponse() throws Exception {
        Blueprint bp = new Blueprint("john", "tower", List.of(new Point(5, 5)));
        doThrow(new BlueprintPersistenceException("Blueprint already exists: john/tower"))
                .when(services).addNewBlueprint(any(Blueprint.class));

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Blueprint already exists: john/tower"));
    }

    @Test
    void createBlueprint_WhenInvalidPayload_ShouldReturn400AndApiResponse() throws Exception {
        Blueprint invalidBp = new Blueprint("", "", List.of());

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void addPoint_WhenFound_ShouldReturn202AndApiResponse() throws Exception {
        Point newPoint = new Point(30, 40);
        doNothing().when(services).addPoint(eq("john"), eq("house"), eq(30), eq(40));

        mockMvc.perform(put("/api/v1/blueprints/john/house/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPoint)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202))
                .andExpect(jsonPath("$.message").value("Point added successfully"))
                .andExpect(jsonPath("$.data.x").value(30))
                .andExpect(jsonPath("$.data.y").value(40));
    }

    @Test
    void addPoint_WhenBlueprintNotFound_ShouldReturn404AndApiResponse() throws Exception {
        Point newPoint = new Point(30, 40);
        doThrow(new BlueprintNotFoundException("Blueprint not found: john/nonexistent"))
                .when(services).addPoint(eq("john"), eq("nonexistent"), eq(30), eq(40));

        mockMvc.perform(put("/api/v1/blueprints/john/nonexistent/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPoint)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Blueprint not found: john/nonexistent"));
    }
}
