package com.nilhcem.bblfr.core.map;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class MapUtils {

    public static final float HUE_DEFAULT = 25.0f;

    private MapUtils() {
        throw new UnsupportedOperationException();
    }

    public static Observable<GoogleMap> getGoogleMapObservable(final MapFragment fragment) {
        return Observable.create(new Observable.OnSubscribe<GoogleMap>() {
            @Override
            public void call(Subscriber<? super GoogleMap> subscriber) {
                subscriber.onNext(fragment.getMap());
                subscriber.onCompleted();
            }
        });
    }

    public static LatLng gpsToLatLng(String gps) {
        String[] gpsSplit = gps.split(",");
        return new LatLng(Double.parseDouble(gpsSplit[0].trim()), Double.parseDouble(gpsSplit[1].trim()));
    }

    public static void moveToCurrentLocation(GoogleMap map, List<Marker> markers, Location lastLocation, float zoom) {
        map.setMyLocationEnabled(true);

        if (lastLocation == null && markers != null && !markers.isEmpty()) {
            moveToMarkerBounds(map, markers);
        } else if (lastLocation != null) {
            LatLng currentLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoom));
        } else {
            Timber.w("Both lastLocation and markers are null");
        }
    }

    public static void moveToMarkerBounds(GoogleMap map, List<Marker> markers) {
        // Calculate the bounds of all the markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        // Obtain a movement description object
        int padding = 0; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        // Move the map
        map.moveCamera(cu);
    }
}
