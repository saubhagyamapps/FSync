package com.pro.sau.fsync;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.pro.sau.fsync.model.ImeiModel;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

public class IMEI_Permission extends AppCompatActivity {

    private static final String TAG = "IMEI_Permission";
    Button btn_no, btn_yes;
    TextView IMEI_num;
    String imei;
    ApiConfig apiConfig;
    TelephonyManager mngr;
    public static final int MULTIPLE_PERMISSIONS = 10;

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei__permission);
        apiConfig = AppConfig.getRetrofit().create(ApiConfig.class);
        mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(IMEI_Permission.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // call_permissions();
            return;
        }
        //call_permissions();
        init();
    }

    /*@Override
    protected void onStart() {
        super.onStart();
    }
*/
    private void init() {

        IMEI_num = findViewById(R.id.imei_num);
        btn_no = findViewById(R.id.no);
        btn_yes = findViewById(R.id.yes);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        imei = mngr.getDeviceId();

        IMEI_num.setText(imei);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //apicall();
                startActivity(new Intent(IMEI_Permission.this,MainActivity.class));

            }
        });
    }

/*    private void apicall() {

        Call<ImeiModel> call = apiConfig.getIMEI(imei);
        call.enqueue(new Callback<ImeiModel>() {
            @Override
            public void onResponse(Call<ImeiModel> call, retrofit2.Response<ImeiModel> response) {
                Log.e(TAG, "onResponse: " + response.body().getImei() + "fhhdkfj" + response.body().getStatus());
                if(response.body().getStatus()==0){
                    startActivity(new Intent(IMEI_Permission.this,MainActivity.class));
                }
                else {
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ImeiModel> call, Throwable t) {
                Log.e(TAG, "onFailure: " );
            }
        });
    }*/

    private void call_permissions() {

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            } else {
                finish();
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
        }
        return;
    }

}
