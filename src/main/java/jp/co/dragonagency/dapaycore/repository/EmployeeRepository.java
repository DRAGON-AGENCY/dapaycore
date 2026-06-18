package jp.co.dragonagency.dapaycore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.dragonagency.dapaycore.model.Employee;

/**
 * 社員マスタ (m_employee) の永続化を担うリポジトリ。
 * 主キーは社員番号 (employee_number) を表す String 型とする。
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * メールアドレスを指定して社員を 1 件取得する。
     *
     * @param email メールアドレス
     * @return 該当する社員。存在しない場合は空
     */
    Optional<Employee> findByEmail(String email);

    /**
     * 論理削除されていない全社員を社員番号の昇順で取得する。
     *
     * @return 社員の一覧
     */
    List<Employee> findAllByDeleteFlagFalseOrderByEmployeeNumberAsc();

    /**
     * 論理削除されていない社員の中で、指定したメールアドレスの社員が存在するか返す。
     *
     * @param email メールアドレス
     * @return 存在する場合は true
     */
    boolean existsByEmailAndDeleteFlagFalse(String email);

    /**
     * 論理削除されていない社員の中で、指定した社員番号以外に
     * 指定したメールアドレスの社員が存在するか返す。
     * 編集時に自分自身を除外してメールアドレスの重複を判定するために使用する。
     *
     * @param email メールアドレス
     * @param employeeNumber 除外する社員番号
     * @return 存在する場合は true
     */
    boolean existsByEmailAndEmployeeNumberNotAndDeleteFlagFalse(
            String email, String employeeNumber);
}
