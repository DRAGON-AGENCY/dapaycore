package jp.co.dragonagency.dapaycore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.dragonagency.dapaycore.dto.EmployeeRequest;
import jp.co.dragonagency.dapaycore.dto.EmployeeResponse;
import jp.co.dragonagency.dapaycore.service.EmployeeService;

/**
 * EmployeeController の Web 層テスト（項番 T67〜T70）。
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String CSRF_TOKEN = "test-csrf-token-for-employee-controller";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // =========================================================
    // POST /employee/save（T67、T68）
    // =========================================================

    @Test
    void T67_save_管理者のときsaveEmployeeが呼ばれて結果が返る() throws Exception {
        when(employeeService.saveEmployee(any(EmployeeRequest.class), eq("user001")))
                .thenReturn(new EmployeeResponse(true, "保存しました。"));

        mockMvc.perform(post("/employee/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"new\",\"email\":\"test@example.com\","
                                + "\"employeeName\":\"田中 太郎\",\"authorityCode\":\"01\","
                                + "\"password\":\"Pass@1234\"}")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01")
                        .sessionAttr("csrfToken", CSRF_TOKEN)
                        .header("X-CSRF-TOKEN", CSRF_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(employeeService).saveEmployee(any(EmployeeRequest.class), eq("user001"));
    }

    @Test
    void T68_save_管理者以外のとき権限がありませんが返る() throws Exception {
        mockMvc.perform(post("/employee/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"new\",\"email\":\"test@example.com\","
                                + "\"employeeName\":\"田中 太郎\",\"authorityCode\":\"01\","
                                + "\"password\":\"Pass@1234\"}")
                        .sessionAttr("loginUser", "user002")
                        .sessionAttr("authorityCode", "02")
                        .sessionAttr("csrfToken", CSRF_TOKEN)
                        .header("X-CSRF-TOKEN", CSRF_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("権限がありません。"));
    }

    // =========================================================
    // POST /employee/delete（T69、T70）
    // =========================================================

    @Test
    void T69_delete_管理者のときdeleteEmployeeが呼ばれて結果が返る() throws Exception {
        when(employeeService.deleteEmployee("user002"))
                .thenReturn(new EmployeeResponse(true, "削除しました。"));

        mockMvc.perform(post("/employee/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeNumber\":\"user002\"}")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01")
                        .sessionAttr("csrfToken", CSRF_TOKEN)
                        .header("X-CSRF-TOKEN", CSRF_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(employeeService).deleteEmployee("user002");
    }

    @Test
    void T70_delete_管理者以外のとき権限がありませんが返る() throws Exception {
        mockMvc.perform(post("/employee/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeNumber\":\"user002\"}")
                        .sessionAttr("loginUser", "user002")
                        .sessionAttr("authorityCode", "02")
                        .sessionAttr("csrfToken", CSRF_TOKEN)
                        .header("X-CSRF-TOKEN", CSRF_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("権限がありません。"));
    }
}
