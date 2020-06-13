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
		
		//帳號密碼
        boolean flag = ftpUtils.initFTPSetting("140.119.163.23", 21, "ttsai", "456963aaa");  
		
        createFolder(folderPath + "/" + item);
      
       
        int  size = db.getItemSize(item);
         
        
         
         //文物有size個黨就下載size次         
        for(int i=1;i<=size;i++){
        
        String fileName=String.valueOf(i)+".mp3";
        String localFilePath=folderPath+"/"+item+"/"+fileName;
       
  
     
        
        
        


    	 if(ftpUtils.downLoadFile(localFilePath,item,fileName)==true){
    		 Log.e("D","成功下載"+fileName);
    		
    		
    	}
    	else {
    		 Log.w("F",fileName+"下載失敗");
		}


	
    	 
    	 File file=new File(localFilePath);
    	 
    	 //如果檔案下載不完全則刪除
    	 if(file.length()!=db.getLengthOfFile(localFilePath)){
 			Log.w("比較檔案大小",String.valueOf(file.length()));
 			Log.w("比較檔案大小",String.valueOf( db.getLengthOfFile(localFilePath)));
 			file.delete();
    	 }
 	    else
 	    {
 	    	Log.e("比較檔案大小","大小符合");
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
