
package anipr.stampitgo.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.common.activities.SampleActivityBase;
import com.facebook.Settings;
import com.facebook.widget.LikeView;

public class AboutUs extends SampleActivityBase {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_us);
		Settings.sdkInitialize(this);
		
		
		LikeView likeView = (LikeView) findViewById(R.id.like_view);
		likeView.setObjectId("https://www.facebook.com/stampitgo");
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		LikeView.handleOnActivityResult(this, requestCode, resultCode, intent);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem aboutus_item = (MenuItem) menu.findItem(R.id.action_aboutUs);
		aboutus_item.setVisible(false);
		return true;
	}
	
}
