package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.MemberListItemDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会員一覧画面のデータ取得を担うサービス。
 * m_merchant_application と m_merchant_application_document を 1 クエリで取得し、
 * 必要な列のみを選択することで転送データ量を最小化する。
 */
@Service
public class MemberListService {

    private static final String SQL_FIND_ALL =
            "SELECT a.member_code, a.corporate_name_kana, a.corporate_name, "
            + "a.corporate_number, a.industry_category, "
            + "a.rep_last_name, a.rep_first_name, "
            + "a.application_status, a.submitted_at, "
            + "COUNT(d.id) AS document_count "
            + "FROM m_merchant_application a "
            + "LEFT JOIN m_merchant_application_document d "
            + "    ON d.member_code = a.member_code "
            + "WHERE a.delete_flag = false "
            + "GROUP BY a.member_code, a.corporate_name_kana, a.corporate_name, "
            + "    a.corporate_number, a.industry_category, "
            + "    a.rep_last_name, a.rep_first_name, "
            + "    a.application_status, a.submitted_at "
            + "ORDER BY a.submitted_at DESC NULLS LAST";

    private final JdbcTemplate jdbcTemplate;

    public MemberListService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MemberListItemDto> findAll() {
        return jdbcTemplate.query(SQL_FIND_ALL, (rs, rowNum) ->
                new MemberListItemDto(
                        rs.getString("member_code"),
                        rs.getString("corporate_name_kana"),
                        rs.getString("corporate_name"),
                        rs.getString("corporate_number"),
                        rs.getString("industry_category"),
                        rs.getString("rep_last_name"),
                        rs.getString("rep_first_name"),
                        rs.getString("application_status"),
                        rs.getObject("submitted_at", LocalDateTime.class),
                        rs.getLong("document_count")));
    }
}
