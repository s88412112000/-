package com.example.multicastsocketsend;
  
  
import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.OutputStream;  
import java.net.SocketException;  
  
import org.apache.commons.net.ftp.FTP;  
import org.apache.commons.net.ftp.FTPClient;  
import org.apache.commons.net.ftp.FTPFile;  
import org.apache.commons.net.ftp.FTPReply;  
import org.apache.commons.net.io.CopyStreamAdapter;
  
import android.R.bool;  
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
  

public class FTPUtils {  
	 private FTPClient ftpClient = null;  
	    private static FTPUtils ftpUtilsInstance = null;  
	    private String FTPUrl;  
	    private int FTPPort;  
	    private String UserName;  
	    private String UserPassword;  
	      
	    private FTPUtils()  
	    {  
	        ftpClient = new FTPClient();  
	    }  

 
	    public  static FTPUtils getInstance() {  
	        if (ftpUtilsInstance == null)  
	        {  
	            ftpUtilsInstance = new FTPUtils();  
	        }  
	        return ftpUtilsInstance;  
	    }  
	      
	    /** 
	     * FTP設定
	     * @param FTPUrl   IP
	     * @param FTPPort   Port
	     * @param UserName     帳號
	     * @param UserPassword   密碼
	     * @return 
	     */  
	    public boolean initFTPSetting(String FTPUrl, int FTPPort, String UserName, String UserPassword)  
	    {     
	        this.FTPUrl = FTPUrl;  
	        this.FTPPort = FTPPort;  
	        Log.d("p", String.valueOf(FTPPort));
	        this.UserName = UserName;  
	        this.UserPassword = UserPassword;  
	          
	        int reply;  
	          
	        try {  
	            //1.連接
	            ftpClient.connect(FTPUrl, FTPPort);  
	              
	            //2.登入
	            ftpClient.login(UserName, UserPassword);  
	              
	            //3.看return的值是不是230，是的話表示登入成功
	            reply = ftpClient.getReplyCode();  
	          
	            if (!FTPReply.isPositiveCompletion(reply))  
	            {  
	                
	                ftpClient.disconnect();  
	                return false;  
	            }  
	              
	             
	            return true;  
	              
	        } catch (SocketException e) {  
	            
	            e.printStackTrace();  
	            return false;  
	        } catch (IOException e) {  
	           
	            e.printStackTrace();  
	            return false;  
	        }  
	    }  
	      
	   
	    


	    public boolean downLoadFile(String FilePath, String item,String FileName) {  
	          
	    	Log.d("downLoadFile","downLoadFile");
	    	
	        if (!ftpClient.isConnected())  
	        {  
	            if (!initFTPSetting(FTPUrl,  FTPPort,  UserName,  UserPassword))  
	            {  
	                return false;  
	            } 
	            Log.w("downLoadFile", "FTP連線失敗");
	        }  
	           
	        try {  
	            // 進到item資料夾裡
	        	ftpClient.changeWorkingDirectory("/"+item);
	         
	          
	            ftpClient.setFileType(FTP.BINARY_FILE_TYPE); 
	            
	            
	              
	             //client路徑
	            FileOutputStream fos = new FileOutputStream( FilePath);
	            
	            
	             
	            Log.i("me","開始下載");
	             
	           
	            try{
	            	//下載
	            ftpClient.retrieveFile( FileName , fos );
	            }
	            catch(IOException ioe ){
	            	Log.e("FTPUtils",ioe.toString());
	            	 
	            }
	            Log.i("me","下載結束");
	            fos.close();
	              
	              
	            //登出
	            ftpClient.logout();  
	            ftpClient.disconnect(); 
	            return true;  
	          
	              
	        } catch (IOException e) {  
	            Log.e("downLoadFile",e.toString());
	            e.printStackTrace();  
	            return false;  
	        }  
	          
	        
	    }
	    


   
      
} 