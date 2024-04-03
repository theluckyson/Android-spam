package com.example.spam;

public class Sms {
//    private int _id;
    private String address;
//    private String person;
    private String body;
    private String date;
//    private int type;

//    public int get_id(){
//        return _id;
//    }
    public String getAddress(){
        return address;
    }
//    public String getPerson(){
//        return person;
//    }
    public String getBody(){
        return body;
    }
    public String getDate(){
        return date;
    }
//    public int getType(){
//        return type;
//    }
//    public Sms(int id,String address,String person,String body,String date,int type){
//        this._id=id;
//        this.address=address;
//        this.person=person;
//        this.body=body;
//        this.date=date;
//        this.type=type;
//    }

    public Sms(String address,String body,String date){
        this.address=address;
        this.body=body;
        this.date=date;
    }
}
