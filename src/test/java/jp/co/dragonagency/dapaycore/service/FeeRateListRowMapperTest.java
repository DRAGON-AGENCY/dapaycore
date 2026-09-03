package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.FeeRateListItemDto;
import jp.co.dragonagency.dapaycore.repository.FeeRateRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * {@link FeeRateService#findAllForList()} が使用する RowMapper の単体テスト。
 * 単体テスト仕様書_手数料一覧_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T1〜T6（■ テストケース一覧）に対応する。
 * INPUT データ:
 * C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_手数料一覧_v1.00.xlsx
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class FeeRateListRowMapperTest {

    @Mock
    private FeeRateRepository feeRateRepository;

    @Mock
    private MerchantApplicationRepository applicationRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FeeRateService service;

    @Test
    void T1_findAllForList_RowMapperがid_memberCode_startDate_statusを写す()
            throws Exception {
        RowMapper<FeeRateListItemDto> rowMapper = captureRowMapper();
        ResultSet resultSet = baseResultSet();
        when(resultSet.getLong("id")).thenReturn(101L);
        when(resultSet.getString("member_code")).thenReturn("FEE-0001");
        when(resultSet.getString("status")).thenReturn("valid");

        FeeRateListItemDto dto = rowMapper.mapRow(resultSet, 0);

        assertEquals(101L, dto.getId());
        assertEquals("FEE-0001", dto.getMemberCode());
        assertEquals("2026-04-01", dto.getStartDate());
        assertEquals("valid", dto.getStatus());
    }

    @Test
    void T2_findAllForList_corporateNameKanaがnullのとき空文字になる()
            throws Exception {
        RowMapper<FeeRateListItemDto> rowMapper = captureRowMapper();
        ResultSet resultSet = baseResultSet();
        when(resultSet.getString("corporate_name_kana")).thenReturn(null);

        FeeRateListItemDto dto = rowMapper.mapRow(resultSet, 0);

        assertEquals("", dto.getCorporateNameKana());
    }

    @Test
    void T3_findAllForList_endDateはnull許容でyyyyMMdd文字列になる()
            throws Exception {
        RowMapper<FeeRateListItemDto> rowMapper = captureRowMapper();

        ResultSet withEndDate = baseResultSet();
        when(withEndDate.getDate("end_date"))
                .thenReturn(Date.valueOf("2026-12-31"));
        assertEquals("2026-12-31",
                rowMapper.mapRow(withEndDate, 0).getEndDate());

        ResultSet withoutEndDate = baseResultSet();
        when(withoutEndDate.getDate("end_date")).thenReturn(null);
        assertNull(rowMapper.mapRow(withoutEndDate, 0).getEndDate());
    }

    @Test
    void T4_findAllForList_feeRateを小数第2位HALF_UPの文字列にする()
            throws Exception {
        RowMapper<FeeRateListItemDto> rowMapper = captureRowMapper();

        ResultSet rounded = baseResultSet();
        when(rounded.getBigDecimal("fee_rate")).thenReturn(new BigDecimal("3.245"));
        assertEquals("3.25",
                rowMapper.mapRow(rounded, 0).getFeeRateDisplay());

        ResultSet padded = baseResultSet();
        when(padded.getBigDecimal("fee_rate")).thenReturn(new BigDecimal("3.2"));
        assertEquals("3.20",
                rowMapper.mapRow(padded, 0).getFeeRateDisplay());
    }

    @Test
    void T5_findAllForList_statusをそのまま保持する() throws Exception {
        RowMapper<FeeRateListItemDto> rowMapper = captureRowMapper();

        for (String status : new String[] {"valid", "future", "expired"}) {
            ResultSet resultSet = baseResultSet();
            when(resultSet.getString("status")).thenReturn(status);

            assertEquals(status, rowMapper.mapRow(resultSet, 0).getStatus());
        }
    }

    @Test
    void T6_findAllForList_想定SQLでqueryを1回呼ぶ() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of());

        service.findAllForList();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(1))
                .query(sqlCaptor.capture(), any(RowMapper.class));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("delete_flag = false"),
                "delete_flag=false の絞込みを含むこと");
        assertTrue(sql.contains("ORDER BY f.member_code, f.start_date"),
                "member_code, start_date 昇順の並び順を含むこと");
        assertTrue(sql.contains("CURRENT_DATE"),
                "end_date と CURRENT_DATE による status 判定を含むこと");
    }

    /**
     * findAllForList() を 1 回実行し、JdbcTemplate.query へ渡された RowMapper を取り出す。
     */
    private RowMapper<FeeRateListItemDto> captureRowMapper() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of());
        service.findAllForList();
        ArgumentCaptor<RowMapper> captor =
                ArgumentCaptor.forClass(RowMapper.class);
        verify(jdbcTemplate).query(anyString(), captor.capture());
        return captor.getValue();
    }

    /**
     * RowMapper が必ず参照する列（start_date・fee_rate）を有効値で満たした ResultSet。
     * RowMapper は多数の列 getter を順に呼ぶため、lenient 設定にして
     * 「未消化のスタブと引数が違う」検査（PotentialStubbingProblem）を無効化する。
     */
    private static ResultSet baseResultSet() throws Exception {
        ResultSet resultSet =
                mock(ResultSet.class, withSettings().strictness(Strictness.LENIENT));
        when(resultSet.getDate("start_date"))
                .thenReturn(Date.valueOf("2026-04-01"));
        when(resultSet.getBigDecimal("fee_rate"))
                .thenReturn(new BigDecimal("3.00"));
        return resultSet;
    }
}
