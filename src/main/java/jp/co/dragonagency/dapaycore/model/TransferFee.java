package jp.co.dragonagency.dapaycore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 振込手数料マスタを表すエンティティ。
 * m_transfer_fee テーブルの 1 行に対応し、振込手数料管理画面で使用する。
 */
@Entity
@Table(name = "m_transfer_fee")
public class TransferFee {

    /** 該当する銀行コードの行が無い場合に使用する既定値の銀行コード。 */
    public static final String DEFAULT_BANK_CODE = "DEFAULT";

    @Id
    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "transfer_fee")
    private int transferFee;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "update_user_id")
    private String updateUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delete_flag")
    private boolean deleteFlag;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    public String toString() {
        return "TransferFee{"
                + "bankCode=" + bankCode
                + ", transferFee=" + transferFee
                + "}";
    }
}
