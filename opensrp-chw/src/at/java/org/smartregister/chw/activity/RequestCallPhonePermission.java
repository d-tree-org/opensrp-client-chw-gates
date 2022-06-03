package org.smartregister.chw.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.smartregister.chw.R;

public class RequestCallPhonePermission extends FragmentActivity {
    private static final int READ_PHONE_NUMBERS_PERMISSION_CODE = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_PHONE_NUMBERS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(RequestCallPhonePermission.this, "Read Phone Number Permission Granted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RequestCallPhonePermission.this, "Read Phone Number Permission Not Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(RequestCallPhonePermission.this,
                Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
            startLoginActivity();
        }
        setContentView(R.layout.activity_request_call_phone_permission);
    }

    public void onClickNoThanks(View view) {

    }

    public void onClickGrant(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkPermission(Manifest.permission.READ_PHONE_NUMBERS, READ_PHONE_NUMBERS_PERMISSION_CODE);
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(RequestCallPhonePermission.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(RequestCallPhonePermission.this, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(RequestCallPhonePermission.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void startLoginActivity() {
        Intent intent = new Intent(RequestCallPhonePermission.this, LoginActivity.class);
        startActivity(intent);
    }
}