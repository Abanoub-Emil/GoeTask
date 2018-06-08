package com.champion.bero.goetask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FaceBookActivity extends AppCompatActivity {

    @BindView(R.id.login_button) LoginButton loginButton;
    CallbackManager callbackManager;
    ProgressDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_face_book);

        ButterKnife.bind(this);

        //facebook api
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));


        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mdialog = new ProgressDialog(FaceBookActivity.this);
                mdialog.setMessage("retrieve data");
                mdialog.show();
                // get user profile data
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken()
                        , new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                getfacebookdata(object , getApplicationContext());
                                mdialog.dismiss();
                            }
                        });
                //graph api
                Bundle parameter = new Bundle();
                parameter.putString("fields", "id,name");
                request.setParameters(parameter);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // check for network access
        boolean states = isNetworkAvailable();
        if (states == true) {
            // internet is avilable
        } else {
            // no internet connection
            Toast.makeText(this, "Enable Internet Please", Toast.LENGTH_LONG).show();
        }
    }

    // check network states
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void getfacebookdata(JSONObject jsonObject , Context context) {
        try {

            String userName = jsonObject.getString("name");
//            String email = jsonObject.getString("email");
            // facebook cant give acess to user password so instead i will use id
            String id = jsonObject.getString("id");
            Bitmap bitmap = null;
            Intent intent = new Intent(FaceBookActivity.this, MapsActivity.class);
//            intent.putExtra("email", email);
            intent.putExtra("userName", userName);
            intent.putExtra("id", id);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
