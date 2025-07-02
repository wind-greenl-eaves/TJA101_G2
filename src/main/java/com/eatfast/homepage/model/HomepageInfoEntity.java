package com.eatfast.homepage.model;

import java.util.Arrays;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 首頁資訊實體 (Homepage Info Entity) - 【審查確認】
 * <p>
 * 此實體對應資料庫中的 `homepage_Info` 資料表，用於儲存網站首頁上顯示的內容。
 *
 * 【審查結論】:
 * 經過對資料庫結構的分析，`homepage_Info` 表是一個獨立的資料表，沒有任何其他表的外鍵關聯到它。
 * 因此，它在物件模型中是一個「葉節點」(Leaf Node)，**不應該也不需要擁有任何 @OneToMany 關聯**。
 * 您原始的程式碼結構是完全正確的，無需修改。
 * </p>
 */
@Entity
@Table(name = "homepage_Info")
public class HomepageInfoEntity {

    //================================================================
    // 							欄位定義 (Fields)
    //================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "homepage_Info_id")
    private Long homepageInfoId;

    @Lob
    @Column(name = "homepage_banner")
    private byte[] homepageBanner;

    @NotBlank(message = "介紹內容不可為空")
    @Size(max = 5000, message = "介紹內容長度不可超過 5000 個字元")
    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @Lob
    @Column(name = "picture")
    private byte[] picture;

    //================================================================
    //					 建構子 (Constructors)
    //================================================================
    public HomepageInfoEntity() {}

    //================================================================
    //					 Getters and Setters
    //================================================================
    public Long getHomepageInfoId() {
        return homepageInfoId;
    }
    public void setHomepageInfoId(Long homepageInfoId) {
        this.homepageInfoId = homepageInfoId;
    }
    public byte[] getHomepageBanner() {
        return homepageBanner;
    }
    public void setHomepageBanner(byte[] homepageBanner) {
        this.homepageBanner = homepageBanner;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public byte[] getPicture() {
        return picture;
    }
    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    //================================================================
    // 物件核心方法 (equals, hashCode, toString)
    //================================================================
    @Override
    public String toString() {
        return "HomepageInfoEntity{" +
                "homepageInfoId=" + homepageInfoId +
                ", homepageBanner(bytes)=" + (homepageBanner != null ? homepageBanner.length : 0) +
                ", content='" + content + '\'' +
                ", picture(bytes)=" + (picture != null ? picture.length : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomepageInfoEntity that = (HomepageInfoEntity) o;
        return Objects.equals(homepageInfoId, that.homepageInfoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(homepageInfoId);
    }
}
