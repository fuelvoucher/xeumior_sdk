package com.smart.st8050b_demo.bean;

/**
 * Created by Administrator on 2018/6/27.
 */

public class OrderInfo {
    private String name = "";
    private int quantity = 0;
    private double price = 0;
    private String profile = "";
    private int age = 0;

    public OrderInfo(String _name,int _quantity,double _price){
        setName(_name);
        setQuantity(_quantity);
        setPrice(_price);
    }

    public OrderInfo(String _name,int _quantity,double _price,String profile,int age){
        setName(_name);
        setQuantity(_quantity);
        setPrice(_price);
        setProfile(profile);
        setAge(age);
    }

    public OrderInfo(String _name,String profile){
        setName(_name);
        setProfile(profile);
    }

    public OrderInfo(String _name,String profile,int age){
        setName(_name);
        setProfile(profile);
        setAge(age);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
