package anipr.stampitgo.android;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class StampsAdapter extends ArrayAdapter<StampCard>
{	

	private Activity activity;
    int position;
    StampCard currentStamp;
	private List<StampCard> listItems;

	public StampsAdapter( Activity activity,List<StampCard> stampcards) {
		super (activity,R.layout.stamp_card,stampcards);
		this.activity = activity;
		listItems = stampcards;
	}
    
	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public StampCard getItem(int arg0) {
		return listItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	
	
	@Override
	public View getView(int arg0, View convertView, ViewGroup arg2) {
			
			ViewHolder holder;
			if(convertView == null){
			convertView = LayoutInflater.from(activity).inflate(R.layout.stamp_card, null);
			holder = new ViewHolder();
			
			holder.restaurant  = (TextView)convertView.findViewById(R.id.storeName);
			
			holder.address = (TextView)convertView.findViewById(R.id.address);
			holder.category = (TextView)convertView.findViewById(R.id.category);
			holder.reward_details = (TextView)convertView.findViewById(R.id.reward_message);
			holder.user_stamps = (TextView)convertView.findViewById(R.id.user_stamps);
			holder.restaurantCover = (ImageView)convertView.findViewById(R.id.restaurantCover);
			holder.restaurantLogo = (ImageView) convertView.findViewById(R.id.logo);
			convertView.setTag(holder);
			
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
     		if(listItems != null)
			{
     		currentStamp = listItems.get(arg0);
			position = arg0;
			holder.restaurant.setText(currentStamp.restaurant_name);
			holder.address.setText(currentStamp.address);
			holder.category.setText("#"+currentStamp.category);
			holder.reward_details.setText(currentStamp.reward_details);
			holder.user_stamps.setText(""+currentStamp.user_stamps);
			//holder.reward_stamps.setText(""+currentStamp.reward_stamps);
			File coverFile=new File(activity.getApplicationInfo().dataDir+"/app_"+AppController.COVER_DIR , currentStamp.code);
			if(coverFile.exists())
			{
			Picasso.with(activity).load(coverFile).into(holder.restaurantCover);
			}
			else{
				holder.restaurantCover.setImageDrawable(convertView.getResources().getDrawable(R.drawable.background_card));
				DbHelper dhelper = DbHelper.getInstance(activity.getApplicationContext());
				SQLiteDatabase db = dhelper.getReadableDatabase();
				String query = "select * from "+DbHelper.RESTAURANT_TABLE +
						" where "+DbHelper.STORECODE+" = ? ;";
				Cursor c = db.rawQuery(query,new String[]{currentStamp.code});
				 if(c.moveToFirst())
				 {
				Log.e("image downlaod", "Downloading image"+currentStamp.restaurant_name);
				new DownloadImagesVolley(activity,
											c.getString(c.getColumnIndex(DbHelper.COVER_URL)), 
											currentStamp.code, AppController.COVER_DIR).makeRequest();
				 }
				 c.close();
			}
			File logoFile=new File(activity.getApplicationInfo().dataDir+"/app_"+AppController.LOGO_DIR , currentStamp.code);
			if(logoFile.exists())
			{
			Picasso.with(activity).load(logoFile).into(holder.restaurantLogo);
			}
			else{
				holder.restaurantLogo.setImageDrawable(convertView.getResources().getDrawable(R.drawable.background_card));
				DbHelper dhelper = DbHelper.getInstance(activity.getApplicationContext());
				SQLiteDatabase db = dhelper.getReadableDatabase();
				String query = "select "+DbHelper.LOGO_URL+" from "+DbHelper.RESTAURANT_TABLE +
						" where "+DbHelper.STORECODE+" = ? ;";
				Cursor c = db.rawQuery(query, new String[]{currentStamp.code});
				if(c.moveToFirst())
				 {
				Log.e("image downlaod", "Downloading image"+currentStamp.restaurant_name);	
				new DownloadImagesVolley(activity,
											c.getString(c.getColumnIndex(DbHelper.LOGO_URL)), 
											currentStamp.code, AppController.LOGO_DIR).makeRequest();
				
				 }
				 c.close();
			}
			
		}
			
			return convertView;
			
	}
	
	public void refresh(List<StampCard> stampcards){
		 listItems.clear();
		 listItems.addAll(stampcards);
		 notifyDataSetChanged();
	}
	private static class ViewHolder{
		public TextView category;
		TextView restaurant;
		TextView address;
		TextView reward_details;
		TextView user_stamps;
		ImageView restaurantCover;
		ImageView restaurantLogo;
		}
	
     
}