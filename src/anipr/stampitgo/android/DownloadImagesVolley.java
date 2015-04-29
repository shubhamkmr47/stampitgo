package anipr.stampitgo.android;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

public class DownloadImagesVolley {

	Context mContext;
	String imgUrl, fileName, dir;

	public DownloadImagesVolley(Context context, String imgUrl,
			String fileName, String dir) {
		this.mContext = context;
		this.fileName = fileName;
		this.dir = dir;
		this.imgUrl = imgUrl;
	}

	public void makeRequest() {
		ImageRequest imgRequest = new ImageRequest(imgUrl,
				new Response.Listener<Bitmap>() {

					@Override
					public void onResponse(Bitmap response) {
						saveToInternalStorage(response, fileName, mContext, dir);
						response.recycle();
					}
				}, 0, 0, null, null);
		AppController.getInstance().addToRequestQueue(imgRequest);
	}

	private void saveToInternalStorage(Bitmap bitmapImage, String imageName,
			Context context, String dir) {
		ContextWrapper cw = new ContextWrapper(context);
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir(dir, Context.MODE_PRIVATE);
		// Create imageDir
		File mypath = new File(directory, imageName);

		FileOutputStream fos = null;
		try {

			fos = new FileOutputStream(mypath);

			// Use the compress method on the BitMap object to write image to
			// the OutputStream
			bitmapImage.compress(Bitmap.CompressFormat.PNG, 30, fos);
			Log.e("Stored in "+dir, "btmap image : "+imageName);
			fos.close();
			bitmapImage.recycle();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
