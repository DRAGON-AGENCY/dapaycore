package jp.co.dragonagency.dapaycore.dto;

import java.time.LocalDateTime;

/**
 * 振込手数料一覧画面の 1 行分のデータ転送オブジェクト。
 */
public class TransferFeeListItemDto {

    private String bankCode;
    private int transferFee;
    private String remarks;
    private String updateUserId;
    private LocalDateTime updatedAt;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public int getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(int transferFee) {
        this.transferFee = transferFee;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
