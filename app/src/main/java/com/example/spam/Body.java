package com.example.spam;

public class Body {
    private String body;
    private String date;

    public String getBody(){
        return body;
    }
    public String getDate(){
        return date;
    }

    public Body(String body,String date){
        this.body=body;
        this.date=date;
    }
}
