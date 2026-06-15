package jp.co.dragonagency.dapaycore.dto;

public class PostalCodeSearchResult {

    private String zipCode;
    private String prefecture;
    private String prefectureKana;
    private String city;
    private String cityKana;
    private String town;
    private String townKana;

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public String getPrefectureKana() {
        return prefectureKana;
    }

    public void setPrefectureKana(String prefectureKana) {
        this.prefectureKana = prefectureKana;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityKana() {
        return cityKana;
    }

    public void setCityKana(String cityKana) {
        this.cityKana = cityKana;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getTownKana() {
        return townKana;
    }

    public void setTownKana(String townKana) {
        this.townKana = townKana;
    }
}
