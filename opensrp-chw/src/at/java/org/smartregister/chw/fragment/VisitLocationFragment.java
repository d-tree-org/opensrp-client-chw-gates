package org.smartregister.chw.fragment;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.smartregister.chw.R;
import org.smartregister.chw.service.GpsLocationService;

import java.util.Objects;

/**
 * Author issyzac on 21/11/2023
 */
public class VisitLocationFragment extends Fragment {

    public static final int LOCATION_SETTINGS_REQUEST_CODE = 1812;
    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 99;

    public VisitLocationFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(getFragmentActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST_CODE);
        } else {
            startGpsLocationService();
        }

    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(getFragmentActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startGpsLocationService() {

        if (!isLocationEnabled()) {
            showLocationServiceDisabledDialog();
            return;
        }

        Intent intent = new Intent(Objects.requireNonNull(this.getActivity()), GpsLocationService.class);
        getFragmentActivity().startService(intent);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getFragmentActivity().getSystemService(LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private Activity getFragmentActivity(){
        return Objects.requireNonNull(this.getActivity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGpsLocationService();
            }
        }

        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE){
            startGpsLocationService();
        }
    }

    private void showLocationServiceDisabledDialog() {

        AlertDialog.Builder alertBulder = new AlertDialog.Builder(getFragmentActivity());
        alertBulder.setTitle(getString(R.string.location_service_disabled_title));
        alertBulder.setMessage(getString(R.string.location_service_disabled_message));

        alertBulder.setPositiveButton(getString(R.string.enable), (dialog, which) -> {
            dialog.dismiss();
            Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(settingsIntent, LOCATION_SETTINGS_REQUEST_CODE);
        });

        AlertDialog dialog = alertBulder.create();
        dialog.show();
    }

}
