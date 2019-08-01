package com.parse.starter;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class ChatActivity extends AppCompatActivity {
    String activeUser = "";

    boolean keyboardOn = true;

    ArrayList<String> messages = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    public void sendText(View view) {
        final EditText chatEditText = (EditText) findViewById(R.id.chatEditText);
        ParseObject message = new ParseObject("Message");

        final String messageContent = chatEditText.getText().toString();

        message.put("sender", ParseUser.getCurrentUser().getUsername());
        message.put("recipient", activeUser);
        message.put("message", messageContent);
        chatEditText.setText("");

        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    messages.add(messageContent);
                    arrayAdapter.notifyDataSetChanged();

                }
            }
        });
    }

    public void screenResize(View view) {
        ListView chatLists = (ListView) findViewById(R.id.chatListView);
        ViewGroup.LayoutParams params = chatLists.getLayoutParams();
        params.height = 650;
        chatLists.setLayoutParams(params);
        keyboardOn = true;
    }

    public void onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (keyboardOn == true) {
                ListView chatLists = (ListView) findViewById(R.id.chatListView);
                ViewGroup.LayoutParams params = chatLists.getLayoutParams();
                params.height = 1200;
                chatLists.setLayoutParams(params);
                keyboardOn = false;
            } else {
                super.dispatchKeyEvent(event);
            }
        }
    }

    public void repeat(){
        arrayAdapter.notifyDataSetChanged();
    }
//    final View activityRootView = findViewById(R.id.activityRoot);
//    activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//        @Override
//        public void onGlobalLayout() {
//            Rect r = new Rect();
//            //r will be populated with the coordinates of your view that area still visible.
//            activityRootView.getWindowVisibleDisplayFrame(r);
//
//            int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
//            if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
//        ... do something here
//            }
//        }
//    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Intent intent = getIntent();

        activeUser = intent.getStringExtra("username");
        setTitle("Chat with " + activeUser);
        ListView chatListView = (ListView) findViewById(R.id.chatListView);
        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        screenResize(chatListView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(arrayAdapter);
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Message");

        query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recipient", activeUser);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Message");

        query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("sender", activeUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.orderByAscending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        messages.clear();
                        for (ParseObject message : objects) {
                            String messageContent = message.getString("message");
                            if (!message.getString("sender").equals(ParseUser.getCurrentUser().getUsername())) {
                                messageContent = activeUser + ": " + messageContent;


                            } else {
                                messageContent = "You: " + messageContent;
                            }
                            messages.add(messageContent);

                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        messageTimer.scheduleAtFixedRate(task,1000,1000);
    }
    Timer messageTimer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
            public void run() {
                arrayAdapter.notifyDataSetChanged();
        }
    };
}

