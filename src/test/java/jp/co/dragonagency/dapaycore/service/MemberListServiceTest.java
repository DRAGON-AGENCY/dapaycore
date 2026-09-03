package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.MemberListItemDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.LocalDateTime;
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

/**
 * {@link MemberListService} の単体テスト。
 * 単体テスト仕様書_会員一覧_自社mPOSキャッシュレス管理システム_v1.00.xlsx の
 * 項番 T10〜T13（■ テストケース一覧）に対応する。
 * INPUT データ:
 * C:\work\自社mPOS精算システム\単体テスト\単体テストINPUTデータ_会員一覧_v1.00.xlsx
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class MemberListServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MemberListService service;

    @Test
    void T10_findAll_RowMapperが各列をDTOへ写す() throws Exception {
        RowMapper<MemberListItemDto> rowMapper = captureRowMapper();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("member_code")).thenReturn("MEM-0001");
        when(resultSet.getString("corporate_name"))
                .thenReturn("株式会社テスト商事");
        when(resultSet.getString("rep_last_name")).thenReturn("山田");
        when(resultSet.getString("rep_first_name")).thenReturn("太郎");
        when(resultSet.getString("application_status")).thenReturn("APPROVED");
        when(resultSet.getObject("submitted_at", LocalDateTime.class))
                .thenReturn(LocalDateTime.of(2026, 8, 20, 10, 0));
        when(resultSet.getLong("document_count")).thenReturn(3L);

        MemberListItemDto dto = rowMapper.mapRow(resultSet, 0);

        assertEquals("MEM-0001", dto.getMemberCode());
        assertEquals("株式会社テスト商事", dto.getCorporateName());
        assertEquals("山田 太郎", dto.getRepName());
        assertEquals("APPROVED", dto.getApplicationStatus());
        assertEquals(LocalDateTime.of(2026, 8, 20, 10, 0), dto.getSubmittedAt());
        assertEquals(3L, dto.getDocumentCount());
    }

    @Test
    void T11_findAll_submittedAtがnullでもDTOを生成する() throws Exception {
        RowMapper<MemberListItemDto> rowMapper = captureRowMapper();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("member_code")).thenReturn("MEM-0002");
        when(resultSet.getObject("submitted_at", LocalDateTime.class))
                .thenReturn(null);

        MemberListItemDto dto = rowMapper.mapRow(resultSet, 0);

        assertNull(dto.getSubmittedAt());
    }

    @Test
    void T12_findAll_documentCountが0のときDTOも0() throws Exception {
        RowMapper<MemberListItemDto> rowMapper = captureRowMapper();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("member_code")).thenReturn("MEM-0003");
        when(resultSet.getLong("document_count")).thenReturn(0L);

        MemberListItemDto dto = rowMapper.mapRow(resultSet, 0);

        assertEquals(0L, dto.getDocumentCount());
    }

    @Test
    void T13_findAll_想定SQLでqueryを1回呼ぶ() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of());

        service.findAll();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(1))
                .query(sqlCaptor.capture(), any(RowMapper.class));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains("delete_flag = false"),
                "delete_flag=false の絞込みを含むこと");
        assertTrue(sql.contains("submitted_at DESC NULLS LAST"),
                "submitted_at DESC NULLS LAST の並び順を含むこと");
    }

    /**
     * findAll() を 1 回実行し、JdbcTemplate.query へ渡された RowMapper を取り出す。
     */
    private RowMapper<MemberListItemDto> captureRowMapper() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of());
        service.findAll();
        ArgumentCaptor<RowMapper> captor =
                ArgumentCaptor.forClass(RowMapper.class);
        verify(jdbcTemplate).query(anyString(), captor.capture());
        return captor.getValue();
    }
}
