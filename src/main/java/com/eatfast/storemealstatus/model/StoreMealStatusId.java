package com.eatfast.storemealstatus.model;

import java.io.Serializable;
import java.util.Objects;
 
// 複合主鍵類別必須實作 Serializable 介面
public class StoreMealStatusId implements Serializable {

    private static final long serialVersionUID = 1L; // 建議加上 serialVersionUID

    private Integer storeId; // 必須與 Entity 中的 storeId 欄位名稱和類型一致
    private Integer mealId;  // 必須與 Entity 中的 mealId 欄位名稱和類型一致

    // 必須有公共的無參數建構子
    public StoreMealStatusId() {
    }

    // 帶參數的建構子 (可選，但方便初始化)
    public StoreMealStatusId(Integer storeId, Integer mealId) {
        this.storeId = storeId;
        this.mealId = mealId;
    }

    // 必須有 Getter 和 Setter 方法 (JPA 需要)
    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public Integer getMealId() {
        return mealId;
    }

    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }

    // 必須實作 equals() 和 hashCode() 方法，基於所有主鍵欄位
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreMealStatusId that = (StoreMealStatusId) o;
        return Objects.equals(storeId, that.storeId) &&
               Objects.equals(mealId, that.mealId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, mealId);
    }
}