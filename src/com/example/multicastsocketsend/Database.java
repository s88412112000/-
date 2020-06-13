package com.example.multicastsocketsend;

 

 
import java.util.Vector;

import android.content.Context;
 
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
 



public class Database {

	String folderPath=Environment
	.getExternalStorageDirectory().getPath() + "/MuGuide";
	
    private SQLiteDatabase db;
    int latestVersion=17;
 

    public Database() {
      
    	db= SQLiteDatabase.openOrCreateDatabase(folderPath+"/Mus_Database.db", null);
    
    }
 
   
    public void close() {
        db.close();
    }
    
    public boolean needUpgrade(){
	    Cursor cursor=db.rawQuery("select version from about", null);
		
		if(cursor.moveToFirst()){
			int version=cursor.getInt(0);
			cursor.close();
			Log.d("Database","現在:"+String.valueOf(version)+"最新:"+String.valueOf(latestVersion));
			if(version==latestVersion)
			   return false;
			else 
				return true;
			}
		else
			return true;
    	 
    }
    
    //以下是資料庫的Query
    public int getLengthOfFile( String filePath){
    Cursor cursor=db.rawQuery("select length from files where filePath=\""+filePath+"\"", null);
		
		if(cursor.moveToFirst()){
			int length=cursor.getInt(0);
			cursor.close();
			return length;
			}
		else
			return 0;
    }
    
	public ArrayAdapter<String> getNameOfAllItem(Context context){
		 
		ArrayAdapter<String> nameAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
		 Cursor c=  db.rawQuery("SELECT name FROM item ORDER BY _id", null);
		while (c.moveToNext()) {
	          
	           nameAdapter.add(c.getString(0));
	        }
		c.close();
		     return nameAdapter;
	}
	public String[] getNameOfAllItem(){
		
 
		Cursor count=db.rawQuery("SELECT COUNT(*) FROM item" , null);
		int length=0;
		if(count.moveToNext())
			length=count.getInt(0);
		 Log.w("資料庫", "length="+String.valueOf(length));
		count.close();
		String[] result= new String[length];
		 Cursor c=  db.rawQuery("SELECT name FROM item ORDER BY _id", null);
	    	
		 int i=0;
		 while (c.moveToNext()) {
			     result[i]=c.getString(0);
			     Log.w("資料庫", "i="+String.valueOf(i));
		           i++;
		        }
			c.close();
			return result;
			  
		 
		
	}
	public Vector<String> getNameOfALLItemByRoomId(String roomID){
	
		Cursor c=  db.rawQuery("select item.name from item, room where item.roomID=room.roomID AND room.roomID=\""+roomID+"\"", null);
		Vector<String> names=new Vector<String>();
		
		while (c.moveToNext()) {
	           
	           names.add(c.getString(0));
	        }
		c.close();
		return names;
	}
	public Vector<String> getNameOfALLItemByNFC(long NFC){
		
		Cursor c=  db.rawQuery("select item.name from item, room where item.roomID=room.roomID AND room.NFC="+NFC, null);
		Vector<String> names=new Vector<String>();
		
		while (c.moveToNext()) {
	           
	           names.add(c.getString(0));
	        }
		c.close();
		return names;
	}
	
	public ArrayAdapter<String> getNameOfAllItemByRoomId(Context context, String roomID){
		ArrayAdapter<String> nameAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
		 Cursor c=  db.rawQuery("select item.name from item, room where item.roomID=room.roomID AND room.roomID=\""+roomID+"\"", null);
		while (c.moveToNext()) {
	          
	           nameAdapter.add(c.getString(0));
	        }
		c.close();
		     return nameAdapter;
	}
	public ArrayAdapter<String> getNameOfAllItemByNFC(Context context, long NFC){
		ArrayAdapter<String> nameAdapter=new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
		 Cursor c=  db.rawQuery("select item.name from item, room where item.roomID=room.roomID AND room.NFC="+NFC, null);
		while (c.moveToNext()) {
	          
	           nameAdapter.add(c.getString(0));
	        }
		c.close();
		     return nameAdapter;
	}
	
	//
	public String getIconOfItem(String name){
		Cursor cursor=db.rawQuery("select icon from item where name=\""+name+"\" or name_in_English=\""+name+"\"", null);
		if(cursor.moveToFirst()){
		String icon=cursor.getString(0);
		cursor.close();
		return icon;
		}
		else
			return null;
	}
	
	public Vector<String> getIconOfALLItemByRoomId(String roomID){
		
		Cursor c=  db.rawQuery("select item.icon from item, room where item.roomID=room.roomID AND room.roomID=\""+roomID+"\"", null);
		Vector<String> icons=new Vector<String>();
		
		while (c.moveToNext()) {
	           
			  icons.add(c.getString(0));
	        }
		c.close();
		return icons;
	}
	public Vector<String> getIconOfALLItemByNFC(long NFC){
		
		Cursor c=  db.rawQuery("select item.icon from item, room where item.roomID=room.roomID AND room.NFC="+NFC, null);
		Vector<String> icons=new Vector<String>();
		
		while (c.moveToNext()) {
	           
			icons.add(c.getString(0));
	        }
		c.close();
		return icons;
	}


	public String getItemName(int id){
		Cursor cursor=db.rawQuery("select name from item where _id="+String.valueOf(id), null);
		
		if(cursor.moveToFirst()){
			String name=cursor.getString(0);
			cursor.close();
			return name;
			}
		else
			return null;
	}
	
	public String getItemNameInEnglish(int id){
		Cursor cursor=db.rawQuery("select name_in_English from item where _id="+String.valueOf(id), null);
		if(cursor.moveToFirst()){
		String name_in_English=cursor.getString(0);
		cursor.close();
		return name_in_English;
		}
		else
			return null;
	}
	
	public int getItemSize(int id){
    	Cursor cursor=db.rawQuery("select size from item where _id="+id, null);
		if(cursor.moveToFirst()){
    	cursor.moveToFirst();
		int size=cursor.getInt(0);
		cursor.close();
		return size;
		}
		return -1;
    }
    public int getItemSize(String name){
    	Cursor cursor=db.rawQuery("select size from item where name=\""+name+"\" or name_in_English=\""+name+"\"", null);
		if(cursor.moveToFirst()){
		int size=cursor.getInt(0);
		cursor.close();
		return size;
		}
		else
			return -1;
    }
    public String getIntroductionOfItem(String name){
    	Cursor cursor=db.rawQuery("select introduction from item where name=\""+name+"\" or name_in_English=\""+name+"\"", null);
		if(cursor.moveToFirst()){
		String introduction=cursor.getString(0);
		cursor.close();
		return introduction;
		}
		else
			return null;
    }
    public String getNameOfRandomItem(){
    	Cursor cursor=db.rawQuery("SELECT COUNT(*) FROM item" , null);
    	String result=null;
    	
    	if (cursor.moveToNext()) {
            int count = cursor.getInt(0); 
            int r = (int) (Math.random() * count + 1);
          
             Cursor cursor2=db.rawQuery("SELECT name FROM item WHERE _id="+String.valueOf(r) , null);
             if( cursor2.moveToFirst())
               result=cursor2.getString(0);
             cursor2.close();
    	}
    	
       cursor.close();	
       return result;
    }
    public String getNameOfRandomItemInEnglish(){
    	Cursor cursor=db.rawQuery("SELECT COUNT(*) FROM item" , null);
    	String result=null;
    	
    	if (cursor.moveToNext()) {
            int count = cursor.getInt(0); 
            int r = (int) (Math.random() * count + 1);
          
             Cursor cursor2=db.rawQuery("SELECT name_in_English FROM item WHERE _id="+String.valueOf(r) , null);
             if( cursor2.moveToFirst())
               result=cursor2.getString(0);
             cursor2.close();
    	}
    	
       cursor.close();	
       return result;
    }
    
    public String getPictureOfItem(String NameInChinese){
        Cursor cursor=db.rawQuery("select picture from item where name=\""+NameInChinese+"\"", null);
     	 
        
        
    	 if(cursor.moveToFirst()){
    			String picture= cursor.getString(0);
    		 
    			cursor.close();
    			return picture;
    			}
    	 else {
			return null;
		}
    }
    
    public String getPictureOfRoom(String roomID){
    	 Cursor cursor=db.rawQuery("select picture from room where roomID=\""+roomID+"\"", null);
     	 
    	 if(cursor.moveToFirst()){
    	    	 
    			String picture= cursor.getString(0);
    			cursor.close();
    			return picture;
    			}
    	 else {
			return null;
		}
    }
    public String getPictureOfRoom(long NFC){
   	 Cursor cursor=db.rawQuery("select picture from room where NFC="+NFC, null);
    	 
   	 if(cursor.moveToFirst()){
   	    	 
   			String picture= cursor.getString(0);
   			cursor.close();
   			return picture;
   			}
   	 else {
			return null;
		}
   }
    public String getRoomName(String roomID){
    	 Cursor cursor=db.rawQuery("select name from room where roomID=\""+roomID+"\"", null);
	     	 
    	 if(cursor.moveToFirst()){
    	    	 
    			String name=cursor.getString(0);
    			cursor.close();
    			return name;
    			}
    	 else {
			return null;
		}
    }
    public String getRoomName(long nfc){
    	
    	 Cursor cursor=db.rawQuery("select name from room where NFC="+nfc, null);
     	 
    	 if(cursor.moveToFirst()){
    			String name=cursor.getString(0);
    			cursor.close();
    			return name;
    			}
    	 else {
			return null;
		}
    }
    public boolean isRoomExist(String roomID){
       Cursor cursor=db.rawQuery("select * from room where roomID=\""+roomID+"\"", null);
		 
	if(cursor.moveToFirst()){
		cursor.close();
		return true;
	}
	else
	{
		cursor.close();
		return false;
	}
		
    }
    public boolean isRoomExist(long nfcID){
    	 Cursor cursor=db.rawQuery("select * from room where NFC="+nfcID, null);
		 Log.d("NFCID",String.valueOf(nfcID));
    	 
    		if(cursor.moveToFirst()){
    			cursor.close();
    			return true;
    		}
    		else
    		{
    			cursor.close();
    			return false;
    		}
    }
    public String translateNameIntoEnglish(String chinese){
    	Cursor cursor=db.rawQuery("select name_in_English from item where name=\""+chinese+"\"", null);
		if(cursor.moveToFirst()){
    	cursor.moveToFirst();
		String english=cursor.getString(0);
		cursor.close();
		return english;
		}
		return null;
    }
}
