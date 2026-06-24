package jp.co.dragonagency.dapaycore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import jp.co.dragonagency.dapaycore.dto.EmployeeRequest;
import jp.co.dragonagency.dapaycore.dto.EmployeeResponse;
import jp.co.dragonagency.dapaycore.dto.LoginResult;
import jp.co.dragonagency.dapaycore.model.Employee;
import jp.co.dragonagency.dapaycore.repository.EmployeeRepository;

/**
 * EmployeeService の単体テスト（項番 T7〜T64）。
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EmployeeService service;

    // =========================================================
    // findAllEmployees（T7）
    // =========================================================

    @Test
    void T7_findAllEmployees_deleteFlag_falseの社員のみを社員番号昇順で全件取得しtrueは除外する() {
        Employee active = buildEmployee("user001", "a@example.com", false);
        when(employeeRepository.findAllByDeleteFlagFalseOrderByEmployeeNumberAsc())
                .thenReturn(List.of(active));

        List<Employee> result = service.findAllEmployees();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isDeleteFlag());
        verify(employeeRepository).findAllByDeleteFlagFalseOrderByEmployeeNumberAsc();
    }

    // =========================================================
    // findByEmail（T9、T11、T12）
    // =========================================================

    @Test
    void T9_findByEmail_nullまたは空文字のときnullを返す() {
        assertNull(service.findByEmail(null));
        assertNull(service.findByEmail(""));
        assertNull(service.findByEmail("  "));
    }

    @Test
    void T11_findByEmail_存在するメールアドレスdelete_flag_falseのときEmployeeを返す() {
        Employee employee = buildEmployee("user001", "test@example.com", false);
        when(employeeRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(employee));

        Employee result = service.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void T12_findByEmail_delete_flag_trueまたは存在しないメールアドレスのときnullを返す() {
        Employee deleted = buildEmployee("user001", "del@example.com", true);
        when(employeeRepository.findByEmail("del@example.com"))
                .thenReturn(Optional.of(deleted));
        when(employeeRepository.findByEmail("none@example.com"))
                .thenReturn(Optional.empty());

        assertNull(service.findByEmail("del@example.com"));
        assertNull(service.findByEmail("none@example.com"));
    }

    // =========================================================
    // findByEmployeeNumber（T14、T16、T17）
    // =========================================================

    @Test
    void T14_findByEmployeeNumber_nullまたは空文字のときnullを返す() {
        assertNull(service.findByEmployeeNumber(null));
        assertNull(service.findByEmployeeNumber(""));
        assertNull(service.findByEmployeeNumber("  "));
    }

    @Test
    void T16_findByEmployeeNumber_存在するdelete_flag_falseのときEmployeeを返す() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));

        Employee result = service.findByEmployeeNumber("user001");

        assertNotNull(result);
        assertEquals("user001", result.getEmployeeNumber());
    }

    @Test
    void T17_findByEmployeeNumber_delete_flag_trueまたは存在しない社員番号のときnullを返す() {
        Employee deleted = buildEmployee("user001", "a@example.com", true);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(deleted));
        when(employeeRepository.findById("user999")).thenReturn(Optional.empty());

        assertNull(service.findByEmployeeNumber("user001"));
        assertNull(service.findByEmployeeNumber("user999"));
    }

    // =========================================================
    // login（T19、T21、T23〜T28）
    // =========================================================

    @Test
    void T19_login_employeeNumberが空またはrawPasswordがnullのとき失敗メッセージを返す() {
        LoginResult r1 = service.login("", "pass");
        LoginResult r2 = service.login("user001", null);

        assertFalse(r1.isSuccess());
        assertEquals("社員番号またはパスワードが正しくありません", r1.getMessage());
        assertFalse(r2.isSuccess());
        assertEquals("社員番号またはパスワードが正しくありません", r2.getMessage());
    }

    @Test
    void T21_login_社員が存在しないまたはdelete_flag_trueのとき失敗メッセージを返す() {
        Employee deleted = buildEmployee("user001", "a@example.com", true);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(deleted));
        when(employeeRepository.findById("user999")).thenReturn(Optional.empty());

        LoginResult r1 = service.login("user001", "pass");
        LoginResult r2 = service.login("user999", "pass");

        assertFalse(r1.isSuccess());
        assertEquals("社員番号またはパスワードが正しくありません", r1.getMessage());
        assertFalse(r2.isSuccess());
        assertEquals("社員番号またはパスワードが正しくありません", r2.getMessage());
    }

    @Test
    void T23_login_passwordErrorCountが5以上のときアカウントロックメッセージを返す() {
        Employee locked = buildEmployee("user001", "a@example.com", false);
        locked.setPasswordErrorCount(5);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(locked));

        LoginResult result = service.login("user001", "pass");

        assertFalse(result.isSuccess());
        assertEquals("アカウントがロックされています。管理者にお問い合わせください",
                result.getMessage());
    }

    @Test
    void T24_login_パスワード不一致のときerrorCountを1増やす() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        employee.setPasswordErrorCount(1);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("wrong", employee.getPassword())).thenReturn(false);

        LoginResult result = service.login("user001", "wrong");

        assertFalse(result.isSuccess());
        assertEquals(2, employee.getPasswordErrorCount());
        verify(employeeRepository).save(employee);
    }

    @Test
    void T25_login_errorCountが5になったときアカウントロックメッセージを返す() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        employee.setPasswordErrorCount(4);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("wrong", employee.getPassword())).thenReturn(false);

        LoginResult result = service.login("user001", "wrong");

        assertFalse(result.isSuccess());
        assertEquals("アカウントがロックされています。管理者にお問い合わせください",
                result.getMessage());
        assertEquals(5, employee.getPasswordErrorCount());
    }

    @Test
    void T26_login_認証成功のときemployeeNumberとauthorityCodeを返す() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        employee.setAuthorityCode("01");
        employee.setPasswordErrorCount(0);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("Correct1!", employee.getPassword())).thenReturn(true);

        LoginResult result = service.login("user001", "Correct1!");

        assertTrue(result.isSuccess());
        assertEquals("user001", result.getEmployeeNumber());
        assertEquals("01", result.getAuthorityCode());
    }

    @Test
    void T27_login_認証成功でerrorCountが0以外のとき0にリセットしsaveを呼ぶ() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        employee.setPasswordErrorCount(3);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("Correct1!", employee.getPassword())).thenReturn(true);

        service.login("user001", "Correct1!");

        assertEquals(0, employee.getPasswordErrorCount());
        verify(employeeRepository).save(employee);
    }

    @Test
    void T28_login_認証成功でerrorCountが既に0のときsaveを呼ばない() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        employee.setPasswordErrorCount(0);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("Correct1!", employee.getPassword())).thenReturn(true);

        service.login("user001", "Correct1!");

        verify(employeeRepository, never()).save(any());
    }

    // =========================================================
    // saveEmployee 入力検証（T29〜T38）
    // =========================================================

    @Test
    void T29_saveEmployee_requestがnullのとき入力エラーを返す() {
        EmployeeResponse result = service.saveEmployee(null, "user001");

        assertFalse(result.isSuccess());
        assertEquals("入力内容が正しくありません。", result.getMessage());
    }

    @Test
    void T30_saveEmployee_emailが空のときメールアドレス必須エラーを返す() {
        EmployeeRequest req = buildNewRequest("", "田中 太郎", "01", "Passw0rd!");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("メールアドレスを入力してください。", result.getMessage());
    }

    @Test
    void T31_saveEmployee_emailが無効形式のときフォーマットエラーを返す() {
        EmployeeRequest req = buildNewRequest("invalid-email", "田中 太郎", "01", "Passw0rd!");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("メールアドレスの形式が正しくありません。", result.getMessage());
    }

    @Test
    void T32_saveEmployee_employeeNameが空のとき社員名必須エラーを返す() {
        EmployeeRequest req = buildNewRequest("a@example.com", "", "01", "Passw0rd!");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("社員名を入力してください。", result.getMessage());
    }

    @Test
    void T33_saveEmployee_authorityCodeが不正のとき権限コードエラーを返す() {
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "99", "Passw0rd!");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("権限コードが正しくありません。", result.getMessage());
    }

    @Test
    void T34_saveEmployee_authorityCode_01_02_03のとき検証を通過する() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString())).thenReturn(true);
        for (String code : new String[]{"01", "02", "03"}) {
            EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", code, "Passw0rd!");
            EmployeeResponse result = service.saveEmployee(req, "user001");
            assertNotNull(result);
            assertFalse("権限コードが正しくありません。".equals(result.getMessage()),
                    "authorityCode=" + code + " で権限エラーが出てはいけない");
        }
    }

    @Test
    void T35_saveEmployee_phoneNumberが不正形式のとき電話番号エラーを返す() {
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Passw0rd!");
        req.setPhoneNumber("abc");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("電話番号は半角数字とハイフンで入力してください。", result.getMessage());
    }

    @Test
    void T36_saveEmployee_phoneNumberが空のときエラーなし() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString())).thenReturn(true);
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Passw0rd!");
        req.setPhoneNumber("");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse("電話番号は半角数字とハイフンで入力してください。"
                .equals(result.getMessage()));
    }

    @Test
    void T37_saveEmployee_faxNumberが不正形式のときFAXエラーを返す() {
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Passw0rd!");
        req.setFaxNumber("fax");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("FAX は半角数字とハイフンで入力してください。", result.getMessage());
    }

    @Test
    void T38_saveEmployee_passwordErrorCountが負のときエラーを返す() {
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Passw0rd!");
        req.setPasswordErrorCount(-1);
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("パスワード入力間違い回数は 0 以上で入力してください。",
                result.getMessage());
    }

    // =========================================================
    // saveEmployee 登録（T39〜T51）
    // =========================================================

    @Test
    void T39_saveEmployee_メールアドレスが重複するとき重複エラーを返す() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse("dup@example.com"))
                .thenReturn(true);
        EmployeeRequest req = buildNewRequest("dup@example.com", "田中 太郎", "01", "Passw0rd!");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("入力されたメールアドレスは既に登録されています。", result.getMessage());
    }

    @Test
    void T40_saveEmployee_passwordが空のときパスワード必須エラーを返す() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("パスワードを入力してください。", result.getMessage());
    }

    @Test
    void T41_saveEmployee_passwordが7文字以下または73文字以上のとき長さエラーを返す() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);

        EmployeeRequest shortReq = buildNewRequest("a@example.com", "田中 太郎", "01", "Pas1!");
        assertFalse(service.saveEmployee(shortReq, "u").isSuccess());
        assertEquals("パスワードは 8 文字以上 72 文字以内で入力してください。",
                service.saveEmployee(shortReq, "u").getMessage());

        String longPass = "A1!".repeat(25);
        EmployeeRequest longReq = buildNewRequest("a@example.com", "田中 太郎", "01", longPass);
        assertFalse(service.saveEmployee(longReq, "u").isSuccess());
        assertEquals("パスワードは 8 文字以上 72 文字以内で入力してください。",
                service.saveEmployee(longReq, "u").getMessage());
    }

    @Test
    void T43_saveEmployee_passwordに全角文字が含まれるとき文字種エラーを返す() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Passw0rd！");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("パスワードは半角の英数字と記号で入力してください。", result.getMessage());
    }

    @Test
    void T44_saveEmployee_passwordに各文字種が1種類でも欠けるときポリシーエラーを返す() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);

        String[] invalid = {
            "passw0rd!",   // 英大文字なし
            "PASSW0RD!",   // 英小文字なし
            "Passwddd!",   // 数字なし
            "Passw0rdA"    // 記号なし
        };
        for (String pass : invalid) {
            EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", pass);
            EmployeeResponse result = service.saveEmployee(req, "user001");
            assertFalse(result.isSuccess(), "'" + pass + "' でポリシーエラーが出るべき");
            assertEquals("パスワードは英大文字・英小文字・数字・記号をそれぞれ 1 文字以上含めてください。",
                    result.getMessage());
        }
    }

    @Test
    void T48_saveEmployee_有効なパスワードのとき検証を通過する() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");

        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Correct1!");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertTrue(result.isSuccess());
    }

    @Test
    void T49_saveEmployee_新規登録でシーケンスから社員番号が採番される() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(5);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");

        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Correct1!");
        service.saveEmployee(req, "user001");

        verify(jdbcTemplate).queryForObject(
                "SELECT nextval('seq_employee_number')", Integer.class);
    }

    @Test
    void T50_saveEmployee_パスワードがBCryptでハッシュ化される() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        when(passwordEncoder.encode("Correct1!")).thenReturn("$2a$hashed");

        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Correct1!");
        service.saveEmployee(req, "user001");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("$2a$hashed", captor.getValue().getPassword());
    }

    @Test
    void T51_saveEmployee_updateUserIdにloginUserIdが設定される() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(1);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");

        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Correct1!");
        service.saveEmployee(req, "user999");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("user999", captor.getValue().getUpdateUserId());
    }

    // =========================================================
    // saveEmployee 更新（T52、T54〜T59）
    // =========================================================

    @Test
    void T52_saveEmployee_更新時employeeNumberが空または存在しないとき社員が見つからないを返す() {
        when(employeeRepository.findById("user999")).thenReturn(Optional.empty());

        EmployeeRequest emptyNum = buildUpdateRequest("", "a@example.com", "田中 太郎", "01");
        EmployeeResponse r1 = service.saveEmployee(emptyNum, "user001");

        EmployeeRequest notFound = buildUpdateRequest("user999", "a@example.com", "田中 太郎", "01");
        EmployeeResponse r2 = service.saveEmployee(notFound, "user001");

        assertFalse(r1.isSuccess());
        assertEquals("対象の社員が見つかりません。", r1.getMessage());
        assertFalse(r2.isSuccess());
        assertEquals("対象の社員が見つかりません。", r2.getMessage());
    }

    @Test
    void T54_saveEmployee_更新時他の社員とメールが重複するとき重複エラーを返す() {
        Employee target = buildEmployee("user001", "old@example.com", false);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(target));
        when(employeeRepository.existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
                "dup@example.com", "user001")).thenReturn(true);

        EmployeeRequest req = buildUpdateRequest("user001", "dup@example.com", "田中 太郎", "01");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertEquals("入力されたメールアドレスは既に登録されています。", result.getMessage());
    }

    @Test
    void T55_saveEmployee_更新時自分と同じメールアドレスのときエラーなし() {
        Employee target = buildEmployee("user001", "same@example.com", false);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(target));
        when(employeeRepository.existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
                "same@example.com", "user001")).thenReturn(false);

        EmployeeRequest req = buildUpdateRequest("user001", "same@example.com", "田中 太郎", "01");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertTrue(result.isSuccess());
    }

    @Test
    void T56_saveEmployee_更新時passwordが空のとき現在のパスワードを維持する() {
        Employee target = buildEmployee("user001", "a@example.com", false);
        target.setPassword("$2a$existingHash");
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(target));
        when(employeeRepository.existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
                anyString(), anyString())).thenReturn(false);

        EmployeeRequest req = buildUpdateRequest("user001", "a@example.com", "田中 太郎", "01");
        req.setPassword("");
        service.saveEmployee(req, "user001");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("$2a$existingHash", captor.getValue().getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void T57_saveEmployee_更新時passwordが入力されたとき検証する() {
        Employee target = buildEmployee("user001", "a@example.com", false);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(target));
        when(employeeRepository.existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
                anyString(), anyString())).thenReturn(false);

        EmployeeRequest req = buildUpdateRequest("user001", "a@example.com", "田中 太郎", "01");
        req.setPassword("short");
        EmployeeResponse result = service.saveEmployee(req, "user001");

        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    void T58_saveEmployee_更新時passwordが有効のときBCryptでハッシュ化する() {
        Employee target = buildEmployee("user001", "a@example.com", false);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(target));
        when(employeeRepository.existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
                anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode("NewPass1!")).thenReturn("$2a$newHash");

        EmployeeRequest req = buildUpdateRequest("user001", "a@example.com", "田中 太郎", "01");
        req.setPassword("NewPass1!");
        service.saveEmployee(req, "user001");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("$2a$newHash", captor.getValue().getPassword());
    }

    @Test
    void T59_saveEmployee_更新成功のときupdatedAtとupdateUserIdが更新される() {
        Employee target = buildEmployee("user001", "a@example.com", false);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(target));
        when(employeeRepository.existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
                anyString(), anyString())).thenReturn(false);

        EmployeeRequest req = buildUpdateRequest("user001", "a@example.com", "田中 太郎", "01");
        service.saveEmployee(req, "operator001");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        Employee saved = captor.getValue();
        assertTrue(saved.getUpdatedAt().isAfter(before));
        assertEquals("operator001", saved.getUpdateUserId());
    }

    // =========================================================
    // deleteEmployee（T60、T63）
    // =========================================================

    @Test
    void T60_deleteEmployee_employeeNumberが空_存在しない_論理削除済みのいずれかのとき社員が見つからないを返す() {
        Employee deleted = buildEmployee("user001", "a@example.com", true);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(deleted));
        when(employeeRepository.findById("user999")).thenReturn(Optional.empty());

        EmployeeResponse r1 = service.deleteEmployee("");
        EmployeeResponse r2 = service.deleteEmployee("user999");
        EmployeeResponse r3 = service.deleteEmployee("user001");

        assertEquals("対象の社員が見つかりません。", r1.getMessage());
        assertEquals("対象の社員が見つかりません。", r2.getMessage());
        assertEquals("対象の社員が見つかりません。", r3.getMessage());
    }

    @Test
    void T63_deleteEmployee_成功のときdeleteFlagをtrueにしupdatedAtを更新する() {
        Employee employee = buildEmployee("user001", "a@example.com", false);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        when(employeeRepository.findById("user001")).thenReturn(Optional.of(employee));

        EmployeeResponse result = service.deleteEmployee("user001");

        assertTrue(result.isSuccess());
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleteFlag());
        assertTrue(captor.getValue().getUpdatedAt().isAfter(before));
    }

    // =========================================================
    // 社員番号採番（T64）
    // =========================================================

    @Test
    void T64_saveEmployee_新規登録でシーケンスからuserプレフィックスと0埋め3桁の社員番号を採番する_11ならuser011() {
        when(employeeRepository.existsByEmailAndDeleteFlagFalse(anyString()))
                .thenReturn(false);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(11);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");

        EmployeeRequest req = buildNewRequest("a@example.com", "田中 太郎", "01", "Correct1!");
        service.saveEmployee(req, "user001");

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("user011", captor.getValue().getEmployeeNumber());
    }

    // =========================================================
    // ヘルパーメソッド
    // =========================================================

    private Employee buildEmployee(String number, String email, boolean deleteFlag) {
        Employee e = new Employee();
        e.setEmployeeNumber(number);
        e.setEmail(email);
        e.setPassword("$2a$hashed");
        e.setAuthorityCode("01");
        e.setDeleteFlag(deleteFlag);
        return e;
    }

    private EmployeeRequest buildNewRequest(
            String email, String name, String authorityCode, String password) {
        EmployeeRequest req = new EmployeeRequest();
        req.setMode("new");
        req.setEmail(email);
        req.setEmployeeName(name);
        req.setAuthorityCode(authorityCode);
        req.setPassword(password);
        req.setPasswordErrorCount(0);
        return req;
    }

    private EmployeeRequest buildUpdateRequest(
            String employeeNumber, String email, String name, String authorityCode) {
        EmployeeRequest req = new EmployeeRequest();
        req.setMode("edit");
        req.setEmployeeNumber(employeeNumber);
        req.setEmail(email);
        req.setEmployeeName(name);
        req.setAuthorityCode(authorityCode);
        req.setPasswordErrorCount(0);
        return req;
    }
}
