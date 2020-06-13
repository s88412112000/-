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

 


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;


public class room extends Activity {
	
	ListView itemList;
	TextView textView_roomName;
	ImageView imageView;
	  Database db;
	  String folderPath;
	  String roomID;
	 
	   
		private Handler clientHandler;
	  boolean randomFlag=true;
	  MyAdapter adapter;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room);
		
		textView_roomName=(TextView)findViewById(R.id.roomName);
		
		itemList=(ListView)findViewById(R.id.itemList);
		imageView=(ImageView)findViewById(R.id.roomPicture);
		
		
			  db = new Database();
		Bundle bundle = getIntent().getExtras();
	    String method=bundle.getString("method");
	    Log.d("room","method="+method);
	    
	    
	    
	    
	    
	    
	    
	    //如果是打數字
		if(method.equals("type")){
			Log.v("room", "type");
		 roomID=bundle.getString("roomID");
		
		
		


		adapter=new MyAdapter(this,roomID);
		  itemList.setAdapter(adapter);
	
		 
		 
		 
		String resId=db.getPictureOfRoom(roomID);
		Resources res=getResources();
      imageView.setImageResource(res.getIdentifier(resId,"drawable",getPackageName()));
		
      
      
   
		
		textView_roomName.setText("現在所在房間："+db.getRoomName(roomID));
		
		}
		
		
		//如果是逼NFC
		else if(method.equals("NFC")){
			long nfcID=bundle.getLong("tag_id");
		
		 
			adapter=new MyAdapter(this,nfcID);
			  itemList.setAdapter(adapter);
			
			
			
			
			String resId=db.getPictureOfRoom(nfcID);
			Resources res=getResources();
	      imageView.setImageResource(res.getIdentifier(resId,"drawable",getPackageName()));
			
			
			textView_roomName.setText("現在所在房間："+db.getRoomName(nfcID));
		}
		
		
		 
		
		
		folderPath=bundle.getString("folderPath"); 
		
	
	
		
		
		 
		 
		
		itemList.setOnItemClickListener(new OnItemClickListener() {

			//按下第position個文物
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				   
				
				 
				
			//	String itemName=(String)parent.getItemAtPosition (position);
			
				 
				String itemName=adapter.getItemName(position);
				Log.e("XXXXXXXXXXXX",itemName);
				
				
				
				
				
				
				 Intent intent = new Intent();
				 Bundle bundle = new Bundle();
				 bundle.putString("folderPath", folderPath);
		        bundle.putString("itemName", itemName);
		        
		         intent.putExtras(bundle);
		         
				 intent.setClass(room.this, item.class);
				 
				 
				 
				
		         
		         
				 randomFlag=false;
				 //進到該文物頁面
			        startActivity(intent);
			        
		 
			}

			 

		});
		
		


	 
	}


	
	
	
	private Runnable clientThread = new Runnable() {

		ServerSocket serverSkt;
		Database db;
		String item;

		boolean firstPlay = true;

		public void run() {
			String TAG="clientThread.run()";
			Log.i(TAG, "開始random~~~~");


			
				db = new Database();
			
			while(randomFlag){
 


		

			try {
				int port = 0;
				serverSkt = new ServerSocket(port);
				
				
			

				 

					
				 



						item = db.getNameOfRandomItemInEnglish();
					 
						sendMultiBroadcast(serverSkt.getLocalPort());
						 
						if (TCPreceive() == true) {
							
						 

						} else {
							 
						 
						}

						
						


			} catch (IOException e) {

				Toast.makeText(room.this,
						"sendMultiBroadcast錯誤：\n" + e.toString(),
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			}
			
			 
		}

		
		//寄出訊息
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
									.equals(Integer.toString(i) + ".3gp")) {
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
					Toast.makeText(room.this,
							"oos.writeObject(g)\n" + ioe.toString(),
							Toast.LENGTH_SHORT).show();

				}

				DatagramPacket packet = new DatagramPacket(bos.toByteArray(),
						bos.size(), address, 8601);
			 

				try {

					socket.send(packet);
					Log.i(TAG,"寄出我要「"+g.name+"」的訊息");
				 
				} catch (Exception e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							room.this);
					builder.setMessage(e.toString())
							.setTitle("錯誤")
							.setPositiveButton("確定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
										 
										}
									});
					AlertDialog dialog = builder.create();
					dialog.show();
				}

			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						room.this);
				builder.setMessage("item=" + item)
						.setTitle(e.toString())
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User clicked OK button
									}
								});
				AlertDialog dialog = builder.create();
				dialog.show();
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

		 

				
               //Timeout：3秒
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
				 
				outputStream.flush();
				outputStream.close();
				inputStream.close();

				 
				clientSkt.close();

				 

				 

				return true;
				 
			} catch (SocketTimeoutException timeout) {

				 

				return false;
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						room.this);
				builder.setMessage(e.toString())
						.setTitle("TCP Receive錯誤")
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User clicked OK button
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
		  
		  
		  //取得自己IP
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
	
	protected void onResume(){
		super.onResume();
		randomFlag=true;
		HandlerThread handlerThread = new HandlerThread("room");
		handlerThread.start();
		clientHandler = new Handler(handlerThread.getLooper());
		clientHandler.post(clientThread);
		Log.d("onResume","randomFlag改成true");
	}
	
protected void onDestroy() {

		 	super.onDestroy();
		randomFlag=false;
		
	
		 db.close();

		
	}
}
