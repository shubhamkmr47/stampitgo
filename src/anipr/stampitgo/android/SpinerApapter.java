package anipr.stampitgo.android;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SpinerApapter extends ArrayAdapter<String> 
{ 
	Activity mActivity;	
 public SpinerApapter(Activity activity, int txtViewResourceId,String[] categories) 
 {
  super(activity, txtViewResourceId,categories);
  this.mActivity = activity;
  }
 @Override
 public View getDropDownView(int position, View convertView, ViewGroup parent)
 	{
	 return getCustomView(position, convertView, parent); 
	 }
 @Override 
 public View getView(int pos, View convertView, ViewGroup prnt) 
 	{
	 	return getCusView(pos, convertView, prnt);
 	} 
 public View getCustomView(int position, View convertView, ViewGroup parent) 
 { 
	 if(convertView == null)
	 {	 
	 LayoutInflater inflater = mActivity.getLayoutInflater(); 
	 convertView = inflater.inflate(R.layout.filter_spinner_item, parent, false);
	 }
  TextView subSpinner = (TextView) convertView .findViewById(R.id.category_value); 
  subSpinner.setText(AppController.CATEGORIES[position]); 
  ImageView left_icon = (ImageView) convertView .findViewById(R.id.category_logo); 
  left_icon.setImageResource(AppController.CATEGORY_ICONS[position]);
  return convertView;
  } 
 public View getCusView(int position, View convertView, ViewGroup parent) {
	 if(convertView == null)
	 {	 
	 LayoutInflater inflater = mActivity.getLayoutInflater(); 
	 convertView = inflater.inflate(R.layout.filter_spinner_item, parent, false);
	 }
	 TextView subSpinner = (TextView) convertView.findViewById(R.id.category_value); 
	 ImageView left_icon = (ImageView) convertView.findViewById(R.id.category_logo); 
	 if(position!=0)
	 {
	 subSpinner.setText("Showing "+AppController.CATEGORIES[position]);
	 }else
	 {
	  subSpinner.setText(R.string.default_value);
	 }
	 left_icon.setImageResource(AppController.CATEGORY_ICONS[position]);
	 return convertView;
	}
}
