package com.example.mosis;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import static androidx.core.content.ContextCompat.getSystemService;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private View view;
    private GoogleMap googleMap;
    private MapView mapView;
    private LatLngBounds mapBoundary;
    private UserLocation userLocation;
    private UserLocation userPosition;
    private ArrayList<UserModel> userList = new ArrayList<>();
    private ArrayList<MatchModel> myMatchModels = new ArrayList<>();
    private ArrayList<MatchModel> allMatchModels = new ArrayList<>();
    private UserModel userModel;

    //private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    ImageButton btnAddMatchOnMap;

    FirebaseFirestore db;

    private ArrayList<UserLocation> userLocations = new ArrayList<>();

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_map, container, false);

        db = FirebaseFirestore.getInstance();
        btnAddMatchOnMap = view.findViewById(R.id.btnAddMatchOnMap);

        if (checkLocationPermission()) {
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

                    setCameraView();
                    addMapMarkers();
                    startLocationService();
                }
            });
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));


        btnAddMatchOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddMatchActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
            }
        });

        //get My matches
        setAllMatches();

        //get All matches (mine and my friends)

        getMatches();

        setUpFont();
        return view;
    }

    private ArrayList<String> meAndFriends = new ArrayList<>();

    private void getMatches() {

        meAndFriends.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseFirestore myDb = FirebaseFirestore.getInstance();
        myDb.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    UserModel user = task.getResult().toObject(UserModel.class);

                    meAndFriends.addAll(user.getFriends());

                    getAllMatches(meAndFriends);
                }
            }
        });
    }

    private ArrayList<MatchModel> matchModels = new ArrayList<>();
    private ArrayList<MatchModel> _allMatchModels = new ArrayList<>();

    private void getAllMatches(final ArrayList<String> meAndFriends) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Matches").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful()) {
                    for(DocumentSnapshot documentSnapshot : task.getResult()) {
                        MatchModel matchModel = documentSnapshot.toObject(MatchModel.class);
                        matchModels.add(matchModel);
                    }

                    for(String user : meAndFriends) {

                        for(MatchModel matchModel : matchModels) {

                            if(matchModel.getCreator().getUser_id() == user)
                                _allMatchModels.add(matchModel);

                            Log.d("203. TAG _allMatchModels: ", "matchModel: " + matchModel);
                        }
                    }
                }
            }
        });


        Log.d("203. TAG _allMatchModels: ", "_allMatchModels " + _allMatchModels);
    }


    private void setAllMatches() {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    userModel = task.getResult().toObject(UserModel.class);

                    myMatchModels = userModel.getMatches();

                    Log.d("161: TAG", "myMatchModels: " + myMatchModels);
                    setAllMatchModelss(userModel);
                }
            }
        });
    }

    private void setAllMatchModelss(UserModel userModel) {

        FirebaseFirestore mdb = FirebaseFirestore.getInstance();

        for(String friend : userModel.getFriends()) {

            if(friend != null) {
                mdb.collection("users").document(friend).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()) {

                            UserModel userModel = task.getResult().toObject(UserModel.class);

                            allMatchModels.addAll(userModel.getMatches());

                            Log.d("168: TAG", "allMatchModels: " + allMatchModels);
                        }
                    }
                });


            }

        }

        allMatchModels.addAll(myMatchModels);
        Log.d("168: TAG", "allMatchModels: " + allMatchModels);
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(getContext(), LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                getContext().startForegroundService(serviceIntent);
                setCameraView();
                //addMapMarkers();
            } else {
                getContext().startService(serviceIntent);
                setCameraView();
                //addMapMarkers();
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d("TAG", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("TAG", "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private void startUserLocationsRunnable(){
        Log.d("TAG", "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations(){
        Log.d("TAG", "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try{
            for(final ClusterMarker clusterMarker: mClusterMarkers){

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection("User Locations")
                        .document(clusterMarker.getUser().getUser_id());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            // update the location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getUser().getUser_id().equals(updatedUserLocation.getUser().getUser_id())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeoPoint().getLatitude(),
                                                updatedUserLocation.getGeoPoint().getLongitude()
                                        );

                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                    }


                                } catch (NullPointerException e) {
                                    Log.e("TAG", "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        }catch (IllegalStateException e){
            Log.e("TAG", "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }

    }

    private void addMapMarkers() {

        if(googleMap != null){

            Log.d("TAG: ADD MAP MARKERS", "USAO SAM U TELO FUNKCIJE");

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getContext(), googleMap);

                Log.d("129. TAG: ADD MAP MARKERS", "mClusterManager: " + mClusterManager.toString());
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getContext(), googleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);

                Log.d("138. TAG: ADD MAP MARKERS", "mClusterManagerRenderer: " + mClusterManagerRenderer.toString());
            }

            //userLocations()
            db = FirebaseFirestore.getInstance();

            db.collection("User Locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot documentSnapshot : task.getResult()) {

                            userLocations.add(documentSnapshot.toObject(UserLocation.class));
                            Log.d("TAG: ADD MAP MARKERS", "userLocations " + userLocations);
                        }

                        for(UserLocation userLocation: userLocations) {

                            Log.d("TAG", "addMapMarkers: location: " + userLocation.getGeoPoint().toString());
                            try{
                                String snippet = "";
                                if(userLocation.getUser().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                                    snippet = "This is you: ";
                                }
                                else{
                                    snippet = "This is your friend: ";
                                }

                                Uri avatar = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_profile%2Fclipart-profile-4.jpg?alt=media&token=386ddecc-8dd1-41f0-be8b-adf66d235525"); // set the default avatar

                                try{
                                    if(userLocation.getUser().getImage_url().length() > 0) {
                                        avatar = Uri.parse(userLocation.getUser().getImage_url());
                                        Log.d("171. AVATAR", "addMapMarkers: avatar  " + avatar);
                                    }

                                }catch (NumberFormatException e){
                                    Log.d("TAG", "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
                                }

                                Log.d("TAG", "addMapMarkers: no avatar for " + userLocation.getUser().getUsername() + ", setting default.");
                                ClusterMarker newClusterMarker = new ClusterMarker(
                                        new LatLng(userLocation.getGeoPoint().getLatitude(), userLocation.getGeoPoint().getLongitude()),
                                        userLocation.getUser().getUsername(),
                                        snippet,
                                        avatar,
                                        userLocation.getUser()
                                );

                                //mClusterManagerRenderer.onBeforeClusterItemRendered(newClusterMarker, );

                                Log.d("185 TAG", "addMapMarkers: mClusterMarkers " + newClusterMarker);
                                mClusterManager.addItem(newClusterMarker);
                                mClusterMarkers.add(newClusterMarker);

                            } catch (NullPointerException e){
                                Log.e("TAG", "addMapMarkers: NullPointerException: " + e.getMessage() );
                            }
                        }
                        mClusterManager.cluster();

                        setCameraView();
                    }
                }
            });
            //userLocations()

            Log.d("159. TAG: ADD MAP MARKERS", "userLocations " + userLocations);
        } else {

        }
    }

    //region FUNCTIONS FOR USER LOCATION, CAMERA...

    private void setCameraView() {

        Log.d("TAG setCameraView: ", "CALLED");

        if (userPosition == null) {
            userPosition = new UserLocation();

            DocumentReference locationRef = db.collection("User Locations").document(FirebaseAuth.getInstance().getUid());

            Log.d("TAG setCameraView: ", locationRef.toString());

            locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().toObject(UserLocation.class) != null) {
                            userPosition = task.getResult().toObject(UserLocation.class);
                            Log.d("TAG USER LOCATIONS setCameraView:", userPosition.toString());
                        }
                        LocationManager locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        String provider = locationManager.getBestProvider(criteria, true);
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location = locationManager.getLastKnownLocation(provider);

                        if (location != null) {

                            LatLng target = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraPosition position = googleMap.getCameraPosition();

                            CameraPosition.Builder builder = new CameraPosition.Builder();
                            builder.zoom(15);
                            builder.target(target);

                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                        }
                    }
                }
            });
        }
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

    //endregion

    //region STATES
    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if(checkLocationPermission()) {
                mapView.onResume();
                getUserData();
                startUserLocationsRunnable();
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
            stopLocationUpdates();
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

    //endregion

    private void setUpFont() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue.ttf");
        TextView txt_Map = (TextView) view.findViewById(R.id.txt_Map);
        txt_Map.setTypeface(typeface);
    }


    private void getUserLocation(UserModel userModel) {

        DocumentReference locationRef = db.collection("User Locations").document(userModel.getUser_id());

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
                        //getUserLocation(user);
                        userList.add(user);
                    }
                }
            }
        });
    }

    private void setUsersOnMap() {

        //get all existing users



    }

}
