package com.minlite.usbcardreadersample;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class USBCardReader {
    String currentInput = "";
    Boolean processing = false;
    long processingStartTime;
    View.OnKeyListener mKeyboardListener;

    EditText mEditText;
    USBSwipeListener mUsbSwipeListener;
    long mTimeout = 5000;

    ScheduledExecutorService timeOutWorker = Executors.newSingleThreadScheduledExecutor();

    public USBCardReader(EditText editText, USBSwipeListener listener) {
        this(editText, listener, 5000);
    }

    public USBCardReader(EditText editText, USBSwipeListener listener, long timeout) {
        mEditText = editText;
        mUsbSwipeListener = listener;
        mTimeout = timeout;
    }

    public void hook() {
        mKeyboardListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (!processing) {
                    processing = true;
                    processingStartTime = System.currentTimeMillis();

                    // Schedule the timeout handler
                    Runnable task = new Runnable() {
                        public void run() {
                            currentInput = "";
                            processing = false;
                            processingStartTime = 0;
                            mEditText.setOnKeyListener(null);
                            mUsbSwipeListener.onFailure("Timeout");
                        }
                    };
                    timeOutWorker.schedule(task, mTimeout / 1000, TimeUnit.SECONDS);


                    mUsbSwipeListener.onProcessingSwipe();
                }

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() != KeyEvent.KEYCODE_SHIFT_LEFT && keyEvent.getKeyCode() != KeyEvent.KEYCODE_SHIFT_RIGHT) {
                    currentInput = currentInput + (char) keyEvent.getUnicodeChar();

                    // Check if the current value of the field is valid track data (either track 1 or 2)
                    if (Pattern.matches(".*^%B\\d{0,19}\\^[\\w\\s\\/]{2,26}\\^\\d{7}\\w*\\?.*", currentInput)) {
                        Pattern track1 = Pattern.compile("(^%B\\d{0,19}\\^[\\w\\s\\/]{2,26}\\^\\d{7}\\w*\\?)");
                        Matcher m = track1.matcher(currentInput);
                        if (m.find()) {
                            // Found
                            // Remove the listener
                            unHook();
                            mUsbSwipeListener.onSwipe(m.group(1));
                            return false;
                        }
                    } else if (Pattern.matches(".*;\\d{0,19}=\\d{7}\\w*\\?.*", currentInput)) {
                        Pattern track2 = Pattern.compile("(;\\d{0,19}=\\d{7}\\w*\\?)");
                        Matcher m = track2.matcher(currentInput);
                        if (m.find()) {
                            // Found
                            // Remove the listener
                            unHook();
                            mUsbSwipeListener.onSwipe(m.group(1));
                            return false;
                        }
                    } else {
                        // Not valid track data
                        if (System.currentTimeMillis() - processingStartTime > mTimeout) {
                            // 5 seconds has passed. Timeout
                            currentInput = "";
                            processing = false;
                            processingStartTime = 0;
                            mEditText.setOnKeyListener(null);
                            mUsbSwipeListener.onFailure("Timeout");
                        }
                    }
                }

                return false;
            }
        };

        mEditText.setOnKeyListener(mKeyboardListener);
    }

    public void unHook() {
        mEditText.setOnKeyListener(null);
        timeOutWorker.shutdownNow();
    }


    interface USBSwipeListener {
        public void onProcessingSwipe();
        public void onSwipe(String trackData);
        public void onFailure(String errorMsg);
    }
}
