package com.pro.sau.fsync;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.pro.sau.fsync.model.ImeiModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_PHONE_STATE;

public class InfoActivity extends AppCompatActivity {

    private static final String TAG = "InfoActivity";
    private static final int PERMISSION_REQUEST_CODE = 200;
    ApiConfig apiService;
    TextView txtiemi;
    Button btnnext, btncancel;
    String imei;
    String flag = "1";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService =
                AppConfig.getRetrofit().create(ApiConfig.class);


        apicall();
        if (!checkPermission()) {

            requestPermission();

        } else {

            // Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            // Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();
            getUniqueIMEIId(this);
        }

        getUniqueIMEIId(getApplicationContext());
    }

    private void intli() {
        setContentView(R.layout.activity_info);
        txtiemi = findViewById(R.id.txtiemi);
        btnnext = findViewById(R.id.btnnext);
        btncancel = findViewById(R.id.btncancel);
        txtiemi.setText(imei);


    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);


        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted) {
                        Toast.makeText(this, "Permission Granted, Now you can access phone state data ", Toast.LENGTH_SHORT).show();
                        getUniqueIMEIId(this);
                    }
                    else {

                        Toast.makeText(this, "Permission Denied, You cannot access phone state data", Toast.LENGTH_SHORT).show();
                        //  Snackbar.make(view, "Permission Denied, You cannot access location data and camera.", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
                                showMessageOKCancel(
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_PHONE_STATE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }

                break;
        }
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(InfoActivity.this)
                .setMessage("You need to allow access to both the permissions")
                .setPositiveButton("OK", okListener)
                /* .setNegativeButton("Cancel", null)*/
                .create()
                .show();
    }

    public String getUniqueIMEIId(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "";
            }
            imei = telephonyManager.getDeviceId();
            Log.e("imei", "=" + imei);
            if (imei != null && !imei.isEmpty()) {

                txtiemi.setText(imei);
                return imei;
            } else {
                return android.os.Build.SERIAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "not_found";
    }


    private void apicall() {
        Call<ImeiModel> modelCall = apiService.getIMEI();
        modelCall.enqueue(new Callback<ImeiModel>() {
            @Override
            public void onResponse(Call<ImeiModel> call, Response<ImeiModel> response) {
                for (int i = 0; i < response.body().getData().size(); i++) {
                    if (response.body().getData().get(i).equals(imei)) {
                        flag = "0";
                        Log.e(TAG, "onResponse: " + response.body().getData().get(i));

                    } else {

                    }
                }
                if (flag.equals("0")) {
                    if (response.body().getApp_Status().equals("started")) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), ServerOfflineAcivity.class));
                        finish();
                    }

                } else {
                    if (!checkPermission()) {

                        requestPermission();

                    } else {

                        getUniqueIMEIId(InfoActivity.this);
                    }
                    intli();
                    btnnext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("IMEI", imei);
                            clipboard.setPrimaryClip(clip);

                            Toast.makeText(InfoActivity.this, "Text Copy", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<ImeiModel> call, Throwable t) {
                Toast.makeText(InfoActivity.this, "Sorry! Not connected to internet", Toast.LENGTH_LONG).show();

                Log.e(TAG, "onFailure: ");
            }
        });
    }
}