package com.teampay.teampay;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by denis on 22/10/14.
 */
public class User {
    String id;
    String name;
    Double income;
    Double balance;

    public User(String id, String name, Double income, Double balance) {
        this.id = id;
        this.name = name;
        this.income = income;
        this.balance = balance;
    }

    public JSONObject toJson(){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("name", name);
            jsonObject.put("income", income);
            jsonObject.put("balance", balance);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public String getId(){
        return id;
    }

    public Double getBalance(){
        return balance;
    }

    public Double getIncome(){
        return income;
    }

    public static User fromJson(JSONObject jsonObject) throws JSONException{
        return new User(jsonObject.getString("userId"), jsonObject.getString("name"), jsonObject.getDouble("income"), jsonObject.getDouble("balance"));
    }


}
