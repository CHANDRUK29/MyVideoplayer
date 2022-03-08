package com.chandru.videoplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.PublicKey;

public class AccessActivity extends AppCompatActivity {

    Button allowbtn;
    public static final int STORAGE_PERMISSION = 1;
    public static final int REQUEST_PERMISSION_SETTING = 12;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        sharedPreferences = getSharedPreferences("Allow access",MODE_PRIVATE);

        String value = sharedPreferences.getString("Allow","");

        if (value.equals("OK")){
            Intent i = new Intent(AccessActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }else{
            editor = sharedPreferences.edit();
            editor.putString("Allow","OK");
            editor.apply();

        }
        allowbtn = findViewById(R.id.allowbtn);
        allowbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(AccessActivity.this, "Allow Access!!!", Toast.LENGTH_SHORT).show();
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    Intent i = new Intent(AccessActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    ActivityCompat.requestPermissions(AccessActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
                }
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode ==STORAGE_PERMISSION){
            for (int i = 0; i<permissions.length; i++){
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    boolean showRational = shouldShowRequestPermissionRationale(permission);
                    if (!showRational){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("APP PERMISSION").setMessage("You must allow permission to play videos"
                                + "\n\n" + "Now follow the below steps" + "\n\n" +"open Settings from below button"
                        + "\n" + "click on permissions" + "\n" + "Allow Access for Storage")
                                .setPositiveButton("open Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                                        intent.setData(uri);
                                        startActivityForResult(intent,REQUEST_PERMISSION_SETTING);
                                    }
                                }).create().show();

                    }else{
                        ActivityCompat.requestPermissions(AccessActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);

                    }
                }else{
                    Intent intent = new Intent(AccessActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(AccessActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

    }
}