package com.example.mosis;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private View view;
    private GoogleMap googleMap;
    private MapView mapView;
    private ArrayList<UserModel> userList = new ArrayList<>();
    //private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private UserLocation userLocation;
    FirebaseFirestore db;

    private ArrayList<UserLocation> userLocations = new ArrayList<>();

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_map, container, false);

        db = FirebaseFirestore.getInstance();

        if(checkLocationPermission()) {
            mapView = (MapView) view.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);

            mapView.onResume();
            try {
                MapsInitializer.initialize(Objects.requireNonNull(getActivity()).getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    googleMap.setMyLocationEnabled(true);
                    //To add marker
                    LatLng sydney = new LatLng(-34, 151);
                    googleMap.addMarker(new MarkerOptions().position(sydney).title("Title").snippet("Marker Description"));
                    // For zooming functionality
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));

        setUpFont();
        return view;
    }

    private void getUserLocation(UserModel userModel) {

        DocumentReference locationRef = db.collection("User Locations").document(userModel.getUser_Id());

        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().toObject(UserLocation.class) != null) {
                        userLocations.add(task.getResult().toObject(UserLocation.class));
                        Log.d("TAG USER LOCATIONS getUserLocation:", task.getResult().toObject(UserLocation.class).toString());
                    }
                }
            }
        });

    }

    private void showData() {
        db = FirebaseFirestore.getInstance();

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {

                    for(DocumentSnapshot documentSnapshot : task.getResult()) {

                        //Log.d("TAG", documentSnapshot.get("username").toString() + " " + documentSnapshot.get("team").toString());
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        Log.d("TAG SHOW DATA", user.toString());
                        getUserLocation(user);
                        userList.add(user);
                    }
                }
            }
        });
    }

    private void getUserData() {
        if(userLocation == null) {
            userLocation = new UserLocation();

            DocumentReference userRef = db.collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Log.d("TAG", "onComplete: successfully get the user details. ");

                    UserModel user = Objects.requireNonNull(task.getResult()).toObject(UserModel.class);
                    userLocation.setUser(user);
                    ((UserAuth)(getActivity().getApplicationContext())).setUser(user);
                    getLastKnownLocation();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });

        }
    }

    private void saveUserLocation() {

        db = FirebaseFirestore.getInstance();

        if(userLocation != null){
            DocumentReference locationRef = db.collection("User Locations").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("TAG", "savedUserLocation: \ninsered user location into database." + "\n latitude: " + userLocation.getGeoPoint().getLatitude()+"\n longitude: " + userLocation.getGeoPoint().getLatitude());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });
        }
    }

    private void getLastKnownLocation() {
        Log.d("LAST KNOWN LOCATION", "getLastKnownLocation: called.");

//        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    Location location = task.getResult();
                    assert location != null;
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    Log.d("TAG", "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d("TAG", "onComplete: longitude: " + geoPoint.getLongitude());

                    userLocation.setGeoPoint(geoPoint);
                    userLocation.setTimestamp(null);
                    saveUserLocation();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });
    }


    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("DO YOU WANT TO ADD THIS PERMISSION")
                        .setMessage("DO YOU WANT TO ADD THIS PERMISSION")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.ACCESS_FINE_LOCATION);//Request location updates:
//locationManager.requestLocationUpdates(provider, 400, 1, this);

            }  // permission denied, boo! Disable the
            // functionality that depends on this permission.

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if(checkLocationPermission()) {
                mapView.onResume();
                //getLastKnownLocation();
                getUserData();
                showData();

            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onPause();
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onDestroy();
        }


    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapView.onLowMemory();
        }

    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        google_map = googleMap;
//
//        //43°19'19.0"N 21°54'16.4"E
//        LatLng MyLocation = new LatLng(43.321951, 21.904550);
//        google_map.addMarker(new MarkerOptions().position(MyLocation).title("Лука Јеж"));
//        google_map.moveCamera(CameraUpdateFactory.newLatLng(MyLocation));
//    }


    private void setUpFont() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue.ttf");
        TextView txt_Map = (TextView) view.findViewById(R.id.txt_Map);
        txt_Map.setTypeface(typeface);
    }

}
