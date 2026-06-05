package jp.co.dragonagency.dapaycore.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.dragonagency.dapaycore.dto.EmployeeRequest;
import jp.co.dragonagency.dapaycore.dto.EmployeeResponse;
import jp.co.dragonagency.dapaycore.dto.LoginResult;
import jp.co.dragonagency.dapaycore.model.Employee;
import jp.co.dragonagency.dapaycore.repository.EmployeeRepository;

/**
 * 社員情報の取得・登録・更新・削除を担うサービス。
 * 社員マスタ (m_employee) に対する一覧取得とメンテナンスを提供する。
 */
@Service
public class EmployeeService {

    private static final String MODE_NEW = "new";
    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String AUTHORITY_OPERATOR = "02";
    private static final String AUTHORITY_VIEWER = "03";
    private static final String EMPLOYEE_NUMBER_PREFIX = "user";
    private static final int EMPLOYEE_NUMBER_DIGITS = 3;
    private static final int FIRST_EMPLOYEE_NUMBER = 1;

    // パスワードを連続して間違えられる上限。これに達するとロック扱いとする
    private static final int MAX_PASSWORD_ERROR_COUNT = 5;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    private static final int PASSWORD_MIN_LENGTH = 8;
    // BCrypt は 72 バイトを超える分を切り捨てるため上限を 72 文字とする
    private static final int PASSWORD_MAX_LENGTH = 72;
    // 使用を許可する文字 (空白を含まない半角の英数字・記号)
    private static final Pattern PASSWORD_ALLOWED_PATTERN =
            Pattern.compile("^[\\x21-\\x7E]+$");
    private static final Pattern PASSWORD_UPPERCASE_PATTERN =
            Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE_PATTERN =
            Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT_PATTERN =
            Pattern.compile("[0-9]");
    private static final Pattern PASSWORD_SYMBOL_PATTERN =
            Pattern.compile("[!-/:-@\\[-`{-~]");

    // 電話番号・FAX は半角数字とハイフンのみを許可する
    private static final Pattern PHONE_NUMBER_PATTERN =
            Pattern.compile("^[0-9-]+$");

    private static final String MESSAGE_INVALID_INPUT =
            "入力内容が正しくありません。";
    private static final String MESSAGE_EMAIL_REQUIRED =
            "メールアドレスを入力してください。";
    private static final String MESSAGE_EMAIL_FORMAT =
            "メールアドレスの形式が正しくありません。";
    private static final String MESSAGE_EMAIL_DUPLICATED =
            "入力されたメールアドレスは既に登録されています。";
    private static final String MESSAGE_NAME_REQUIRED =
            "社員名を入力してください。";
    private static final String MESSAGE_AUTHORITY_INVALID =
            "権限コードが正しくありません。";
    private static final String MESSAGE_PHONE_FORMAT =
            "電話番号は半角数字とハイフンで入力してください。";
    private static final String MESSAGE_FAX_FORMAT =
            "FAX は半角数字とハイフンで入力してください。";
    private static final String MESSAGE_PASSWORD_REQUIRED =
            "パスワードを入力してください。";
    private static final String MESSAGE_PASSWORD_LENGTH =
            "パスワードは 8 文字以上 72 文字以内で入力してください。";
    private static final String MESSAGE_PASSWORD_CHARACTER =
            "パスワードは半角の英数字と記号で入力してください。";
    private static final String MESSAGE_PASSWORD_POLICY =
            "パスワードは英大文字・英小文字・数字・記号を"
            + "それぞれ 1 文字以上含めてください。";
    private static final String MESSAGE_ERROR_COUNT_NEGATIVE =
            "パスワード入力間違い回数は 0 以上で入力してください。";
    private static final String MESSAGE_EMPLOYEE_NOT_FOUND =
            "対象の社員が見つかりません。";
    private static final String MESSAGE_LOGIN_FAILED =
            "社員番号またはパスワードが正しくありません";
    private static final String MESSAGE_ACCOUNT_LOCKED =
            "アカウントがロックされています。管理者にお問い合わせください";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 全社員を社員番号の昇順で取得する。
     *
     * @return 社員の一覧
     */
    public List<Employee> findAllEmployees() {
        return employeeRepository.findAllByOrderByEmployeeNumberAsc();
    }

    /**
     * メールアドレスを指定して社員を 1 件取得する。
     *
     * @param email メールアドレス
     * @return 該当する社員。存在しない場合は null
     */
    public Employee findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return employeeRepository.findByEmail(email).orElse(null);
    }

    /**
     * 社員番号を指定して社員を 1 件取得する。
     *
     * @param employeeNumber 社員番号
     * @return 該当する社員。存在しない場合は null
     */
    public Employee findByEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.isBlank()) {
            return null;
        }
        return employeeRepository.findById(employeeNumber).orElse(null);
    }

    /**
     * 社員番号とパスワードでログイン認証を行う。
     * パスワードは BCrypt のハッシュと照合する。連続して
     * 一定回数間違えた場合はアカウントをロック扱いとし、
     * 認証に成功した場合は間違い回数を 0 に戻す。
     *
     * @param employeeNumber 社員番号
     * @param rawPassword 入力されたパスワード (平文)
     * @return 認証結果
     */
    @Transactional
    public LoginResult login(String employeeNumber, String rawPassword) {
        String number = trimToEmpty(employeeNumber);
        if (number.isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            return new LoginResult(false, MESSAGE_LOGIN_FAILED, null, null);
        }
        Employee employee = employeeRepository.findById(number).orElse(null);
        if (employee == null) {
            return new LoginResult(false, MESSAGE_LOGIN_FAILED, null, null);
        }
        // 既にロック上限に達している場合は照合せずロックを通知する
        if (employee.getPasswordErrorCount() >= MAX_PASSWORD_ERROR_COUNT) {
            return new LoginResult(false, MESSAGE_ACCOUNT_LOCKED, null, null);
        }
        if (!passwordEncoder.matches(rawPassword, employee.getPassword())) {
            int nextErrorCount = employee.getPasswordErrorCount() + 1;
            employee.setPasswordErrorCount(nextErrorCount);
            employeeRepository.save(employee);
            if (nextErrorCount >= MAX_PASSWORD_ERROR_COUNT) {
                return new LoginResult(false, MESSAGE_ACCOUNT_LOCKED, null, null);
            }
            return new LoginResult(false, MESSAGE_LOGIN_FAILED, null, null);
        }
        // 認証成功。間違い回数が残っていれば 0 に戻す (変化時のみ更新する)
        if (employee.getPasswordErrorCount() != 0) {
            employee.setPasswordErrorCount(0);
            employeeRepository.save(employee);
        }
        return new LoginResult(
                true, null, employee.getEmployeeNumber(),
                employee.getAuthorityCode());
    }

    /**
     * 社員情報を登録または更新する。
     * 入力値を検査し、問題があれば失敗結果とメッセージを返す。
     *
     * @param request 画面から送信された社員情報
     * @param loginUserId 操作中のログインユーザの社員番号 (更新者として記録する)
     * @return 処理結果
     */
    @Transactional
    public EmployeeResponse saveEmployee(
            EmployeeRequest request, String loginUserId) {
        if (request == null) {
            return new EmployeeResponse(false, MESSAGE_INVALID_INPUT);
        }

        String email = trimToEmpty(request.getEmail());
        String employeeName = trimToEmpty(request.getEmployeeName());
        String employeeNameKana = trimToEmpty(request.getEmployeeNameKana());
        String department = trimToEmpty(request.getDepartment());
        String authorityCode = trimToEmpty(request.getAuthorityCode());
        String phoneNumber = trimToEmpty(request.getPhoneNumber());
        String faxNumber = trimToEmpty(request.getFaxNumber());
        String password = request.getPassword();

        String validationMessage = validateInput(
                email, employeeName, authorityCode, phoneNumber, faxNumber,
                request.getPasswordErrorCount());
        if (validationMessage != null) {
            return new EmployeeResponse(false, validationMessage);
        }

        boolean isNewMode = MODE_NEW.equals(request.getMode());
        if (isNewMode) {
            return createEmployee(
                    email, employeeName, employeeNameKana, department,
                    authorityCode, phoneNumber, faxNumber, password,
                    request.getPasswordErrorCount(), loginUserId);
        }
        return updateEmployee(
                trimToEmpty(request.getEmployeeNumber()), email, employeeName,
                employeeNameKana, department, authorityCode, phoneNumber,
                faxNumber, password, request.getPasswordErrorCount(),
                loginUserId);
    }

    /**
     * 社員番号を指定して社員を削除する。
     *
     * @param employeeNumber 社員番号
     * @return 処理結果
     */
    @Transactional
    public EmployeeResponse deleteEmployee(String employeeNumber) {
        String targetEmployeeNumber = trimToEmpty(employeeNumber);
        if (targetEmployeeNumber.isEmpty()
                || !employeeRepository.existsById(targetEmployeeNumber)) {
            return new EmployeeResponse(false, MESSAGE_EMPLOYEE_NOT_FOUND);
        }
        employeeRepository.deleteById(targetEmployeeNumber);
        return new EmployeeResponse(true, null);
    }

    /**
     * 新規社員を登録する。メールアドレスの重複とパスワード必須を検査する。
     */
    private EmployeeResponse createEmployee(
            String email, String employeeName, String employeeNameKana,
            String department, String authorityCode, String phoneNumber,
            String faxNumber, String password, int passwordErrorCount,
            String loginUserId) {
        if (employeeRepository.existsByEmail(email)) {
            return new EmployeeResponse(false, MESSAGE_EMAIL_DUPLICATED);
        }
        String passwordMessage = validatePassword(password);
        if (passwordMessage != null) {
            return new EmployeeResponse(false, passwordMessage);
        }

        Employee employee = new Employee();
        employee.setEmployeeNumber(generateNextEmployeeNumber());
        employee.setEmail(email);
        employee.setEmployeeName(employeeName);
        employee.setEmployeeNameKana(employeeNameKana);
        employee.setDepartment(department);
        employee.setAuthorityCode(authorityCode);
        employee.setPhoneNumber(phoneNumber);
        employee.setFaxNumber(faxNumber);
        employee.setPassword(passwordEncoder.encode(password));
        employee.setPasswordErrorCount(passwordErrorCount);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        // 更新者として操作中のログインユーザの社員番号を記録する
        employee.setUpdateUserId(loginUserId);

        employeeRepository.save(employee);
        return new EmployeeResponse(true, null);
    }

    /**
     * 既存社員を更新する。パスワードは空欄の場合に現状を維持する。
     */
    private EmployeeResponse updateEmployee(
            String employeeNumber, String email, String employeeName,
            String employeeNameKana, String department, String authorityCode,
            String phoneNumber, String faxNumber, String password,
            int passwordErrorCount, String loginUserId) {
        if (employeeNumber.isEmpty()) {
            return new EmployeeResponse(false, MESSAGE_EMPLOYEE_NOT_FOUND);
        }
        Employee employee =
                employeeRepository.findById(employeeNumber).orElse(null);
        if (employee == null) {
            return new EmployeeResponse(false, MESSAGE_EMPLOYEE_NOT_FOUND);
        }
        if (employeeRepository.existsByEmailAndEmployeeNumberNot(
                email, employeeNumber)) {
            return new EmployeeResponse(false, MESSAGE_EMAIL_DUPLICATED);
        }

        // パスワードは入力された場合のみ検査する (空欄は現状維持)
        boolean passwordProvided = password != null && !password.isBlank();
        if (passwordProvided) {
            String passwordMessage = validatePassword(password);
            if (passwordMessage != null) {
                return new EmployeeResponse(false, passwordMessage);
            }
        }

        employee.setEmail(email);
        employee.setEmployeeName(employeeName);
        employee.setEmployeeNameKana(employeeNameKana);
        employee.setDepartment(department);
        employee.setAuthorityCode(authorityCode);
        employee.setPhoneNumber(phoneNumber);
        employee.setFaxNumber(faxNumber);
        employee.setPasswordErrorCount(passwordErrorCount);

        // パスワードは入力された場合のみハッシュ化して更新する (空欄は現状維持)
        if (passwordProvided) {
            employee.setPassword(passwordEncoder.encode(password));
        }

        // 更新日時にシステム日時を、更新者に操作中のログインユーザの
        // 社員番号を設定する
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setUpdateUserId(loginUserId);

        employeeRepository.save(employee);
        return new EmployeeResponse(true, null);
    }

    /**
     * 入力値の必須・形式・範囲を検査する。
     *
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateInput(
            String email, String employeeName, String authorityCode,
            String phoneNumber, String faxNumber, int passwordErrorCount) {
        if (email.isEmpty()) {
            return MESSAGE_EMAIL_REQUIRED;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return MESSAGE_EMAIL_FORMAT;
        }
        if (employeeName.isEmpty()) {
            return MESSAGE_NAME_REQUIRED;
        }
        if (!isValidAuthorityCode(authorityCode)) {
            return MESSAGE_AUTHORITY_INVALID;
        }
        // 社員名 (カナ)・部署・電話番号は任意項目。
        // 電話番号は入力された場合のみ形式を検査する
        if (!phoneNumber.isEmpty()
                && !PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
            return MESSAGE_PHONE_FORMAT;
        }
        // FAX は任意項目のため、入力された場合のみ形式を検査する
        if (!faxNumber.isEmpty()
                && !PHONE_NUMBER_PATTERN.matcher(faxNumber).matches()) {
            return MESSAGE_FAX_FORMAT;
        }
        if (passwordErrorCount < 0) {
            return MESSAGE_ERROR_COUNT_NEGATIVE;
        }
        return null;
    }

    /**
     * パスワードがポリシーを満たすかどうかを検査する。
     * 8〜72 文字の半角英数字・記号で構成され、英大文字・英小文字・数字・
     * 記号をそれぞれ 1 文字以上含むことを必須とする。
     *
     * @param password 検査対象のパスワード
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return MESSAGE_PASSWORD_REQUIRED;
        }
        if (password.length() < PASSWORD_MIN_LENGTH
                || password.length() > PASSWORD_MAX_LENGTH) {
            return MESSAGE_PASSWORD_LENGTH;
        }
        if (!PASSWORD_ALLOWED_PATTERN.matcher(password).matches()) {
            return MESSAGE_PASSWORD_CHARACTER;
        }
        if (!PASSWORD_UPPERCASE_PATTERN.matcher(password).find()
                || !PASSWORD_LOWERCASE_PATTERN.matcher(password).find()
                || !PASSWORD_DIGIT_PATTERN.matcher(password).find()
                || !PASSWORD_SYMBOL_PATTERN.matcher(password).find()) {
            return MESSAGE_PASSWORD_POLICY;
        }
        return null;
    }

    /**
     * 権限コードが許可された値 (01/02/03) かどうかを判定する。
     *
     * @param authorityCode 権限コード
     * @return 許可された値の場合は true
     */
    private boolean isValidAuthorityCode(String authorityCode) {
        return AUTHORITY_ADMINISTRATOR.equals(authorityCode)
                || AUTHORITY_OPERATOR.equals(authorityCode)
                || AUTHORITY_VIEWER.equals(authorityCode);
    }

    /**
     * 既存の最大社員番号を基に、次の社員番号を採番する。
     * 形式は「user」+ 0 埋め 3 桁の連番 (例: user011)。
     *
     * @return 新しい社員番号
     */
    private String generateNextEmployeeNumber() {
        Optional<Employee> latest =
                employeeRepository.findFirstByOrderByEmployeeNumberDesc();
        int nextNumber = FIRST_EMPLOYEE_NUMBER;
        if (latest.isPresent()) {
            nextNumber =
                    extractEmployeeNumber(latest.get().getEmployeeNumber()) + 1;
        }
        return String.format(
                "%s%0" + EMPLOYEE_NUMBER_DIGITS + "d",
                EMPLOYEE_NUMBER_PREFIX, nextNumber);
    }

    /**
     * 社員番号から末尾の数値部分を取り出す。
     * 数値として解釈できない場合は 0 を返す。
     *
     * @param employeeNumber 社員番号
     * @return 数値部分
     */
    private int extractEmployeeNumber(String employeeNumber) {
        String digits = employeeNumber.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }

    /**
     * 文字列の前後の空白を除去する。null の場合は空文字を返す。
     *
     * @param value 対象の文字列
     * @return 前後の空白を除去した文字列
     */
    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
