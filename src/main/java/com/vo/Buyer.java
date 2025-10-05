package com.vo;

public class Buyer {
    // 买家姓名，1-5位，允许中文、英文、数字
    private String buyerName;

    // 买家手机号，11位数字
    private String buyerPhoneNumber;

    // 交易地址，最多20位，允许中文、英文、数字
    private String address;

    // 交易时间，格式：XXXX-XX-XX XX:XX
    private String tradeTime;

    // 无参构造方法
    public Buyer() {
    }

    // 有参构造方法
    public Buyer(String buyerName, String buyerPhoneNumber, String address, String tradeTime) {
        this.buyerName = buyerName;
        this.buyerPhoneNumber = buyerPhoneNumber;
        this.address = address;
        this.tradeTime = tradeTime;
    }

    // getter 和 setter 方法
    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerPhoneNumber() {
        return buyerPhoneNumber;
    }

    public void setBuyerPhoneNumber(String buyerPhoneNumber) {
        this.buyerPhoneNumber = buyerPhoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(String tradeTime) {
        this.tradeTime = tradeTime;
    }

    @Override
    public String toString() {
        return "Buyer{" +
                "buyerName='" + buyerName + '\'' +
                ", buyerPhoneNumber='" + buyerPhoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", tradeTime='" + tradeTime + '\'' +
                '}';
    }
}

