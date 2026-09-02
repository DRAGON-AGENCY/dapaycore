package jp.co.dragonagency.dapaycore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * ネットスターズ還元データ日次取込の稼働設定を表すエンティティ。
 * m_netstars_import_config テーブルの 1 行（config_key = {@link #KEY_DAILY_IMPORT}）に対応する。
 */
@Entity
@Table(name = "m_netstars_import_config")
public class NetStarsImportConfig {

    /** 日次取込の稼働設定を表す config_key の固定値。 */
    public static final String KEY_DAILY_IMPORT = "DAILY_IMPORT";

    @Id
    @Column(name = "config_key")
    private String configKey;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    @Column(name = "auto_resumed")
    private boolean autoResumed;

    @Column(name = "note")
    private String note;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "update_user_id")
    private String updateUserId;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getStoppedAt() {
        return stoppedAt;
    }

    public void setStoppedAt(LocalDateTime stoppedAt) {
        this.stoppedAt = stoppedAt;
    }

    public boolean isAutoResumed() {
        return autoResumed;
    }

    public void setAutoResumed(boolean autoResumed) {
        this.autoResumed = autoResumed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    @Override
    public String toString() {
        return "NetStarsImportConfig{"
                + "configKey=" + configKey
                + ", enabled=" + enabled
                + ", stoppedAt=" + stoppedAt
                + "}";
    }
}
