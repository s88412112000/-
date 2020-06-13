package com.example.multicastsocketsend;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class random extends Service {

	private Handler clientHandler;
	String folderPath;
	@Override
	public void onCreate() {
		
		super.onCreate();
		
	
		
		Log.d("random", "onCreate() executed");
	}

	 
	  @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		  
			Bundle bundle = intent.getExtras();
			 folderPath=bundle.getString("folderPath"); 
		  
		  
		  HandlerThread handlerThread = new HandlerThread("random");
			handlerThread.start();
			clientHandler = new Handler(handlerThread.getLooper());
			clientHandler.post(clientThread);
			
			return super.onStartCommand(intent, flags, startId);
	}
	
	    @Override
	    public void onDestroy() {
	       
	        super.onDestroy();
	    }
	@Override
	public IBinder onBind(Intent arg0) {
	
		return null;
	}
	
	private Runnable clientThread = new Runnable() {

		ServerSocket serverSkt;
		Database db;
		String item;

		boolean firstPlay = true;

		public void run() {
			String TAG="clientThread.run()";
			
		
			
				db = new Database();
			
			while(true){
 
		
			
		

			try {
				int port = 0;
				serverSkt = new ServerSocket(port);
				
				
			

				 

					
				 
			
                         //隨便random出一個文物來
						item = db.getNameOfRandomItemInEnglish();
					 
						sendMultiBroadcast(serverSkt.getLocalPort());
						 
						if (TCPreceive() == true) {
							
						 

						} else {
							 
						 
						}

						
						
			
				
			

			

				



			} catch (IOException e) {

				Toast.makeText(random.this,
						"sendMultiBroadcast錯誤：\n" + e.toString(),
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			}
			
			 
		}

		//傳訊息
		private void sendMultiBroadcast(int port) throws IOException {

			String TAG="sendMultiBroadcast";
		

		 

			MulticastSocket socket = new MulticastSocket(8600);

			InetAddress address = InetAddress.getByName("224.0.0.1");

			socket.joinGroup(address);

			
		 

			try {

				final int size = db.getItemSize(item);

				Data g = new Data();
				g.exist = new boolean[size + 1];
				

				g.name = item;
				createFolder(folderPath + "/" + item);

				/* 判斷缺少那些檔案 */
				File file = new File(folderPath + "/" + item);
				File[] the_Files = file.listFiles();
				for (File f : the_Files) {

					if (f.isDirectory()) {

					} else {

						for (int i = 1; i <= size; i++) {

							if (f.getName()
									.equals(Integer.toString(i) + ".mp3")) {
								g.exist[i] = true;

							}
						}
					
					}

				}

				g.ip = getLocalIpAddress();
			
				g.port = port;

				ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				try {
					oos.writeObject(g);
				} catch (IOException ioe) {
					Toast.makeText(random.this,
							"oos.writeObject(g)\n" + ioe.toString(),
							Toast.LENGTH_SHORT).show();

				}

				DatagramPacket packet = new DatagramPacket(bos.toByteArray(),
						bos.size(), address, 8601);
			

				try {

					socket.send(packet);
					Log.v(TAG,"寄出我要「"+g.name+"」的訊息");
				
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							random.this);
					builder.setMessage(e.toString())
							.setTitle("錯誤")
							.setPositiveButton("確定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User clicked OK button
										}
									});
					AlertDialog dialog = builder.create();
					dialog.show();
				}

			} catch (Exception e) {
				
				e.printStackTrace();
			}

			// 退出组播
			socket.leaveGroup(address);
			socket.close();

			
		}

		//接收檔案
		private boolean TCPreceive() {
			

			String TAG="TCPreceive";

			try {
			
				int port = serverSkt.getLocalPort();

		
				

				serverSkt.setSoTimeout(3000);
				
				Log.i(TAG, "等待對方傳檔案過來....");
				Socket clientSkt = serverSkt.accept();

				Log.i(TAG, "與" + clientSkt.getInetAddress().toString() + "建立連線");

				 
				

				// 取得檔案路徑
				BufferedReader br = new BufferedReader(new InputStreamReader(
						clientSkt.getInputStream()));


				String path = br.readLine();

				Log.i(TAG,"對方說檔案的路徑要存成"+path);
				



				BufferedInputStream inputStream = new BufferedInputStream(
						clientSkt.getInputStream());


				BufferedOutputStream outputStream = new BufferedOutputStream(
						new FileOutputStream(path)); 

				OutputStream sockOut = clientSkt.getOutputStream();
				sockOut.write("FileSendNow".getBytes());

				byte[] buffer = new byte[1024 * 1024];

				int readin = 0;
				//開始接檔案
				while ((readin = inputStream.read(buffer)) != -1) {
					 
					outputStream.write(buffer, 0, readin);

					

					Thread.yield();
				}
				
				
				File file=new File(path);
				
				  //檔案接收不完全就刪除
			    if(file.length()<(db.getLengthOfFile(path)*0.95)){
					Log.w("比較檔案大小",String.valueOf(file.length()));
					Log.w("比較檔案大小",String.valueOf( db.getLengthOfFile(path)));
					file.delete();
				}
			    else
			    {
			    	Log.i("比較檔案大小","大小符合");
			    }
				


				outputStream.flush();
				outputStream.close();
				inputStream.close();



				clientSkt.close();

				 



				return true;


			} catch (SocketTimeoutException timeout) {




				return false;
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						random.this);
				builder.setMessage(e.toString())
						.setTitle("TCP Receive錯誤")
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
									
									}
								});
				AlertDialog dialog = builder.create();
				dialog.show();
				return false;
			}

		}
		
		  private void createFolder(String path) {
			   Log.d("createFolder", "建立資料夾："+path);
				File f = new File(path);
				if (f.exists() == false)
					f.mkdirs();
			}
		  
		  //取得IP位址
			public String getLocalIpAddress() throws UnknownHostException {
			 
				WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
				 
				WifiInfo wifiInfo = wifi_service.getConnectionInfo();
				 
				int ipAddress = wifiInfo.getIpAddress();
			 
				String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
						(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
						(ipAddress >> 24 & 0xff));
				return ip;

			}

	};
	
	
	

}
