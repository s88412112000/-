/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2011 Adam Nyb瓣ck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.multicastsocketsend;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.TextView.OnEditorActionListener;

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device just discovered.
 */
public class TagViewer extends Activity {

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    //private LinearLayout mTagContent;

    private NfcAdapter mAdapter;
    private AlertDialog mDialog;
    String folderPath;
    Database db;
    private TextView Uid;
    EditText editText;
    boolean app_finish=false;
    Intent randomIntent ;
    
    private Handler serverHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
         
          db=new Database();
       editText=(EditText)findViewById(R.id.RoomID);
       
        
      	Bundle bundle3 = getIntent().getExtras();
        folderPath=bundle3.getString("folderPath");
       
       
       
        //TODO:random service
		  randomIntent = new Intent(getApplicationContext(), random.class);  
		 Bundle bundle = new Bundle();
		 bundle.putString("folderPath", folderPath);
		 randomIntent.putExtras(bundle);
         startService(randomIntent);  
    
        
        
    	createFolder(folderPath);
		try {
		
			
			
			//將資料庫複製到儲存空間或SD卡
			InputStream is = getResources().openRawResource(R.raw.db);
				FileOutputStream fos = new FileOutputStream(folderPath
						+ "/Mus_Database.db");
				byte buffer[] = new byte[8192];
				int count = 0;

				while ((count = is.read(buffer)) >= 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
		

		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					TagViewer.this);
			builder.setMessage(e.toString())

			.setPositiveButton("確定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User clicked OK button
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
        
		editText.setOnEditorActionListener(new OnEditorActionListener() {  
            @Override  
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
            	
            	String roomID=editText.getText().toString();
            	
            	Log.d("DDDDD",String.valueOf(db.isRoomExist(roomID)));
            	
            	//如果有這個房間
            	if(db.isRoomExist(roomID))
            	{
            	Intent intent = new Intent();
            	Bundle bundle = new Bundle();
            	bundle.putString("method", "type");
                bundle.putString("roomID", roomID);
                bundle.putString("folderPath", folderPath);
                Intent intent2=intent.setClass(TagViewer.this, room.class);
                intent.putExtras(bundle);
               
                  //進去該房間頁面
                startActivity(intent2);
            	}
              
            	//沒有這個房間
            	else
            	{
            		AlertDialog.Builder builder = new AlertDialog.Builder(
        					TagViewer.this);
        			builder.setMessage("查無此房")

        			.setPositiveButton("確定", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int id) {
        			
        				}
        			});
        			AlertDialog dialog = builder.create();
        			dialog.show();
            	}
            	
            	editText.setText("");
                
                return false;  
            }

			 
        });  
		
        
        
		
		
	

		
		
		
		
		
		//開一個Thread隨時等待訊息
		 Intent startIntent = new Intent(this, Server.class);  
		 Bundle bundle2 = new Bundle();
		 bundle2.putString("folderPath", folderPath);
		 startIntent.putExtras(bundle2);
         startService(startIntent);  
		
		
		
		
        
      

        resolveIntent(getIntent());
   

        mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
        

            finish();
            return;
        }

        
        
        
        
        
    }

    private void createFolder(String path) {
		File f = new File(path);
		if (f.exists() == false)
			f.mkdirs();
	}

	



             //NFC
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
            	            	
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
                
                
                  //取得TAG
               long nfc_id=getReversed(((Tag) tag).getId());
              Log.e("n", String.valueOf(nfc_id));
              
              //如果有對應到房間
                if(db.isRoomExist(nfc_id)){
                	
                Bundle bundle = new Bundle();
                bundle.putString("method", "NFC");
                bundle.putLong("tag_id",nfc_id);
                bundle.putString("folderPath", folderPath);
                Intent intent2=intent.setClass(this, room.class);
                intent.putExtras(bundle);
               
                //進去該房間頁面
                startActivity(intent2);
                
                }
                //沒有對應的房間
                else{
                	AlertDialog.Builder builder = new AlertDialog.Builder(
        					TagViewer.this);
        			builder.setMessage("查無對應的房間")

        			.setPositiveButton("確定", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int id) {
        					
        				}
        			});
        			AlertDialog dialog = builder.create();
        			dialog.show();
                }
                
            }
          
        }
    }

    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
      
        return sb.toString();
    }


    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

  


    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
    
    
    //取得自己IP位址
	public String getLocalIpAddress() throws UnknownHostException {
		

		WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
		 
		WifiInfo wifiInfo = wifi_service.getConnectionInfo();
		 
		int ipAddress = wifiInfo.getIpAddress();
	 
		String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
		return ip;

	}

	
	//一直一直接訊息
	private Runnable serverThread = new Runnable() {
	Database db;
	

	public void run() {
		

		db = new Database();

		 
		try {
			while (!app_finish)
				receiveMultiBroadcast();
		} catch (Exception e) {
			e.printStackTrace();
		}

		

		

	}

	

	//傳檔案
	protected void TCPsend(String ip, int port, String filePath) {

		
		String TAG="TCPsend";
				
		try {

		 

			File file = new File(filePath);

			Log.i(TAG, "遠端主機: %s%n" + ip);
			Log.i(TAG, "遠端主機連接埠:" + String.valueOf(port));
			Log.i(TAG, "傳送檔案:" + file.getName());

			Socket skt = new Socket(ip, port);
			// skt.setSoTimeout(1); //設定Timeout時間

			Log.i(TAG, "連線成功！嘗試傳送檔案....");

			// 傳路徑
			PrintStream printStream = new PrintStream(skt.getOutputStream());
			


			

			printStream.println(file.getPath());
			Log.i(TAG, "告訴它檔案的路徑是："+file.getPath());
			


			BufferedInputStream inputStream = new BufferedInputStream(
					new FileInputStream(filePath));

			String serverInfo = servInfoBack(skt);
			if (serverInfo.equals("FileSendNow")) {

				byte[] buffer = new byte[1024];

				int readin = 0;
				long size = 0;
				final long FileSize = file.length();

			


				// 傳送檔案
				while ((readin = inputStream.read(buffer)) != -1) {
					

					printStream.write(buffer, 0, readin);
				


					size += readin;

					 
				

					Thread.yield();
				}
			

				printStream.flush();
				printStream.close();
				inputStream.close();
			}
			skt.close();

			Log.i(TAG, "檔案傳送完畢！");
		


		} catch (SocketTimeoutException timeout) {
		

		} catch (FileNotFoundException fnfe) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					TagViewer.this);
			builder.setMessage(fnfe.toString())
					.setTitle("TCP Send錯誤：找不到檔案")
					.setPositiveButton("確定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									

								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();

		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					TagViewer.this);
			builder.setMessage(e.toString())
					.setTitle("TCP Send錯誤")
					.setPositiveButton("確定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									


								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	public String servInfoBack(Socket sock) throws Exception 

	{
		InputStream sockIn = sock.getInputStream(); 

		byte[] bufIn = new byte[1024];
		int lenIn = sockIn.read(bufIn);  
		String info = new String(bufIn, 0, lenIn);
		return info;
	}

	
	//接訊息
	protected void receiveMultiBroadcast()   {
		String TAG="receiveMultiBroadcast";
		 

		

	
		;
		try {
         	InetAddress address = InetAddress.getByName("224.0.0.1");
			MulticastSocket socket = new MulticastSocket(8601);

			socket.joinGroup(address);

			 
			byte[] rev = new byte[2048];

		

			DatagramPacket packet = new DatagramPacket(rev, rev.length);

			
	

			 
           Log.i(TAG,"等待訊息中...");
			socket.receive(packet);

			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(rev));

			
			//接到訊息後做拆解
			try {

				String s;
				Data d2 = (Data) ois.readObject();


				s = "文物：";
				s += d2.name + "\n缺少：";

				for (int i = 1; i < d2.exist.length; i++) {
					if (d2.exist[i] == false)
						s += String.valueOf(i) + " ";
					

				}
 
				

				if (d2.ip.equals(getLocalIpAddress()) == false) {
					final String fs = s;
					
                     Log.d(TAG, "收到對方訊息："+s);
        
				 
					int size = db.getItemSize(d2.name);
					boolean none = true;

					// 判斷自己有什麼
					boolean[] belongings = new boolean[size + 1];
					File file = new File(folderPath + "/" + d2.name);
					File[] the_Files = file.listFiles();
					for (File f : the_Files) {

						if (f.isDirectory()) {

						} else {

							for (int i = 1; i <= size; i++) {

								//如果自己有i.mp3，belongings[i]=true
								if (f.getName().equals(
										Integer.toString(i) + ".mp3")) {
									belongings[i] = true;
									none = false;
									

								}
							}
						

						}

					}

					if (none == false) {
						// random決定要傳啥

						// 有X個檔就跑3X次
						for (int i = 0; i < size * 3; i++) {

							int j = (int) (Math.random() * size + 1);

							if (belongings[j] == true
									&& d2.exist[j] == false) {

							 
								 
                                  //傳送檔案
								TCPsend(d2.ip, d2.port, folderPath + "/"
										+ d2.name + "/" + String.valueOf(j)
										+ ".3gp");
								break;

							}

						}
					}
				}
			} catch (ClassNotFoundException cnfe) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						TagViewer.this);
				builder.setMessage(cnfe.toString())
						.setTitle("UDP Receive錯誤")
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog, int id) {
										// User clicked OK button

									}
								});
				AlertDialog dialog = builder.create();
				dialog.show();
			} catch (IOException ioe) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						TagViewer.this);
				builder.setMessage(ioe.toString())
						.setTitle("UDP Receive錯誤")
						.setPositiveButton("確定",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog, int id) {
										 

									}
								});
				AlertDialog dialog = builder.create();
				dialog.show();
			}

		 
			socket.close();
		} catch (IOException ioe) {
			 
			ioe.printStackTrace();
			
		}

	}
};
protected void onDestroy() {

	 

	
	app_finish=true;
	 
    db.close();
	super.onDestroy();
}
    
}
