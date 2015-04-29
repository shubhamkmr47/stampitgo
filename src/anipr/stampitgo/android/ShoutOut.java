package anipr.stampitgo.android;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.common.activities.SampleActivityBase;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class ShoutOut extends SampleActivityBase {

	private ImageView badSmiley, flatfaceSmiley, happySmiley, delightedSmiley;
	private String rating;
	private String storecode;
	final String DEFAULT = "DEFAULT";
	private EditText feedbackComment;
	private SweetAlertDialog sDialog;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shoutout);

		storecode = getIntent().getStringExtra(DbHelper.STORECODE);
		rating = DEFAULT;
		delightedSmiley = (ImageView) findViewById(R.id.delightedsmiley);
		delightedSmiley.setOnClickListener(smileyClickListener);
		happySmiley = (ImageView) findViewById(R.id.happysmiley);
		happySmiley.setOnClickListener(smileyClickListener);
		flatfaceSmiley = (ImageView) findViewById(R.id.flatfacesmiley);
		flatfaceSmiley.setOnClickListener(smileyClickListener);
		badSmiley = (ImageView) findViewById(R.id.badsmiley);
		badSmiley.setOnClickListener(smileyClickListener);
		Button shoutoutSend = (Button) findViewById(R.id.sendfeedbackButton);
		shoutoutSend.setOnClickListener(shoutOutListener);
		feedbackComment = (EditText) findViewById(R.id.feedbackComment);

	}

	OnClickListener smileyClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.delightedsmiley:
				if (rating == "4") {
					rating = DEFAULT;
					delightedSmiley.setImageResource(R.drawable.delighted);
				} else {
					delightedSmiley
							.setImageResource(R.drawable.delighted_pressed);
					happySmiley.setImageResource(R.drawable.happy);
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					badSmiley.setImageResource(R.drawable.bad);
					rating = "4";
				}
				break;

			case R.id.happysmiley:
				if (rating == "3") {
					happySmiley.setImageResource(R.drawable.happy);
					rating = DEFAULT;
				}

				else {
					delightedSmiley.setImageResource(R.drawable.delighted);
					happySmiley.setImageResource(R.drawable.happy_pressed);
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					badSmiley.setImageResource(R.drawable.bad);
					rating = "3";
				}
				break;

			case R.id.flatfacesmiley:
				if (rating == "2") {
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					rating = DEFAULT;
				} else {
					delightedSmiley.setImageResource(R.drawable.delighted);
					happySmiley.setImageResource(R.drawable.happy);
					flatfaceSmiley
							.setImageResource(R.drawable.flatface_pressed);
					badSmiley.setImageResource(R.drawable.bad);
					rating = "2";
				}
				break;

			case R.id.badsmiley:
				if (rating == "1") {
					badSmiley.setImageResource(R.drawable.bad);
					rating = DEFAULT;
				} else {
					delightedSmiley.setImageResource(R.drawable.delighted);
					happySmiley.setImageResource(R.drawable.happy);
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					badSmiley.setImageResource(R.drawable.bad_pressed);
					rating = "1";
				}
				break;

			default:
				delightedSmiley.setImageResource(R.drawable.delighted);
				happySmiley.setImageResource(R.drawable.happy);
				flatfaceSmiley.setImageResource(R.drawable.flatface);
				badSmiley.setImageResource(R.drawable.bad);
				rating = "DEFAULT";
			}

		}
	};

	OnClickListener shoutOutListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!rating.equals("DEFAULT")) {
				String FeedbackJson;
				sDialog = new SweetAlertDialog(ShoutOut.this, SweetAlertDialog.PROGRESS_TYPE);
				sDialog.getProgressHelper().setBarColor(
						getResources().getColor(R.color.primary_color));
				sDialog.setTitleText("Please wait ");
				sDialog.setCancelable(false);
				sDialog.show();

				if (storecode.equals(getString(R.string.app_name))) {

					FeedbackJson = "{\"feedback\": {\"rating\": \"" + rating
							+ "\",\"comment\": \""
							+ feedbackComment.getText().toString() + " \"}}";

				} else {

					FeedbackJson = "{\"storeCode\": \"" + storecode
							+ "\",\"feedback\": {\"rating\": \"" + rating
							+ "\",\"comment\": \""
							+ feedbackComment.getText().toString() + " \"}}";

				}
				CustomParamRequest feedbackReq = new CustomParamRequest(
						Method.POST, ComUtility.ConnectionString + "/feedback",
						AppController.cookie, null, FeedbackJson,
						new Response.Listener<String>() {

							@Override
							public void onResponse(String arg0) {
								Log.d("FeedBack", arg0);
//								
								sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
								

								sDialog.setTitleText("Done!");
										sDialog.setContentText(
												getString(R.string.shout_out_response));
										sDialog.setConfirmText("Done");
										sDialog.setCancelable(true);
										sDialog.setConfirmClickListener(
												new OnSweetClickListener() {

													@Override
													public void onClick(
															SweetAlertDialog sweetAlertDialog) {
														finish();
													}
												});
								// Snackbar.with(ShoutOut.this.getApplicationContext())
								// .type(SnackbarType.MULTI_LINE)
								// .text(getString(R.string.shout_out_response))
								// .actionLabel("Okay")
								// .actionColor(
								// ShoutOut.this.getResources().getColor(
								// R.color.sb__button_text_color_yellow))
								// .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
								// .show(ShoutOut.this);

							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError arg0) {
								if(sDialog.isShowing()){
									sDialog.dismiss();
								}
								if (arg0 instanceof NoConnectionError) {
									Snackbar.with(
											ShoutOut.this
													.getApplicationContext())
											.type(SnackbarType.MULTI_LINE)
											.text(getString(R.string.internet_error_short))
											.actionLabel("Okay")
											.actionColor(
													ShoutOut.this
															.getResources()
															.getColor(
																	R.color.sb__button_text_color_yellow))
											.duration(
													Snackbar.SnackbarDuration.LENGTH_SHORT)
											.show(ShoutOut.this);
								} else {
									Snackbar.with(
											ShoutOut.this
													.getApplicationContext())
											.type(SnackbarType.MULTI_LINE)
											.text(getString(R.string.server_error))
											.actionLabel("Okay")
											.actionColor(
													ShoutOut.this
															.getResources()
															.getColor(
																	R.color.sb__button_text_color_yellow))
											.duration(
													Snackbar.SnackbarDuration.LENGTH_SHORT)
											.show(ShoutOut.this);
								}
							}
						});

				AppController.getInstance().addToRequestQueue(feedbackReq);
			} else {

				Snackbar.with(ShoutOut.this.getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text("Please select atleast one Smiley")
						.actionLabel("Okay")
						.actionColor(
								ShoutOut.this.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(ShoutOut.this);

			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem shoutout_item = (MenuItem) menu.findItem(R.id.action_feedback);
		shoutout_item.setVisible(false);
		return true;
	}
}
