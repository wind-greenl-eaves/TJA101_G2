package com.eatfast.employee.controller;

import com.eatfast.common.enums.AccountStatus;
import com.eatfast.common.enums.EmployeeRole;
import com.eatfast.common.enums.Gender;
import com.eatfast.employee.dto.CreateEmployeeRequest;
import com.eatfast.employee.dto.UpdateEmployeeRequest;
import com.eatfast.employee.model.EmployeeEntity;
import com.eatfast.employee.repository.EmployeeRepository;
import com.eatfast.store.model.StoreEntity;
import com.eatfast.store.repository.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("員工 API 端點整合測試")
class Demo_EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private StoreRepository storeRepository;
    
    private StoreEntity testStore;
    private EmployeeEntity testEmployee;
    
    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        storeRepository.deleteAll();

        testStore = new StoreEntity();
        testStore.setStoreName("總部測試店");
        testStore.setStoreLoc("測試地址");
        testStore.setStorePhone("0912345678");
        testStore.setStoreTime("09:00-21:00");
        testStore = storeRepository.saveAndFlush(testStore);

        testEmployee = new EmployeeEntity();
        testEmployee.setUsername("王大明");
        testEmployee.setAccount("david.wang");
        testEmployee.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        testEmployee.setEmail("david.wang@example.com");
        testEmployee.setPhone("0912345678");
        testEmployee.setNationalId("A123456789");
        testEmployee.setRole(EmployeeRole.STAFF);
        testEmployee.setStatus(AccountStatus.ACTIVE);
        testEmployee.setGender(Gender.M);
        testEmployee.setStore(testStore);
        testEmployee = employeeRepository.saveAndFlush(testEmployee);
    }
    
    @Test
    @DisplayName("C - 新增員工: 應成功建立並回傳 201 CREATED")
    void whenCreateEmployee_thenSuccess() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setUsername("陳小美");
        request.setAccount("may.chen");
        request.setPassword("password123");
        request.setEmail("may.chen@example.com");
        request.setPhone("0987654321");
        request.setNationalId("F298765432");
        request.setRole(EmployeeRole.STAFF);
        request.setGender(Gender.F);
        request.setStoreId(testStore.getStoreId());
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId", notNullValue()))
                .andExpect(jsonPath("$.username", is("陳小美")))
                .andExpect(jsonPath("$.account", is("may.chen")))
                .andDo(print());
    }

    @Test
    @DisplayName("R - 查詢單一員工: 應成功找到並回傳 200 OK")
    void givenEmployeeExists_whenGetEmployeeById_thenSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/employees/" + testEmployee.getEmployeeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", is(testEmployee.getEmployeeId().intValue())))
                .andExpect(jsonPath("$.username", is("王大明")))
                .andExpect(jsonPath("$.storeName", is("總部測試店")))
                .andDo(print());
    }
    
    @Test
    @DisplayName("R - 複合查詢: 應根據條件篩選出正確員工")
    void whenSearchEmployees_withParams_thenSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/employees")
                .param("username", "王大明")
                .param("role", "STAFF")
                .param("storeId", testStore.getStoreId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("王大明")))
                .andDo(print());
    }

    @Test
    @DisplayName("U - 更新員工: 應成功更新並回傳 200 OK")
    void whenUpdateEmployee_thenSuccess() throws Exception {
        UpdateEmployeeRequest request = new UpdateEmployeeRequest();
        request.setUsername("王大明 (已更新)");
        request.setPhone("0999999999");
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/employees/" + testEmployee.getEmployeeId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("王大明 (已更新)")))
                .andExpect(jsonPath("$.phone", is("0999999999")))
                .andDo(print());
    }

    @Test
    @DisplayName("D - 刪除員工: 應成功刪除並回傳 204 NO CONTENT")
    void whenDeleteEmployee_thenSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/employees/" + testEmployee.getEmployeeId()))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}