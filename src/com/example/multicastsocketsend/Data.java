package com.example.multicastsocketsend;

import java.io.Serializable;

 

public class Data  implements Serializable{
	String  name;     //文物名稱
	boolean[] exist;   //已經有哪些檔案
    String ip;         //IP位址
    int port;          //Port


}
