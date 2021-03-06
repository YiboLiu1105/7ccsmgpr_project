package com.oasis.onebox;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.oasis.onebox.tool.RSA;
import com.oasis.onebox.tool.Test;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class WelcomeActivity extends AppCompatActivity {

    private String IP_ADDRESS = "169.254.146.86";
    private String url = "http://"+IP_ADDRESS+":8080/api/register";

    private static final String TAG = "WelcomeActivity";

    private EditText etUsername;
    private EditText etPassword;
    private Button btGo;
    private CardView cv;
    private FloatingActionButton fab;

    public String pubKey;

    public class MyCallback extends StringCallback
    {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            Log.e(TAG, "onError:"+e.getMessage());
        }

        @Override
        public void onResponse(String response, int id) {
            Log.e(TAG, "onResponse：complete");
//            switch (id)
//            {
//                case 100:
//                    Toast.makeText(WelcomeActivity.this, "http", Toast.LENGTH_SHORT).show();
//                    break;
//                case 101:
//                    Toast.makeText(WelcomeActivity.this, "https", Toast.LENGTH_SHORT).show();
//                    break;
//            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        initOkHttp();
        getPublicKey();
        initView();
        setListener();

    }

    public void initOkHttp()
    {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    public void getPublicKey()
    {
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback()
                {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
//                        Toast.makeText(WelcomeActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            pubKey = jsonObject.getString("result");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e(TAG,pubKey);
                    }

                });
    }

    public void postUser()
    {
        RSA rsa = new RSA();
        OkHttpUtils
                .post()
                .url(url)
                .addParams("username", rsa.encryptByPubKey(pubKey,etUsername.getText().toString()))
                .addParams("password", rsa.encryptByPubKey(pubKey,etPassword.getText().toString()))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG,e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e(TAG,response.toString());
                    }
                });

    }

    private void initView() {
        etUsername = findViewById(R.id.et_welcome_username);
        etPassword = findViewById(R.id.et_welcome_password);
        btGo = findViewById(R.id.bt_go);
        cv = findViewById(R.id.cv);
        fab = findViewById(R.id.welcome_fab);
    }

    private void setListener(){
        btGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Explode explode = new Explode();
                explode.setDuration(500);

                getWindow().setExitTransition(explode);
                getWindow().setEnterTransition(explode);

                ActivityOptionsCompat oc2 = ActivityOptionsCompat.makeSceneTransitionAnimation(WelcomeActivity.this);
                Intent i2 = new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(i2, oc2.toBundle());
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setExitTransition(null);
                getWindow().setEnterTransition(null);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this, fab, fab.getTransitionName());
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class), options.toBundle());
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        fab.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setVisibility(View.VISIBLE);
    }
}
