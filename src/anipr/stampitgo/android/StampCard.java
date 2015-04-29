package anipr.stampitgo.android;


public class StampCard {
	public String restaurant_name;
	public String address;
	public String reward_details;
	public String category;
	public String user_stamps;
	public  String code;
	byte[] logo;
	public StampCard(String restaurant_name,String code,String address,String user_stamps,String category,String reward_details,byte[] logo)
	{
		this.restaurant_name = restaurant_name; 
		this.address = address;
		this.reward_details = reward_details;
		this.user_stamps = user_stamps;
		this.category = category;
		this.code = code;
		this.logo = logo;
	}
}
