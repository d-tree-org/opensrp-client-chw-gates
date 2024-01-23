package org.smartregister.chw.viewmodel;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.smartregister.chw.repository.VisitLocationRepository;

/**
 * Author issyzac on 21/11/2023
 */
public class VisitLocationViewModel extends ViewModel {

    private VisitLocationRepository locationRepository;

    public VisitLocationViewModel() {
        locationRepository = VisitLocationRepository.getInstance();
    }

    public LiveData<Location> getLocationLiveData() {
        return locationRepository.getLocationLiveData();
    }

}
