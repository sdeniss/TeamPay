package com.teampay.teampay;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by denis on 22/10/14.
 */
public class User {
    String name;
    Double income;
    Double balance;

    public User(String name, Double income, Double balance) {
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

}
