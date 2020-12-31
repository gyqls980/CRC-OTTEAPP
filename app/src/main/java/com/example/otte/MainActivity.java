package com.example.otte;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.otte.colorpicker.ColorPickerActivity;
import com.example.otte.sendData.ApiService;
import com.example.otte.sendData.ConnectionOTTE;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // ConnectionScene의 actions()에서 receiveData() 호출하기 위해 사용
    public static Context mContext;

    Button ColorPick;
    TextView ReceiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        // /layout/activity_main.xml에 설정한 것들을 가져옴
        ReceiveData = (TextView) findViewById(R.id.tvReceiveData);
        ColorPick = (Button) findViewById(R.id.btnColorPicker);

        // 발생하는 이벤트를 구현한 리스너
        ColorPick.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPick();
            }
        });
    }

    // 패스너 데이터 받아서 textview에 출력
    public void receiveData(final String accessToken, String message) {
        // SingleClick = -1, DoubleClick = 1
        Integer conv_review = 0;

        if(message=="SingleClick"){
            ReceiveData.setText("춥다");
            conv_review = -1;
        }
        else if(message=="DoubleClick"){
            ReceiveData.setText("덥다");
            conv_review = 1;
        }
        else{
        }

        Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ReceiveData.setText("");
            }
        }, 3000);

        // 현재 날짜, 시간
        SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd");
        SimpleDateFormat time = new SimpleDateFormat( "HH:mm:ss");

        Date current_datetime = new Date();

        String current_date = date.format(current_datetime);
        String current_time = time.format(current_datetime);

        // set retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://") // ADD EC2 URL ADDRESS
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service1 = retrofit.create(ApiService.class);

        // send data to OTTE server
        ConnectionOTTE reviewData = new ConnectionOTTE("", current_date, current_time, conv_review); // ADD OTTE ID
        Call<ConnectionOTTE> callData = service1.sendReviewData(accessToken, reviewData);
        callData.enqueue(new Callback<ConnectionOTTE>() {
            @Override
            public void onResponse(Call<ConnectionOTTE> callData, Response<ConnectionOTTE> response) {
                if(response.isSuccessful()){
                    ConnectionOTTE result = response.body();
                    Log.d("sendData", "Success!");
                }
                else{
                    Log.d("sendDataFail", response.toString());
                }
            }

            @Override
            public void onFailure(Call<ConnectionOTTE> callData, Throwable t) {
                Log.d("onFailure", t.getMessage());
            }
        });
    }

    // 패스너 연결 test
    void colorPick() {
        Intent colorPickerSceneIntent = new Intent(getApplicationContext(), ColorPickerActivity.class);
        startActivity(colorPickerSceneIntent);
    }
}

