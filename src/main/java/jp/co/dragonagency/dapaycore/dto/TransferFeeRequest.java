package jp.co.dragonagency.dapaycore.dto;

/**
 * 振込手数料登録・更新リクエスト。
 */
public class TransferFeeRequest {

    private String bankCode;
    private Integer transferFee;
    private String remarks;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public Integer getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(Integer transferFee) {
        this.transferFee = transferFee;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
