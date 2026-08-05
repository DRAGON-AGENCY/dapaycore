package jp.co.dragonagency.dapaycore.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 申込書類ファイルの保存・取得を担うインタフェース。
 * 保存先（ローカルディスク／将来的なファイルサーバー等）の実装を
 * 差し替えられるよう、呼び出し側は本インタフェースにのみ依存する。
 */
public interface DocumentStorageService {

    /**
     * ファイルを保存し、file_path カラムに記録する識別子を返す。
     *
     * @param memberCode   会員コード
     * @param documentType 書類種別
     * @param file         アップロードされたファイル
     * @return 保存先を表す識別子（file_path に記録する値）
     * @throws IOException 保存に失敗した場合
     */
    String store(String memberCode, String documentType, MultipartFile file) throws IOException;

    /**
     * file_path に記録された識別子からファイルを取得する。
     *
     * @param filePath 保存先を表す識別子
     * @return ファイルの内容を表すリソース
     * @throws IOException 取得に失敗した場合
     */
    Resource loadAsResource(String filePath) throws IOException;
}
