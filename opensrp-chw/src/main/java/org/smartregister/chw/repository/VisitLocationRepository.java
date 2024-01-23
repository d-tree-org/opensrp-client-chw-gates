package org.smartregister.chw.repository;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by Kassim Sheghembe on 2023-11-16
 */
public class VisitLocationRepository {

    private MutableLiveData<Location> locationLiveData = new MutableLiveData<>();

    public void setLocationLiveData(Location location) {
        locationLiveData.setValue(location);
    }

    public LiveData<Location> getLocationLiveData() {
        return locationLiveData;
    }

    private static VisitLocationRepository instance;

    public static VisitLocationRepository getInstance() {
        if (instance == null) {
            instance = new VisitLocationRepository();
        }
        return instance;
    }
}
