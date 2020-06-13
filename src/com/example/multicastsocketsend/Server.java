package com.example.multicastsocketsend;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class Server extends Service {

	
	private Handler serverHandler;
		public static final String TAG = "MyService";
		boolean service_finish=false;
		Database db;
		String folderPath;

		@Override
		public void onCreate() {
			
			super.onCreate();
			
		
			
			Log.d(TAG, "onCreate() executed");
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.d(TAG, "onStartCommand() executed");
			
			Bundle bundle = intent.getExtras();
			 folderPath=bundle.getString("folderPath"); 
			
			 
			db = new Database();

			HandlerThread handlerThread2 = new HandlerThread("Server");
			handlerThread2.start();
			serverHandler = new Handler(handlerThread2.getLooper());
			serverHandler.post(serverThread);
			
			
			return super.onStartCommand(intent, flags, startId);
		}
		
		
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			Log.d(TAG, "onDestroy() executed");
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		
		private Runnable serverThread = new Runnable() {

			

			public void run() {
				try {
					while (!service_finish)
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

					// 傳檔名
					PrintStream printStream = new PrintStream(skt.getOutputStream());
					// printStream.println(file.getParent());

					/*
					 * final File f = file;
					 * 
					 * runOnUiThread(new Runnable() { public void run() {
					 * tv.append("\nParent=" + f.getParent()); } });
					 */
					printStream.println(file.getPath());
					Log.i(TAG, "告訴它檔案的路徑是："+file.getPath());
					// printStream.println(filePath);

					BufferedInputStream inputStream = new BufferedInputStream(
							new FileInputStream(filePath));

					String serverInfo = servInfoBack(skt);
					if (serverInfo.equals("FileSendNow")) {

						byte[] buffer = new byte[1024];

						int readin = 0;
						long size = 0;
						final long FileSize = file.length();

					 

						// 開始傳送檔案
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
							Server.this);
					builder.setMessage(fnfe.toString())
							.setTitle("TCP Send錯誤：找不到檔案")
							.setPositiveButton("確定",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int id) {
											// User clicked OK button

										}
									});
					AlertDialog dialog = builder.create();
					dialog.show();

				} catch (Exception e) {
					e.printStackTrace();
				 
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
			public String getLocalIpAddress() throws UnknownHostException {
				 
				WifiManager wifi_service = (WifiManager) getSystemService(WIFI_SERVICE);
			 
				WifiInfo wifiInfo = wifi_service.getConnectionInfo();
				 
				int ipAddress = wifiInfo.getIpAddress();
			 
				String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
						(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
						(ipAddress >> 24 & 0xff));
				return ip;

			}

			protected void receiveMultiBroadcast()   {
				String TAG="receiveMultiBroadcast";
				 

				 

			
				 
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

										//如果我有i.mp3，belongings[i]=true
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

									 
									 

										TCPsend(d2.ip, d2.port, folderPath + "/"
												+ d2.name + "/" + String.valueOf(j)
												+ ".mp3");
										break;

									}

								}
							}
						}
					} catch (ClassNotFoundException cnfe) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								Server.this);
						builder.setMessage(cnfe.toString())
								.setTitle("UDP Receive錯誤")
								.setPositiveButton("確定",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog, int id) {
											 
											}
										});
						AlertDialog dialog = builder.create();
						dialog.show();
					} catch (IOException ioe) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								Server.this);
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
		
		
}
