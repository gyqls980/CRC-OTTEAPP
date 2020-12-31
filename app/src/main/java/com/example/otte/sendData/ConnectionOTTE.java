package com.example.otte.sendData;

public class ConnectionOTTE {

    public String access;
    public String refresh;
    public String username;
    public String password;
    public String date;
    public String time;
    public Integer level;

    public ConnectionOTTE(String username, String password){
        this.username = username;
        this.password = password;
    }

    public ConnectionOTTE(String username, String date, String time, Integer level){
        this.username = username;
        this.date = date;
        this.time = time;
        this.level = level;
    }

        // toString()을 Override 해주지 않으면 response.body()는 객체의 주소값을 출력
//        @Override
//        public String toString() {
//            return "PostResult{" +
//                    "access=" + access +
//                    ", refresh=" + refresh +
//                    '}';
//        }
}
