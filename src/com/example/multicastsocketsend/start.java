package com.example.multicastsocketsend;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class start extends Activity {
	
	public static final String folderPath = Environment
			.getExternalStorageDirectory().getPath() + "/MuGuide";
	
	RelativeLayout layout;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		
		layout=(RelativeLayout)findViewById(R.id.relativeLayout);
		layout.setOnClickListener(new OnClickListener(){

			
			//點一下進到首頁
			@Override
			public void onClick(View view) {
 				 Intent intent = new Intent();


               Bundle bundle = new Bundle();
               bundle.putString("folderPath", folderPath);
                intent.putExtras(bundle);
				 intent.setClass(start.this, TagViewer.class);
				 
			        startActivity(intent);
			        
			        finish();
			        
			        
			   
				
			}
			  
		});
		
		
		 
		 
	}
	
	
	 
}
