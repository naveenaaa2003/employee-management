package com.naveena.employeemanagement;

import com.naveena.employeemanagement.repository.EmployeeRepository;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    private static final String AUTH = "Basic " +
            Base64.getEncoder().encodeToString("admin:admin123".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    // Helper: creates an employee (with login) and returns the response body
    private String createEmployee(String json) throws Exception {
        return mockMvc.perform(post("/employees")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void addEmployee_withValidData_returnsCreated() throws Exception {
        String json = """
            {"name":"Test User","email":"testuser@company.com","department":"QA","salary":50000}
            """;

        mockMvc.perform(post("/employees")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void addEmployee_withoutLogin_returnsUnauthorized() throws Exception {
        String json = """
            {"name":"No Auth","email":"noauth@company.com","department":"QA","salary":50000}
            """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addEmployee_withInvalidEmail_returnsBadRequest() throws Exception {
        String json = """
            {"name":"Bad Email","email":"not-an-email","department":"QA","salary":50000}
            """;

        mockMvc.perform(post("/employees")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"));
    }

    @Test
    void addEmployee_withBlankName_returnsBadRequest() throws Exception {
        String json = """
            {"name":"","email":"blank@company.com","department":"QA","salary":50000}
            """;

        mockMvc.perform(post("/employees")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addEmployee_withDuplicateEmail_returnsConflict() throws Exception {
        String json = """
            {"name":"Dup One","email":"dup@company.com","department":"QA","salary":50000}
            """;
        createEmployee(json);

        mockMvc.perform(post("/employees")
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllEmployees_returnsOk() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.page.totalElements").exists());
    }

    @Test
    void getEmployeeById_whenExists_returnsEmployee() throws Exception {
        String response = createEmployee("""
            {"name":"Lookup Test","email":"lookup@company.com","department":"Finance","salary":60000}
            """);
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/employees/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lookup Test"));
    }

    @Test
    void getEmployeeById_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/employees/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDepartment_returnsOnlyMatching() throws Exception {
        createEmployee("""
            {"name":"Eng Person","email":"eng@company.com","department":"Engineering","salary":60000}
            """);
        createEmployee("""
            {"name":"Sales Person","email":"sales@company.com","department":"Sales","salary":55000}
            """);

        mockMvc.perform(get("/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Engineering"));
    }

    @Test
    void searchByName_returnsMatching() throws Exception {
        createEmployee("""
            {"name":"Searchable Name","email":"search@company.com","department":"IT","salary":50000}
            """);

        mockMvc.perform(get("/employees/search").param("name", "searchable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Searchable Name"));
    }

    @Test
    void getBySalaryRange_returnsMatching() throws Exception {
        createEmployee("""
            {"name":"Range Test","email":"range@company.com","department":"IT","salary":58000}
            """);

        mockMvc.perform(get("/employees/salary-range").param("min", "50000").param("max", "60000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Range Test"));
    }

    @Test
    void countByDepartment_returnsCorrectCount() throws Exception {
        createEmployee("""
            {"name":"Count Test","email":"count@company.com","department":"Legal","salary":50000}
            """);

        mockMvc.perform(get("/employees/department/Legal/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void getAllDepartments_returnsDistinctList() throws Exception {
        createEmployee("""
            {"name":"Dept Test","email":"dept@company.com","department":"Ops","salary":50000}
            """);

        mockMvc.perform(get("/employees/departments"))
                .andExpect(status().isOk());
    }

    @Test
    void updateEmployee_changesFields() throws Exception {
        String response = createEmployee("""
            {"name":"Before Update","email":"update@company.com","department":"HR","salary":40000}
            """);
        Long id = objectMapper.readTree(response).get("id").asLong();

        String updateJson = """
            {"name":"After Update","email":"update@company.com","department":"HR","salary":45000}
            """;

        mockMvc.perform(put("/employees/" + id)
                        .header("Authorization", AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After Update"))
                .andExpect(jsonPath("$.salary").value(45000));
    }

    @Test
    void deleteEmployee_whenExists_returnsNoContent() throws Exception {
        String response = createEmployee("""
            {"name":"To Delete","email":"delete@company.com","department":"IT","salary":50000}
            """);
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/employees/" + id)
                        .header("Authorization", AUTH))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_withoutLogin_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteEmployee_whenNotFound_returns404() throws Exception {
        mockMvc.perform(delete("/employees/999999")
                        .header("Authorization", AUTH))
                .andExpect(status().isNotFound());
    }
}