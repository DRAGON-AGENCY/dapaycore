package jp.co.dragonagency.dapaycore.service;

import jp.co.dragonagency.dapaycore.dto.MemberListItemDto;
import jp.co.dragonagency.dapaycore.model.MerchantApplication;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationDocumentRepository;
import jp.co.dragonagency.dapaycore.repository.MerchantApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会員一覧画面のデータ取得を担うサービス。
 * m_merchant_application と m_merchant_application_document を 2 クエリで取得し結合する。
 */
@Service
public class MemberListService {

    private final MerchantApplicationRepository applicationRepository;
    private final MerchantApplicationDocumentRepository documentRepository;

    public MemberListService(
            MerchantApplicationRepository applicationRepository,
            MerchantApplicationDocumentRepository documentRepository) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberListItemDto> findAll() {
        List<MerchantApplication> applications =
                applicationRepository.findAllOrderBySubmittedAtDesc();

        Map<String, Long> docCountMap = buildDocumentCountMap();

        return applications.stream().map(a -> new MemberListItemDto(
                a.getMemberCode(),
                a.getCorporateNameKana(),
                a.getCorporateName(),
                a.getCorporateNumber(),
                a.getIndustryCategory(),
                a.getRepLastName(),
                a.getRepFirstName(),
                a.getApplicationStatus(),
                a.getSubmittedAt(),
                docCountMap.getOrDefault(a.getMemberCode(), 0L)
        )).collect(Collectors.toList());
    }

    private Map<String, Long> buildDocumentCountMap() {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : documentRepository.countGroupByMemberCode()) {
            String memberCode = (String) row[0];
            Long count = (Long) row[1];
            if (memberCode != null) {
                map.put(memberCode, count);
            }
        }
        return map;
    }
}
