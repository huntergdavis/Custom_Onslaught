package com.hunterdavis.customonslaught;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class CustomOnslaught extends Activity {

	Panel mypanel = null;
	InventorySQLHelper scoreData = new InventorySQLHelper(this);
	ArrayAdapter<String> m_adapterForHighScores;
	int currentScore = 0;
	String lastHighScoreName = "";
	int SELECT_PICTURE = 22;
	int SELECT_SHIP = 23;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mypanel = (Panel) findViewById(R.id.SurfaceView01);
		m_adapterForHighScores = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		mypanel.setScoreData(scoreData);

		// Create an anonymous implementation of OnClickListener
		OnClickListener resetButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				// Boolean didWeSave = saveImage(v.getContext());
				mypanel.reset();
			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener pauseButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				// Boolean didWeSave = saveImage(v.getContext());
				if (mypanel.gamePaused == true) {
					Toast.makeText(v.getContext(), "Game is ALREADY paused.",
							Toast.LENGTH_SHORT).show();
				}

				mypanel.gamePaused = true;

			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener highscoresListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				m_adapterForHighScores.clear();

				Cursor cursor = getScoresCursor();
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						String highscore = cursor.getString(2) + " - "
								+ cursor.getInt(1);
						m_adapterForHighScores.add(highscore);
					}
				} else {
					m_adapterForHighScores.add("Hunter - 100");
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(
						v.getContext());
				builder.setTitle("High Scores");
				builder.setAdapter(m_adapterForHighScores,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								// Do something with the selection
								dialog.dismiss();
							}
						});

				AlertDialog alert = builder.create();
				mypanel.gamePaused = true;
				alert.show();

			}
		};

		Button highscoresButton = (Button) findViewById(R.id.scoresButton);
		highscoresButton.setOnClickListener(highscoresListner);

		Button pauseButton = (Button) findViewById(R.id.pauseButton);
		pauseButton.setOnClickListener(pauseButtonListner);

		// Create an anonymous implementation of OnClickListener
		OnClickListener ballButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				// Boolean didWeSave = saveImage(v.getContext());

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Source Photo"),
						SELECT_PICTURE);

			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener shipButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				// Boolean didWeSave = saveImage(v.getContext());

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Source Photo"),
						SELECT_SHIP);

			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener nameButtonListner = new OnClickListener() {
			public void onClick(View v) {
				mypanel.gamePaused = true;
				// do something when the button is clicked
				// Boolean didWeSave = saveImage(v.getContext());
				// do something when the button is clicked
				// Boolean didWeSave = saveImage(v.getContext());
				// TODO GET NAME AND SAVE TO HIGH SCORES
				AlertDialog.Builder alert = new AlertDialog.Builder(
						v.getContext());

				alert.setTitle("Your Name?");
				alert.setMessage("Please Enter Your Name For the High Score List");

				// Set an EditText view to get user input
				final EditText input = new EditText(v.getContext());
				input.setText(lastHighScoreName);
				alert.setView(input);

				alert.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String tempName = input.getText().toString()
										.trim();

								if (tempName.length() < 1) {
									tempName = "Unnamed Player";
								}
								lastHighScoreName = tempName;
								mypanel.changeName(lastHighScoreName);
								mypanel.reset();
							}

						});

				alert.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Canceled.
								mypanel.reset();
							}
						});

				alert.show();

			}

		};

		Button namebutton = (Button) findViewById(R.id.nameButton);
		namebutton.setOnClickListener(nameButtonListner);

		OnCheckedChangeListener mycheckboxListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				mypanel.setShootReverse(isChecked);
				String firemode = "direct fire";
				if (isChecked) {
					firemode = "reverse fire";
				}
				Toast.makeText(buttonView.getContext(),
						"Switched to " + firemode + " mode", Toast.LENGTH_SHORT)
						.show();

			}

		};

		Button resetButton = (Button) findViewById(R.id.resetButton);
		resetButton.setOnClickListener(resetButtonListner);

		CheckBox mycheckbox = (CheckBox) findViewById(R.id.checkBox1);
		mycheckbox.setOnCheckedChangeListener(mycheckboxListener);

		Button loadImageButton = (Button) findViewById(R.id.loadButton);
		loadImageButton.setOnClickListener(ballButtonListner);

		Button loadShipButton = (Button) findViewById(R.id.shipButton);
		loadShipButton.setOnClickListener(shipButtonListner);

		Cursor cursor = getNamesCursor();
		if (cursor.getCount() > 0) {
			cursor.moveToNext();
			String playerName = cursor.getString(2);
			int spaceLocation = playerName.indexOf(" ");
			if (spaceLocation > 1) {
				String playerLoc = playerName.substring(0, spaceLocation);
				if (playerLoc != null) {
					mypanel.changeName(playerLoc.trim());
				} else {
					mypanel.changeName("Player 1");
				}
			} else {
				mypanel.changeName("Player 1");
			}
		}else {
			mypanel.changeName("Player 1");
		}

		// Toast.makeText(getBaseContext(),
		// "Draw a Line Around Everything Without Touching",
		// Toast.LENGTH_LONG).show();

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	} // end of oncreate

	protected void onPause() {
		super.onPause();
		mypanel.terminateThread();
		System.gc();
	}

	protected void onResume() {
		super.onResume();
		if (mypanel.surfaceCreated == true) {
			mypanel.createThread(mypanel.getHolder());
		}
	}

	// this is called when the screen rotates.
	// (onCreate is no longer called when screen rotates due to manifest, see:
	// android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// setContentView(R.layout.main);

		// InitializeUI();
	}

	private Cursor getScoresCursor() {
		SQLiteDatabase db = scoreData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, InventorySQLHelper.SCORES + " desc");
		startManagingCursor(cursor);
		return cursor;
	}

	private Cursor getNamesCursor() {
		SQLiteDatabase db = scoreData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, "_id desc");
		startManagingCursor(cursor);
		return cursor;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				mypanel.setUri(selectedImageUri);
			}
			if (requestCode == SELECT_SHIP) {
				Uri selectedImageUri = data.getData();
				mypanel.setPlayerUri(selectedImageUri);
			}
		}
	}

}