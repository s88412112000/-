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
import android.app.ProgressDialog;
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
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class item extends Activity {
	
	RelativeLayout relativeLayout_playerButtons;
	TextView textView_itemName;
	  TextView textView_itemNameInEnglish;
	TextView textView_nowPlaying;
	ScrollView scrollView;
	TextView textView_introduction;
	Button button_fullPlay;
	Button button_quickPlay;
	Button button_showIntroduction;
	
	ImageView item_picture;
	ImageView button_pause;
	ImageView button_rewind;
	ImageView button_forward;
	Database db;
	String folderPath;
	String itemName;
	
	MediaPlayer mediaPlayer;
	int nowPlaying=0;
	Handler ftpHandler;
	private Handler clientHandler;
	boolean activity_finish=false;
	boolean firstPlay = true;
	  
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item);
		
		db=new Database();
		
		textView_itemName=(TextView)findViewById(R.id.itemName);
		textView_itemNameInEnglish=(TextView)findViewById(R.id.nameInEnglish);
		textView_nowPlaying=(TextView)findViewById(R.id.nowPlaying);
		textView_introduction=(TextView)findViewById(R.id.introduction);
		relativeLayout_playerButtons=(RelativeLayout)findViewById(R.id.playerButtons);
		
		
		
		 Bundle bundle = getIntent().getExtras();
		 folderPath=bundle.getString("folderPath"); 
		 itemName=bundle.getString("itemName");
		 
		 scrollView=(ScrollView)findViewById(R.id.item_scrollView);
		
		 //UI顯示
		 textView_itemName.setText(itemName);
		 textView_itemNameInEnglish.setText(db.translateNameIntoEnglish(itemName));
		 Log.w("T",db.translateNameIntoEnglish(itemName));
		 
		 textView_introduction.setText(db.getIntroductionOfItem(itemName));
		 
		 item_picture=(ImageView)findViewById(R.id.picture);
		 String resId=db.getPictureOfItem(itemName);
		 try{
		
			Resources res=getResources();
	      item_picture.setImageResource(res.getIdentifier(resId,"drawable",getPackageName()));
		 }catch(Exception e){
			 Log.w("item", "resID="+resId);
		 }
		 
		 button_fullPlay=(Button)findViewById(R.id.fullPlay);
		 button_fullPlay.setOnClickListener(new OnClickListener() {
			
			 
			 //按下完整播放
			@Override
			public void onClick(View v) {
				
				if(mediaPlayer!=null && mediaPlayer.isPlaying())
				{
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				
				firstPlay=true;
				
				
				
				File f=new File(folderPath+"/"+db.translateNameIntoEnglish(itemName));
				
				//如果檔案齊全
			if(f.exists() && f.list()!=null && f.list().length==db.getItemSize(itemName)){
				

				playAudio(db.translateNameIntoEnglish(itemName), 1);
			}
			
			//如果不齊全
			else{
				final ProgressDialog progressDialog=ProgressDialog.show(item.this,"FTP下載中","請稍後",true,false);
					
					
					HandlerThread ftpHandlerThread = new HandlerThread("item");
					ftpHandlerThread.start();
					 
					ftpHandler = new Handler(ftpHandlerThread.getLooper())
					{
						  public void handleMessage(Message msg) {
							  Log.d("QQ","dismiss");
							  progressDialog.dismiss();
							  


							  //載完後播放
							  playAudio(db.translateNameIntoEnglish(itemName), 1);
							  
						  };
						
					};

				
					


                    //開一個Thread做FTP下載
						ftpThread ftp = new ftpThread(ftpHandler,
								folderPath,
								db.translateNameIntoEnglish(itemName));
						
					
					ftpHandler.post(ftp);
			}
		
			
			}
			
		});
		 button_quickPlay=(Button)findViewById(R.id.quickPlay);
		 button_quickPlay.setOnClickListener(new OnClickListener(){

			 
			 //按下快速播放
			@Override
			public void onClick(View arg0) {
				 
				if(mediaPlayer!=null && mediaPlayer.isPlaying())
				{
					mediaPlayer.stop();
					mediaPlayer.release();
				}
				
				
				 
				  
				firstPlay=true;
				HandlerThread handlerThread = new HandlerThread("item");
				handlerThread.start();
				clientHandler = new Handler(handlerThread.getLooper());
				clientHandler.post(clientThread);
			}
			 
		 });
		 
		 button_showIntroduction=(Button)findViewById(R.id.introduction_switch);
		 button_showIntroduction.setOnClickListener(new OnClickListener() {
			
			 
			 //按下文字介紹
			@Override
			public void onClick(View v) {
				
			 
				
				if(scrollView.getVisibility()==View.INVISIBLE){
				item_picture.setAlpha((float)0.1);
				scrollView.setVisibility(View.VISIBLE);
				button_showIntroduction.setText("顯示圖片");
				}
				else {
					item_picture.setAlpha((float)1);
					scrollView.setVisibility(View.INVISIBLE);
					button_showIntroduction.setText("文字簡介");
				}
			}
		});
		 
		 
			
		
			button_pause=(ImageView)findViewById(R.id.pause);
			button_pause.setOnClickListener(new OnClickListener(){

				//按下暫停
				@Override
				public void onClick(View v) {
					if(mediaPlayer!=null ){
						
						if( mediaPlayer.isPlaying()){
						mediaPlayer.pause();
						   
						button_pause.setImageResource(R.drawable.player_play);
						}
						else{
							mediaPlayer.start();
		                     	
							button_pause.setImageResource(R.drawable.player_pause);
						}
					}
					
				}
				
			});
			
			
		
			button_rewind=(ImageView)findViewById(R.id.rewind);
			button_rewind.setOnClickListener(new OnClickListener() {
				
				//按下倒退
				@Override
				public void onClick(View v) {
					
					//如果現在再播的，已經是第一段
					if (nowPlaying - 1 < 1)
						Toast.makeText(item.this, "無前一段", Toast.LENGTH_SHORT)
								.show();
					else {
						if (mediaPlayer != null) {

							if (mediaPlayer.isPlaying()) {
								mediaPlayer.stop();
								 

							}

							 //播前一段
							playAudio(db.translateNameIntoEnglish(itemName),
									nowPlaying - 1);
						}
					}
					
				}
			});
			button_forward=(ImageView)findViewById(R.id.forward);
			button_forward.setOnClickListener(new OnClickListener() {
				
				//按下快轉
				@Override
				public void onClick(View v) {
                    
					//如果已經是播最後一段
				if (nowPlaying + 1 > db.getItemSize(itemName))
					Toast.makeText(item.this, "無下一段", Toast.LENGTH_SHORT)
							.show();
				else {
					if (mediaPlayer != null) {

						if (mediaPlayer.isPlaying()) {
						
							
							 mediaPlayer.stop();
						 

						}

						
						//播下一段
						playAudio(db.translateNameIntoEnglish(itemName),
								nowPlaying + 1);
								
					}
				}
					
				}
			});
			
			 
	}
	
	   private void createFolder(String path) {
		   Log.d("createFolder", "建立資料夾："+path);
			File f = new File(path);
			if (f.exists() == false)
				f.mkdirs();
		}



	
	//告訴別人我要這個文物並接收檔案
	private Runnable clientThread = new Runnable() {

		ServerSocket serverSkt;
		
        String item;


		public void run() {
			String TAG="clientThread.run()";
			Log.v(TAG, "clientThread開始");
			item=db.translateNameIntoEnglish(itemName);


			
			createFolder(folderPath + "/" + item);
			
			while(!activity_finish){
 



			try {
				int port = 0;
				serverSkt = new ServerSocket(port);
				
				
		

					


					
					//如果資料夾不是空的
					if(new File(folderPath + "/" + item).exists()){
					if (firstPlay == true
							&& new File(folderPath + "/" + item).list().length != 0) {

						playAudio(item, 1);



					}

					//如果資料夾是空的
					else if(new File(folderPath + "/" + item).list().length == 0)
					{
						runOnUiThread(new Runnable() { public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									item.this);
							builder.setMessage("檔案欠缺中，請等候")
									.setTitle("請等候")
									.setPositiveButton("確定",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog,
														int id) {
													 
												}
											});
							AlertDialog dialog = builder.create();
							dialog.show();
							
							 } });
					 
					}
				 
					}
					
					
					try{
						
						
						//發出30次訊息並等待接收
					for(int i=0;i<30;i++){
						
						int nowAmount;
						if(new File(folderPath + "/" + item).list()==null)
						{
							nowAmount=0;
							
						}
						else {
							nowAmount=new File(folderPath + "/" + item).list().length;
						}
						
						
						
						
						
						
						
						
						//如果檔案全部都齊全就離開迴圈
						if(nowAmount==db.getItemSize(item))
						{
						
						
						break;
						
						}
					 
						
						//送出訊息
					sendMultiBroadcast(serverSkt.getLocalPort());

					//如果接收檔案成功
					if (TCPreceive() == true) {
				 
						 

						if (firstPlay == true) {

							
							playAudio(item, 1);
						 
 
						}
						
						
						
						
						
						
						

					}
					
					//沒接到檔案
					else {
						 
						
						Log.w("clientThread.run()","Timeout");
						
					}
					
					if(activity_finish)
						break;
					}
					
				}catch(Exception e){
					
					e.printStackTrace();
				}


			} catch (IOException e) {

				Toast.makeText(item.this,
						"sendMultiBroadcast錯誤：\n" + e.toString(),
						Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			
			
			try {
				serverSkt.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			}
			
			 
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
			 

				/* 判斷缺少那些檔案 */
				File file = new File(folderPath + "/" + item);
				File[] the_Files = file.listFiles();
				for (File f : the_Files) {

					if (f.isDirectory()) {

					} else {

						for (int i = 1; i <= size; i++) {
                               //如果i.mp3存在，exist[i]=true
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
					Toast.makeText(item.this,
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
							item.this);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(
						item.this);
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

			// 退出
			socket.leaveGroup(address);
			socket.close();




		}

		private boolean TCPreceive() {
			

			String TAG="TCPreceive";

			try {


				int port = serverSkt.getLocalPort();




				
                   //Timeout：10秒
				serverSkt.setSoTimeout(10000);


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
				Log.i("TCPReceive","接收完畢");
				


				clientSkt.close();

				 




				return true;
			 
			} catch (SocketTimeoutException timeout) {




				return false;
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						item.this);
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

		

	};
	
	
	
	
	 void playAudio(final String item, final int playing) {

		 
	
		 
			File f = new File(folderPath + "/" + item + "/"
					+ String.valueOf(playing) + ".mp3");
			
			//沒檔案了
			if (playing > db.getItemSize(item))
				return;
			
			//檔案不存在，就跳過先播下一個
			else if (f.exists() == false)
				playAudio(item, playing + 1);

			else {
 
				
				
				
				firstPlay = false;

				runOnUiThread(new Runnable() {

					 

					public void run() {

					 
						mediaPlayer = new MediaPlayer();
						try {

						 

							
							//設定路徑
							mediaPlayer.setDataSource(folderPath + "/" + item
									+ "/" + String.valueOf(playing) + ".mp3");
							mediaPlayer.prepare();
							mediaPlayer
									.setOnCompletionListener(new OnCompletionListener() {
										
										//播完之後
										@Override
										public void onCompletion(MediaPlayer mp) {
											runOnUiThread(new Runnable() {

												public void run() {

													nowPlaying=0;
													textView_nowPlaying.setText("");
													relativeLayout_playerButtons.setVisibility(View.INVISIBLE);
													
													//播下一段
													playAudio(item, playing + 1);

												}// end of run
											});

										 
										}// end of onCompletion
									});

						 
                          
							nowPlaying=playing;
							textView_nowPlaying.setText(String.valueOf(playing)+"/"+db.getItemSize(item));
							
							//開始播
							mediaPlayer.start();
							button_pause.setImageResource(R.drawable.player_pause);
							relativeLayout_playerButtons.setVisibility(View.VISIBLE);


						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
							Log.e("PlayAudio", "路徑："+folderPath + "/" + item
									+ "/" + String.valueOf(playing) + ".mp3");
						}
					}

				});// end of runOnUi
			}
		}
	protected void onDestroy() {

		 

		 db.close();
		activity_finish=true;
		
		if(mediaPlayer!=null && mediaPlayer.isPlaying())
		{
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		firstPlay=true;
		

		super.onDestroy();
	}
}
