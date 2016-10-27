/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.koboc.collect.android.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.koboc.collect.android.R;
import org.koboc.collect.android.application.Collect;
import org.koboc.collect.android.preferences.AdminPreferencesActivity;
import org.koboc.collect.android.preferences.PreferencesActivity;
import org.koboc.collect.android.provider.InstanceProviderAPI;
import org.koboc.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.koboc.collect.android.utilities.CompatibilityUtils;
import org.koboc.collect.android.logic.FormDetails;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.koboc.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.koboc.collect.android.tasks.DownloadFormsTask;
import org.koboc.collect.android.tasks.DownloadFormListTask;
import org.koboc.collect.android.listeners.FormListDownloaderListener;
import org.koboc.collect.android.listeners.DiskSyncListener;
import org.koboc.collect.android.listeners.FormDownloaderListener;
import org.koboc.collect.android.tasks.DiskSyncTask;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity implements DiskSyncListener, FormListDownloaderListener, FormDownloaderListener {
	private static final String t = "MainMenuActivity";

	private static final int PASSWORD_DIALOG = 1;

	// menu options
	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_ADMIN = Menu.FIRST + 1;

	// buttons
//	private Button mEnterDataButton;
//	private Button mManageFilesButton;
//	private Button mSendDataButton;
//	private Button mReviewDataButton;
//	private Button mGetFormsButton;
//
//	private View mReviewSpacer;
//	private View mGetFormsSpacer;

	private AlertDialog mAlertDialog;
	private SharedPreferences mAdminPreferences;

	private int mCompletedCount;
	private int mSavedCount;

	private Cursor mFinalizedCursor;
	private Cursor mSavedCursor;

	private IncomingHandler mHandler = new IncomingHandler(this);
	private MyContentObserver mContentObserver = new MyContentObserver();


	private HashMap<String, FormDetails> mFormNamesAndURLs = new HashMap<String, org.koboc.collect.android.logic.FormDetails>();

	private DownloadFormListTask mDownloadFormListTask;

	private static boolean EXIT = true;

	private ArrayList<HashMap<String, String>> mFormList = new ArrayList<HashMap<String,String>>();
	// private static boolean DO_NOT_EXIT = false;

	private DownloadFormsTask mDownloadFormsTask;
	private DiskSyncTask mDiskSyncTask;
	private ProgressDialog mProgressDialog;

	// private static boolean DO_NOT_EXIT = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/////////////////////dalit///////////////////////
		setContentView(R.layout.dalit_main_screen);

		SharedPreferences adminPreferences = getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
		SharedPreferences.Editor editor = adminPreferences.edit();
		editor.putBoolean(AdminPreferencesActivity.KEY_AUTOSEND_WIFI, true);
		editor.commit();
		editor = adminPreferences.edit();
		editor.putBoolean(AdminPreferencesActivity.KEY_AUTOSEND_NETWORK, true);
		editor.commit();


		Button button = (Button)findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!checkformexists()){
					copyFolder("forms","");
					File folder = new File(Environment.getExternalStorageDirectory() +"/odk/forms/"+
							File.separator + "ICT for Dalit Right-media");
					boolean success = true;
					if (!folder.exists()) {
						success = folder.mkdirs();
					}
					if (success) {
						try {
							AssetManager assetManager = getAssets();
							InputStream in = null;
							OutputStream out = null;
							// Do something on success
							in = assetManager.open("forms/ICT for Dalit Right-media/" + "itemsets.csv");

							out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/odk/forms/ICT for Dalit Right-media/" + "itemsets.csv");

							copyFile(in, out);
						}catch (Exception e){

						}

					} else {
						// Do something else on failure
					}

					if (mDiskSyncTask == null) {
						mProgressDialog = new ProgressDialog(MainMenuActivity.this);
						mProgressDialog.setTitle("processing disk for forms please wait");
						mProgressDialog.show();
						Log.i(t, "Starting new disk sync task");
						mDiskSyncTask = new DiskSyncTask();
						mDiskSyncTask.setDiskSyncListener(MainMenuActivity.this);
						mDiskSyncTask.execute((Void[]) null);
					}
				}
				callformactivity("ICT_for_Dalit_Right", MainMenuActivity.this);


//        finish();
			}
		});
		Button button2 = (Button)findViewById(R.id.button2);

		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.question_status_list_Activity.class);

				startActivity(start);
//        finish();
			}
		});
		Button button3 = (Button)findViewById(R.id.button3);

		button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.policeMainActivity.class);
				startActivity(start);
//				startActivity(start.putExtra("urladdress","http://ctpi.mpower-social.com:8003/html/thana_contact.html"));
//        finish();
			}
		});
		Button button4 = (Button)findViewById(R.id.button4);

		button4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);

				startActivity(start.putExtra("urladdress"," http://ctpi.mpower-social.com:8003/html/nearby_legal.html"));
//        finish();
			}
		});
		Button button5 = (Button)findViewById(R.id.button5);

		button5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);

				startActivity(start.putExtra("urladdress"," http://ctpi.mpower-social.com:8003/html/lawrelatedinformation.html"));
//        finish();
			}
		});
		Button button6 = (Button)findViewById(R.id.button6);

		button6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);

				startActivity(start.putExtra("urladdress","http://ctpi.mpower-social.com:8003/html/law.html"));
//        finish();
			}
		});
		Button button7 = (Button)findViewById(R.id.button7);

		button7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);

				startActivity(start.putExtra("urladdress","http://ctpi.mpower-social.com:8003/html/hrworker.html"));
//        finish();
			}
		});
		Button button8 = (Button)findViewById(R.id.button8);

		button8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);

				startActivity(start.putExtra("urladdress","http://ctpi.mpower-social.com:8003/html/nearby_legal.html"));
//        finish();
			}
		});
		Button button9 = (Button)findViewById(R.id.button9);

		button9.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent start = new Intent(MainMenuActivity.this, org.koboc.collect.android.activities.webView.class);

				startActivity(start.putExtra("urladdress","http://ctpi.mpower-social.com:8003/html/hrorg.html"));
//        finish();
			}
		});
		Button button10 = (Button)findViewById(R.id.button10);

		button10.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callformactivity("সচরাচর জিজ্ঞাস্য প্রশ্ন", MainMenuActivity.this);
//        finish();
			}
		});
		////////////////////dalit///////////////////////সচরাচর জিজ্ঞাস্য প্রশ্ন

		// must be at the beginning of any activity that can be called from an
		// external intent
		Log.i(t, "Starting up, creating directories");
		try {
			Collect.createODKDirs();
		} catch (RuntimeException e) {
			createErrorDialog(e.getMessage(), EXIT);
			return;
		}

//		setContentView(R.layout.main_menu);

//		{
//			// dynamically construct the "ODK Collect vA.B" string
//			TextView mainMenuMessageLabel = (TextView) findViewById(R.id.main_menu_header);
//			mainMenuMessageLabel.setText(Collect.getInstance()
//					.getVersionedAppName());
//		}

		setTitle(getString(R.string.app_name) + " > "
				+ getString(R.string.main_menu));

		File f = new File(Collect.ODK_ROOT + "/collect.settings");
		if (f.exists()) {
			boolean success = loadSharedPreferencesFromFile(f);
			if (success) {
				Toast.makeText(this,
						"Settings successfully loaded from file",
						Toast.LENGTH_LONG).show();
				f.delete();
			} else {
				Toast.makeText(
						this,
						"Sorry, settings file is corrupt and should be deleted or replaced",
						Toast.LENGTH_LONG).show();
			}
		}

//		mReviewSpacer = findViewById(R.id.review_spacer);
//		mGetFormsSpacer = findViewById(R.id.get_forms_spacer);
//
		mAdminPreferences = this.getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
//
//		// enter data button. expects a result.
//		mEnterDataButton = (Button) findViewById(R.id.enter_data);
//		mEnterDataButton.setText(getString(R.string.enter_data_button));
//		mEnterDataButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Collect.getInstance().getActivityLogger()
//						.logAction(this, "fillBlankForm", "click");
//				Intent i = new Intent(getApplicationContext(),
//						FormChooserList.class);
//				startActivity(i);
//			}
//		});
//
//		// review data button. expects a result.
//		mReviewDataButton = (Button) findViewById(R.id.review_data);
//		mReviewDataButton.setText(getString(R.string.review_data_button));
//		mReviewDataButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Collect.getInstance().getActivityLogger()
//						.logAction(this, "editSavedForm", "click");
//				Intent i = new Intent(getApplicationContext(),
//						InstanceChooserList.class);
//				startActivity(i);
//			}
//		});
//
//		// send data button. expects a result.
//		mSendDataButton = (Button) findViewById(R.id.send_data);
//		mSendDataButton.setText(getString(R.string.send_data_button));
//		mSendDataButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Collect.getInstance().getActivityLogger()
//						.logAction(this, "uploadForms", "click");
//				Intent i = new Intent(getApplicationContext(),
//						InstanceUploaderList.class);
//				startActivity(i);
//			}
//		});
//
//		// manage forms button. no result expected.
//		mGetFormsButton = (Button) findViewById(R.id.get_forms);
//		mGetFormsButton.setText(getString(R.string.get_forms));
//		mGetFormsButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Collect.getInstance().getActivityLogger()
//						.logAction(this, "downloadBlankForms", "click");
//				SharedPreferences sharedPreferences = PreferenceManager
//						.getDefaultSharedPreferences(MainMenuActivity.this);
//				String protocol = sharedPreferences.getString(
//						PreferencesActivity.KEY_PROTOCOL, getString(R.string.protocol_odk_default));
//				Intent i = null;
//				if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
//					i = new Intent(getApplicationContext(),
//							GoogleDriveActivity.class);
//				} else {
//					i = new Intent(getApplicationContext(),
//							FormDownloadList.class);
//				}
//				startActivity(i);
//
//			}
//		});
//
//		// manage forms button. no result expected.
//		mManageFilesButton = (Button) findViewById(R.id.manage_forms);
//		mManageFilesButton.setText(getString(R.string.manage_files));
//		mManageFilesButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Collect.getInstance().getActivityLogger()
//						.logAction(this, "deleteSavedForms", "click");
//				Intent i = new Intent(getApplicationContext(),
//						FileManagerTabs.class);
//				startActivity(i);
//			}
//		});

		// count for finalized instances
		String selection = InstanceColumns.STATUS + "=? or "
				+ InstanceColumns.STATUS + "=?";
		String selectionArgs[] = { InstanceProviderAPI.STATUS_COMPLETE,
				InstanceProviderAPI.STATUS_SUBMISSION_FAILED };

        try {
            mFinalizedCursor = managedQuery(InstanceColumns.CONTENT_URI, null,
                    selection, selectionArgs, null);
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

    if (mFinalizedCursor != null) {
      startManagingCursor(mFinalizedCursor);
    }
    mCompletedCount = mFinalizedCursor != null ? mFinalizedCursor.getCount() : 0;
        getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, mContentObserver);
//		mFinalizedCursor.registerContentObserver(mContentObserver);

		// count for finalized instances
		String selectionSaved = InstanceColumns.STATUS + "=?";
		String selectionArgsSaved[] = { InstanceProviderAPI.STATUS_INCOMPLETE };

        try {
            mSavedCursor = managedQuery(InstanceColumns.CONTENT_URI, null,
                    selectionSaved, selectionArgsSaved, null);
        } catch (Exception e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

    if (mSavedCursor != null) {
      startManagingCursor(mSavedCursor);
    }
    mSavedCount = mSavedCursor != null ? mSavedCursor.getCount() : 0;
		// don't need to set a content observer because it can't change in the
		// background

//		updateButtons();
		if(!checkformexists()){
			downloadFormList();
		}else{
			mDiskSyncTask = (DiskSyncTask) getLastNonConfigurationInstance();
			if (mDiskSyncTask == null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setTitle("processing disk for forms please wait");
				mProgressDialog.show();
				mProgressDialog.setCancelable(false);
				Log.i(t, "Starting new disk sync task");
				mDiskSyncTask = new DiskSyncTask();
				mDiskSyncTask.setDiskSyncListener(this);
				mDiskSyncTask.execute((Void[]) null);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

		boolean edit = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_EDIT_SAVED, true);
		if (!edit) {
//			mReviewDataButton.setVisibility(View.GONE);
//			mReviewSpacer.setVisibility(View.GONE);
		} else {
//			mReviewDataButton.setVisibility(View.VISIBLE);
//			mReviewSpacer.setVisibility(View.VISIBLE);
		}

		boolean send = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_SEND_FINALIZED, true);
		if (!send) {
//			mSendDataButton.setVisibility(View.GONE);
		} else {
//			mSendDataButton.setVisibility(View.VISIBLE);
		}

		boolean get_blank = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_GET_BLANK, true);
		if (!get_blank) {
//			mGetFormsButton.setVisibility(View.GONE);
//			mGetFormsSpacer.setVisibility(View.GONE);
		} else {
//			mGetFormsButton.setVisibility(View.VISIBLE);
//			mGetFormsSpacer.setVisibility(View.VISIBLE);
		}

		boolean delete_saved = sharedPreferences.getBoolean(
				AdminPreferencesActivity.KEY_DELETE_SAVED, true);
		if (!delete_saved) {
//			mManageFilesButton.setVisibility(View.GONE);
		} else {
//			mManageFilesButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.dismiss();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
	}

	@Override
	protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "onCreateOptionsMenu", "show");
		super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
    		menu.add(0, MENU_PREFERENCES, 0, R.string.general_preferences)
				.setIcon(R.drawable.ic_menu_preferences),
			MenuItem.SHOW_AS_ACTION_NEVER);
		CompatibilityUtils.setShowAsAction(
    		menu.add(0, MENU_ADMIN, 0, R.string.admin_preferences)
				.setIcon(R.drawable.ic_menu_login),
			MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			Collect.getInstance()
					.getActivityLogger()
					.logAction(this, "onOptionsItemSelected",
							"MENU_PREFERENCES");
			Intent ig = new Intent(this, PreferencesActivity.class);
			startActivity(ig);
			return true;
		case MENU_ADMIN:
			Collect.getInstance().getActivityLogger()
					.logAction(this, "onOptionsItemSelected", "MENU_ADMIN");
			String pw = mAdminPreferences.getString(
					AdminPreferencesActivity.KEY_ADMIN_PW, "");
			if ("".equalsIgnoreCase(pw)) {
				Intent i = new Intent(getApplicationContext(),
						AdminPreferencesActivity.class);
				startActivity(i);
			} else {
				showDialog(PASSWORD_DIALOG);
				Collect.getInstance().getActivityLogger()
						.logAction(this, "createAdminPasswordDialog", "show");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createErrorDialog(String errorMsg, final boolean shouldExit) {
		Collect.getInstance().getActivityLogger()
				.logAction(this, "createErrorDialog", "show");
		mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
		mAlertDialog.setMessage(errorMsg);
		DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON_POSITIVE:
					Collect.getInstance()
							.getActivityLogger()
							.logAction(this, "createErrorDialog",
									shouldExit ? "exitApplication" : "OK");
					if (shouldExit) {
						finish();
					}
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton(getString(R.string.ok), errorListener);
		mAlertDialog.show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PASSWORD_DIALOG:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final AlertDialog passwordDialog = builder.create();

			passwordDialog.setTitle(getString(R.string.enter_admin_password));
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			input.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			passwordDialog.setView(input, 20, 10, 20, 10);

			passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
					getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							String pw = mAdminPreferences.getString(
									AdminPreferencesActivity.KEY_ADMIN_PW, "");
							if (pw.compareTo(value) == 0) {
								Intent i = new Intent(getApplicationContext(),
										AdminPreferencesActivity.class);
								startActivity(i);
								input.setText("");
								passwordDialog.dismiss();
							} else {
								Toast.makeText(
										MainMenuActivity.this,
										getString(R.string.admin_password_incorrect),
										Toast.LENGTH_SHORT).show();
								Collect.getInstance()
										.getActivityLogger()
										.logAction(this, "adminPasswordDialog",
												"PASSWORD_INCORRECT");
							}
						}
					});

			passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
					getString(R.string.cancel),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Collect.getInstance()
									.getActivityLogger()
									.logAction(this, "adminPasswordDialog",
											"cancel");
							input.setText("");
							return;
						}
					});

			passwordDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			return passwordDialog;

		}
		return null;
	}

//	private void updateButtons() {
//    if (mFinalizedCursor != null && !mFinalizedCursor.isClosed()) {
//      mFinalizedCursor.requery();
//      mCompletedCount = mFinalizedCursor.getCount();
//      if (mCompletedCount > 0) {
//        mSendDataButton.setText(getString(R.string.send_data_button, mCompletedCount));
//      } else {
//        mSendDataButton.setText(getString(R.string.send_data));
//      }
//    } else {
//      mSendDataButton.setText(getString(R.string.send_data));
//      Log.w(t, "Cannot update \"Send Finalized\" button label since the database is closed. Perhaps the app is running in the background?");
//    }
//
//    if (mSavedCursor != null && !mSavedCursor.isClosed()) {
//      mSavedCursor.requery();
//      mSavedCount = mSavedCursor.getCount();
//      if (mSavedCount > 0) {
//        mReviewDataButton.setText(getString(R.string.review_data_button,
//                mSavedCount));
//      } else {
//        mReviewDataButton.setText(getString(R.string.review_data));
//      }
//    } else {
//      mReviewDataButton.setText(getString(R.string.review_data));
//      Log.w(t, "Cannot update \"Edit Form\" button label since the database is closed. Perhaps the app is running in the background?");
//    }
//  }

	/**
	 * notifies us that something changed
	 *
	 */
	private class MyContentObserver extends ContentObserver {

		public MyContentObserver() {
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			mHandler.sendEmptyMessage(0);
		}
	}

	/*
	 * Used to prevent memory leaks
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<MainMenuActivity> mTarget;

		IncomingHandler(MainMenuActivity target) {
			mTarget = new WeakReference<MainMenuActivity>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			MainMenuActivity target = mTarget.get();
			if (target != null) {
//				target.updateButtons();
			}
		}
	}

	private boolean loadSharedPreferencesFromFile(File src) {
		// this should probably be in a thread if it ever gets big
		boolean res = false;
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(src));
			Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(
					this).edit();
			prefEdit.clear();
			// first object is preferences
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : entries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					prefEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					prefEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					prefEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					prefEdit.putString(key, ((String) v));
			}
			prefEdit.commit();

			// second object is admin options
			Editor adminEdit = getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0).edit();
			adminEdit.clear();
			// first object is preferences
			Map<String, ?> adminEntries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : adminEntries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					adminEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					adminEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					adminEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					adminEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					adminEdit.putString(key, ((String) v));
			}
			adminEdit.commit();

			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}

	private boolean checkformexists() {
		// TODO Auto-generated method stub
		try {
			File file = new File(Environment.getExternalStorageDirectory() + "/odk/forms/ICT for Dalit Right.xml");
			File file2 = new File(Environment.getExternalStorageDirectory() + "/odk/forms/সচর চর জ জ ঞ স য প রশ ন.xml");


			Log.v("return bool", "" + (file.exists() && file2.exists()));

			return file.exists() && file2.exists();
		}catch (Exception e){
			return false;
		}
//		return false;
	}
	private void downloadFormList() {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

		if (ni == null || !ni.isConnected()) {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
		} else {
			mProgressDialog = new ProgressDialog(this);
			mFormNamesAndURLs = new HashMap<String, FormDetails>();
			if (mProgressDialog != null) {
				// This is needed because onPrepareDialog() is broken in 1.6.
				mProgressDialog.setMessage(getString(R.string.please_wait));
			}
			mProgressDialog.show();
			mProgressDialog.setCancelable(false);
//            showDialog(PROGRESS_DIALOG);

			if (mDownloadFormListTask != null &&
					mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
				return; // we are already doing the download!!!
			} else if (mDownloadFormListTask != null) {
				mDownloadFormListTask.setDownloaderListener(null);
				mDownloadFormListTask.cancel(true);
				mDownloadFormListTask = null;
			}

			mDownloadFormListTask = new DownloadFormListTask();
			mDownloadFormListTask.setDownloaderListener(MainMenuActivity.this);
			mDownloadFormListTask.execute();
		}
	}
	@Override
	public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
		// TODO Auto-generated method stub
//		 dismissDialog(PROGRESS_DIALOG);
		mDownloadFormListTask.setDownloaderListener(null);
		mDownloadFormListTask = null;

		if (result == null) {
			Log.e(t, "Formlist Downloading returned null.  That shouldn't happen");
			// Just displayes "error occured" to the user, but this should never happen.
//	            createAlertDialog(getString(R.string.load_remote_form_error),
//	                getString(R.string.error_occured), EXIT);
			return;
		}

		if (result.containsKey(DownloadFormListTask.DL_AUTH_REQUIRED)) {
			// need authorization
//	            showDialog(AUTH_DIALOG);
		} else if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
			// Download failed
			String dialogMessage =
					getString(R.string.list_failed_with_error,
							result.get(DownloadFormListTask.DL_ERROR_MSG).errorStr);
			String dialogTitle = getString(R.string.load_remote_form_error);
//	            createAlertDialog(dialogTitle, dialogMessage, DO_NOT_EXIT);
		} else {
			// Everything worked. Clear the list and add the results.
//	        	mProgressDialog.dismiss();
			mFormNamesAndURLs = result;


			mFormList.clear();

			ArrayList<String> ids = new ArrayList<String>(mFormNamesAndURLs.keySet());
			ArrayList<FormDetails> filesToDownload = new ArrayList<FormDetails>();

			for (int i = 0; i < result.size(); i++) {
				String formDetailsKey = ids.get(i);
				FormDetails details = mFormNamesAndURLs.get(formDetailsKey);
				HashMap<String, String> item = new HashMap<String, String>();
				item.put("FORMNAME", details.formName);
				item.put("FORMID_DISPLAY",
						((details.formVersion == null) ? "" : (getString(R.string.version) + " " + details.formVersion + " ")) +
								"ID: " + details.formID );
				item.put("FORMDETAIL_KEY", formDetailsKey);
				Log.v("formnames", ""+details.formName);
				filesToDownload.add(details);
				// Insert the new form in alphabetical order.
				if (mFormList.size() == 0) {
					mFormList.add(item);
				} else {
					int j;
					for (j = 0; j < mFormList.size(); j++) {
						HashMap<String, String> compareMe = mFormList.get(j);
						String name = compareMe.get("FORMNAME");
						if (name.compareTo(mFormNamesAndURLs.get(ids.get(i)).formName) > 0) {
							break;
						}
					}
					mFormList.add(j, item);
				}
			}
			mDownloadFormsTask = new DownloadFormsTask();
			mDownloadFormsTask.setDownloaderListener(this);
			mDownloadFormsTask.execute(filesToDownload);
//	            mFormListAdapter.notifyDataSetChanged();
		}

	}


	@Override
	public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
		// TODO Auto-generated method stub
		if (mDownloadFormsTask != null) {
			mDownloadFormsTask.setDownloaderListener(null);
		}

		if (mProgressDialog.isShowing()) {
			// should always be true here
			mProgressDialog.dismiss();
		}
		mDiskSyncTask = (DiskSyncTask) getLastNonConfigurationInstance();
		if (mDiskSyncTask == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setTitle("processing disk for forms please wait");
			mProgressDialog.show();
			Log.i(t, "Starting new disk sync task");
			mDiskSyncTask = new DiskSyncTask();
			mDiskSyncTask.setDiskSyncListener(this);
			mDiskSyncTask.execute((Void[]) null);
		}
	}

	@Override
	public void progressUpdate(String currentFile, int progress, int total) {
		// TODO Auto-generated method stub

	}

	@Override
	public void SyncComplete(String result) {
		// TODO Auto-generated method stub
		if(mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
		}
	}

	public void callformactivity(String formname,Context con){

			long idFormsTable = 3;
			String sortOrder = FormsColumns.DISPLAY_NAME + " ASC, " + FormsColumns.JR_VERSION + " DESC";
			Cursor c = ((Activity) con).managedQuery(FormsColumns.CONTENT_URI, null, null, null, sortOrder);
			c.moveToFirst();
			while (c.isAfterLast() == false) {
				for (int i = 0; i < c.getColumnNames().length; i++) {
					if (c.getColumnName(i).equalsIgnoreCase("displayName")) {
						if (c.getString(i).equalsIgnoreCase(formname)) {
							c.moveToLast();
						}
					}
					if (c.getColumnName(i).equalsIgnoreCase("_id")) {
						idFormsTable = Long.parseLong(c.getString(i));
					}
				}
				c.moveToNext();
			}
			Uri formUri = ContentUris.withAppendedId(FormsColumns.CONTENT_URI, idFormsTable);
			Collect.getInstance().getActivityLogger().logAction(this, "onListItemClick", formUri.toString());
			String action = ((Activity) con).getIntent().getAction();
			if (Intent.ACTION_PICK.equals(action)) {
				// caller is waiting on a picked form
				((Activity) con).setResult(Activity.RESULT_OK, new Intent().setData(formUri));
			} else {
				// caller wants to view/edit a form, so launch formentryactivity
				startActivity(new Intent(Intent.ACTION_EDIT, formUri));
			}
//				((Activity) con).finish();


	}
	private void copyFolder(String name,String destinationsubfolder) {
		// "Name" is the name of your folder!
		AssetManager assetManager = getAssets();
		String[] files = null;

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			// Checking file on assets subfolder
			try {
				files = assetManager.list(name);
			} catch (IOException e) {
				Log.e("ERROR", "Failed to get asset file list.", e);
			}
			// Analyzing all file on assets subfolder
			for(String filename : files) {
				InputStream in = null;
				OutputStream out = null;
				// First: checking if there is already a target folder
				File folder = new File(Environment.getExternalStorageDirectory() + "/odk/forms/" + name);
				boolean success = true;
				if (!folder.exists()) {
					success = folder.mkdir();
				}
				if (success) {
					// Moving all the files on external SD
					try {
						in = assetManager.open(name + "/" +filename);
						out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/odk/" +destinationsubfolder+ name + "/" + filename);
						Log.i("WEBVIEW", Environment.getExternalStorageDirectory() + "/odk/" +destinationsubfolder+ name + "/" + filename);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					} catch(IOException e) {
						Log.e("ERROR", "Failed to copy asset file: " + filename, e);
					} finally {
						// Edit 3 (after MMs comment)
						try {
							in.close();
							in = null;
							out.flush();
							out.close();
							out = null;
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				}
				else {
					// Do something else on failure
				}
			}
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			// is to know is we can neither read nor write
		}
	}

	// Method used by copyAssets() on purpose to copy a file.
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}



}
