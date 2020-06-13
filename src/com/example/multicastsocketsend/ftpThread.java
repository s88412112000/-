package com.example.multicastsocketsend;

import java.io.File;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
 



public class ftpThread implements Runnable{

	Handler handler;
	String folderPath;
 
	String item;
 
	Database db;


	public ftpThread(Handler h,String path,String i){
		handler=h;
		folderPath=path;
		item=i;
	}
	
	
	@Override
	public void run() {
		db=new Database();
		FTPUtils ftpUtils = FTPUtils.getInstance();  
		
		//�b���K�X
        boolean flag = ftpUtils.initFTPSetting("140.119.163.23", 21, "ttsai", "456963aaa");  
		
        createFolder(folderPath + "/" + item);
      
       
        int  size = db.getItemSize(item);
         
        
         
         //�媫��size���ҴN�U��size��         
        for(int i=1;i<=size;i++){
        
        String fileName=String.valueOf(i)+".mp3";
        String localFilePath=folderPath+"/"+item+"/"+fileName;
       
  
     
        
        
        


    	 if(ftpUtils.downLoadFile(localFilePath,item,fileName)==true){
    		 Log.e("D","���\�U��"+fileName);
    		
    		
    	}
    	else {
    		 Log.w("F",fileName+"�U������");
		}


	
    	 
    	 File file=new File(localFilePath);
    	 
    	 //�p�G�ɮפU���������h�R��
    	 if(file.length()!=db.getLengthOfFile(localFilePath)){
 			Log.w("����ɮפj�p",String.valueOf(file.length()));
 			Log.w("����ɮפj�p",String.valueOf( db.getLengthOfFile(localFilePath)));
 			file.delete();
    	 }
 	    else
 	    {
 	    	Log.e("����ɮפj�p","�j�p�ŦX");
 	    }
        
        }
        
        
        
        
        
        Message msg=handler.obtainMessage(1, "finish");
        handler.sendMessage(msg);
	}
	
	private void createFolder(String path) {

		File f = new File(path);
		if (f.exists() == false)
			f.mkdirs();
	}

}
