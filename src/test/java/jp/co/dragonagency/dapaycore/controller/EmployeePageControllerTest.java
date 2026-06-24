package jp.co.dragonagency.dapaycore.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jp.co.dragonagency.dapaycore.model.Employee;
import jp.co.dragonagency.dapaycore.service.EmployeeService;

/**
 * EmployeePageController の Web 層テスト（項番 T1〜T6、T71、T72、T75、T88、T89、T98）。
 */
@WebMvcTest(EmployeePageController.class)
class EmployeePageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    // =========================================================
    // employeeList（T1）
    // =========================================================

    @Test
    void T1_employeeList_employeesとauthorityCodeがモデルに設定される() throws Exception {
        Employee emp = buildEmployee("user001", "田中 太郎", "01");
        when(employeeService.findAllEmployees()).thenReturn(List.of(emp));

        mockMvc.perform(get("/employee_list.html")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_list"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("authorityCode", "01"));
    }

    // =========================================================
    // employeeEdit（T2〜T6）
    // =========================================================

    @Test
    void T2_employeeEdit_mode_new_authorityCode_01のとき_employee_editテンプレートを返す()
            throws Exception {
        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee_edit"));
    }

    @Test
    void T3_employeeEdit_authorityCodeが01以外のとき社員一覧へリダイレクト() throws Exception {
        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "02"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employee_list.html"));
    }

    @Test
    void T4_employeeEdit_mode_newのときemployeeはモデルに追加されない() throws Exception {
        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("employee"));
    }

    @Test
    void T5_employeeEdit_editモードのときfindByEmployeeNumberが呼ばれemployeeがモデルに設定される()
            throws Exception {
        Employee emp = buildEmployee("user001", "田中 太郎", "01");
        when(employeeService.findByEmployeeNumber("user001")).thenReturn(emp);

        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "edit")
                        .param("employeeNumber", "user001")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("employee", emp));
    }

    @Test
    void T6_employeeEdit_modeとauthorityCodeがモデルに設定される() throws Exception {
        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mode", "new"))
                .andExpect(model().attribute("authorityCode", "01"));
    }

    // =========================================================
    // employee_list.html Thymeleaf レンダリング（T71、T72、T75）
    // =========================================================

    @Test
    void T71_employeeList_th_eachで社員一覧がレンダリングされる() throws Exception {
        Employee emp = buildEmployee("user001", "田中 太郎", "01");
        when(employeeService.findAllEmployees()).thenReturn(List.of(emp));

        mockMvc.perform(get("/employee_list.html")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("user001")))
                .andExpect(content().string(containsString("田中 太郎")));
    }

    @Test
    void T72_employeeList_権限コードに応じて管理者_担当者_閲覧のみが正しくレンダリングされる()
            throws Exception {
        when(employeeService.findAllEmployees()).thenReturn(List.of(
                buildEmployee("user001", "社員A", "01"),
                buildEmployee("user002", "社員B", "02"),
                buildEmployee("user003", "社員C", "03")));

        mockMvc.perform(get("/employee_list.html")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("管理者")))
                .andExpect(content().string(containsString("担当者")))
                .andExpect(content().string(containsString("閲覧のみ")));
    }

    @Test
    void T75_employeeList_authorityCode_01のときのみ新規登録ボタンがレンダリングされる()
            throws Exception {
        when(employeeService.findAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/employee_list.html")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"new-employee-btn\"")));

        mockMvc.perform(get("/employee_list.html")
                        .sessionAttr("loginUser", "user002")
                        .sessionAttr("authorityCode", "02"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("id=\"new-employee-btn\""))));
    }

    // =========================================================
    // employee_edit.html Thymeleaf レンダリング（T88、T89、T98）
    // =========================================================

    @Test
    void T88_employeeEdit_CSRFメタタグにth_contentでトークンが埋め込まれる() throws Exception {
        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<meta name=\"csrf-token\""
                        + " content=\"[A-Za-z0-9_-]{40,}\">.*")));
    }

    @Test
    void T89_employeeEdit_編集モードではth_valueにemployeeの値が設定され登録モードでは空欄になる()
            throws Exception {
        Employee emp = buildEmployee("user001", "田中 太郎", "01");
        emp.setEmail("tanaka@example.com");
        when(employeeService.findByEmployeeNumber("user001")).thenReturn(emp);

        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "edit")
                        .param("employeeNumber", "user001")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"user001\"")))
                .andExpect(content().string(containsString("tanaka@example.com")));

        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("value=\"user001\""))))
                .andExpect(content().string(not(containsString("tanaka@example.com"))));
    }

    @Test
    void T98_employeeEdit_権限セレクトに01_02_03のオプションがレンダリングされる() throws Exception {
        mockMvc.perform(get("/employee_edit.html")
                        .param("mode", "new")
                        .sessionAttr("loginUser", "user001")
                        .sessionAttr("authorityCode", "01"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"01\"")))
                .andExpect(content().string(containsString("value=\"02\"")))
                .andExpect(content().string(containsString("value=\"03\"")));
    }

    // =========================================================
    // ヘルパーメソッド
    // =========================================================

    private Employee buildEmployee(String number, String name, String authorityCode) {
        Employee e = new Employee();
        e.setEmployeeNumber(number);
        e.setEmployeeName(name);
        e.setAuthorityCode(authorityCode);
        e.setDeleteFlag(false);
        return e;
    }
}
