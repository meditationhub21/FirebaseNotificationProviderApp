package com.example.firebasenotificationprovider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasenotificationprovider.Model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PushNotification";
    private static final String CHANNEL_ID = "101";
    public static MainActivity mainActivity;
    public static Boolean isVisible=false;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST=9000;
    String resultString = null;
    String regID = null;
    String storedToken = null;
    private String name;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity=this;
        createNotificationChannel();
        db=FirebaseFirestore.getInstance();
        TextView messageTextView = (TextView) findViewById(R.id.getText);
        Button btn=(Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name=messageTextView.getText().toString();
                Log.d("User entered: ",name);
                getToken(name,db);
                //saveDatatoDatabase(name,tempToken,db);
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "firebaseNotifChannel";
            String description = "Recieve Firebase Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void getToken(String userTag,FirebaseFirestore db){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(!task.isSuccessful()){
                    Log.d(TAG,"onComplete: Failed to get the Token");
                }
                //Token
                String token=task.getResult();
                saveDatatoDatabase(userTag,token,db);
                Log.d(TAG,"onComplete:"+token);
            }
        });
    }

    private void saveDatatoDatabase(String userTag, String token,FirebaseFirestore db) {
//        DocumentReference dbUserDetails = db.collection("user_details").document(userTag);

        UserDetails userDetails1=new UserDetails(token);

        db.collection("user_details").document(userTag).set(userDetails1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        if (MainActivity.isVisible) {
                            MainActivity.mainActivity.ToastNotify("Hello "+userTag);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastNotify(e.getMessage());
                    }
                });
    }

    public void ToastNotify(final String notificationMessage){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
                // TextView helloText=(TextView) findViewById(R.id.text_hello);
                // helloText.setText(notificationMessage);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        isVisible=true;
    }
    @Override
    protected void onPause(){
        super.onPause();
        isVisible=false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        isVisible=true;
    }
    @Override
    protected void onStop(){
        super.onStop();
        isVisible=false;
    }

    public String getTokenTemp(){
        Task<String> tempToken=FirebaseMessaging.getInstance().getToken();
        String str=tempToken.getResult();
        Log.d(TAG,str);
        boolean ans=tempToken.isComplete();
        System.out.println(ans);
        return str;
    }

}