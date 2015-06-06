package com.minlite.usbcardreadersample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    ProgressDialog mProgressDialog;

    USBCardReader mCardReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hook
        mCardReader = new USBCardReader((EditText) findViewById(R.id.swipeField), new USBCardReader.USBSwipeListener() {
            @Override
            public void onProcessingSwipe() {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setMessage("Processing");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(true);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if(mCardReader != null) mCardReader.unHook();
                    }
                });
                //mProgressDialog.show();
            }

            @Override
            public void onSwipe(String trackData) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Parsed: " + trackData, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMsg) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Timeout! Try again?")
                        .setMessage("Unable to read the swiped card. Do you want to try again?")
                        .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Hook again
                                mCardReader.hook();
                            }
                        })
                        .setNegativeButton("Enter manually", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // TODO: Enter manually
                            }
                        })
                        .show();
            }
        });
        mCardReader.hook();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
