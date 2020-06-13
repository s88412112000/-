package com.example.multicastsocketsend;

import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyAdapter extends BaseAdapter {
	private LayoutInflater adapterLayoutInflater;
	
	String roomID;
	long nfcID;
	Vector<String> itemNames;
	Vector<String> icons;
	Database db;
	
	public MyAdapter(Context c,String room){
		adapterLayoutInflater = LayoutInflater.from(c);

		roomID=room;
		db=new Database();
		
		itemNames=db.getNameOfALLItemByRoomId(roomID);
		icons=db.getIconOfALLItemByRoomId(roomID);
		
	}
	public MyAdapter(Context c,long NFC){
		adapterLayoutInflater = LayoutInflater.from(c);

		nfcID=NFC;
		db=new Database();
		
		itemNames=db.getNameOfALLItemByNFC(nfcID);
		icons=db.getIconOfALLItemByNFC(nfcID);
	}
	
	
	
	
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getItemName(int position){
		if(itemNames!=null)
			return itemNames.elementAt(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		TagView tag;
		if(view == null){
			view = adapterLayoutInflater.inflate(R.layout.adapter, null);
			tag = new TagView(
					 
					(ImageView)view.findViewById(R.id.AdapterImage),
					(TextView)view.findViewById(R.id.AdapterText));
			view.setTag(tag);
		}
		else{
			tag = (TagView)view.getTag();
		}
		
		
	 
		 
		//tag.text.setText("text"+position);
		tag.text.setText(itemNames.elementAt(position));
	
		String resId=db.getIconOfItem(itemNames.elementAt(position));
		//String resId= "ico_jadeite_cabbage_with_insects";
		Resources res=view.getResources();
		tag.image.setBackgroundResource(res.getIdentifier(resId,"drawable",  "com.example.multicastsocketsend"));
		
		
		return view;
	}
	public class TagView{
		 
		ImageView image;
		TextView text;
		
		public TagView(ImageView image, TextView text){
			 
			this.image = image;
			this.text = text;
			
		}
	}
	 
	
}
