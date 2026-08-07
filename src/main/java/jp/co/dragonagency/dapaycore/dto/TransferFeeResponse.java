package jp.co.dragonagency.dapaycore.dto;

/**
 * 振込手数料取得・登録・更新レスポンス。
 */
public class TransferFeeResponse {

    private String bankCode;
    private int transferFee;
    private String remarks;

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
}
