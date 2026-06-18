package jp.co.dragonagency.dapaycore.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jp.co.dragonagency.dapaycore.model.Employee;
import jp.co.dragonagency.dapaycore.security.SessionAttributeNames;
import jp.co.dragonagency.dapaycore.service.EmployeeService;

/**
 * 社員管理の一覧・編集画面を表示するコントローラ。
 * 一覧へ社員データを供給し、編集画面はメンテナンス権限 (管理者) を検査する。
 */
@Controller
public class EmployeePageController {

    private static final String MODE_NEW = "new";
    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String ATTRIBUTE_EMPLOYEES = "employees";
    private static final String ATTRIBUTE_EMPLOYEE = "employee";
    private static final String ATTRIBUTE_MODE = "mode";
    private static final String ATTRIBUTE_AUTHORITY_CODE = "authorityCode";
    private static final String VIEW_EMPLOYEE_LIST = "employee_list";
    private static final String VIEW_EMPLOYEE_EDIT = "employee_edit";
    private static final String REDIRECT_EMPLOYEE_LIST =
            "redirect:/employee_list.html";

    private final EmployeeService employeeService;

    public EmployeePageController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 社員一覧画面を表示する。社員一覧とログイン中ユーザの権限を渡す。
     *
     * @param session ログイン状態と権限を保持するセッション
     * @param model 画面へ渡すモデル
     * @return 社員一覧画面のビュー名
     */
    @GetMapping("/employee_list.html")
    public String employeeList(HttpSession session, Model model) {
        model.addAttribute(
                ATTRIBUTE_EMPLOYEES, employeeService.findAllEmployees());
        model.addAttribute(
                ATTRIBUTE_AUTHORITY_CODE, getAuthorityCode(session));
        return VIEW_EMPLOYEE_LIST;
    }

    /**
     * 社員編集 (登録・更新) 画面を表示する。
     * メンテナンスは管理者のみのため、管理者以外は一覧へ戻す。
     * 編集モードでは選択された社員の内容を社員番号で読み込む。
     *
     * @param mode モード (new=新規登録、それ以外=編集)
     * @param employeeNumber 編集対象の社員番号
     * @param session ログイン状態と権限を保持するセッション
     * @param model 画面へ渡すモデル
     * @return 社員編集画面のビュー名。権限不足時は一覧へのリダイレクト
     */
    @GetMapping("/employee_edit.html")
    public String employeeEdit(
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "employeeNumber", required = false) String employeeNumber,
            HttpSession session,
            Model model) {
        String authorityCode = getAuthorityCode(session);

        // メンテナンスは管理者 (01) のみ。それ以外は一覧へ戻す
        if (!AUTHORITY_ADMINISTRATOR.equals(authorityCode)) {
            return REDIRECT_EMPLOYEE_LIST;
        }

        // 編集モードでは選択された社員の内容を社員番号で読み込む
        if (!MODE_NEW.equals(mode)) {
            Employee employee = employeeService.findByEmployeeNumber(employeeNumber);
            model.addAttribute(ATTRIBUTE_EMPLOYEE, employee);
        }
        model.addAttribute(ATTRIBUTE_MODE, mode);
        model.addAttribute(ATTRIBUTE_AUTHORITY_CODE, authorityCode);
        return VIEW_EMPLOYEE_EDIT;
    }

    /**
     * セッションに保持された権限コードを取得する。
     *
     * @param session 対象のセッション
     * @return 権限コード。未設定の場合は null
     */
    private String getAuthorityCode(HttpSession session) {
        Object authorityCode =
                session.getAttribute(SessionAttributeNames.AUTHORITY_CODE);
        if (authorityCode == null) {
            return null;
        }
        return authorityCode.toString();
    }
}
