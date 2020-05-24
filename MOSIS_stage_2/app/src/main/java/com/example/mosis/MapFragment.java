package com.example.mosis;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import java.util.Iterator;
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
    private String searchTerm = "";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterManager<ClusterMatchMarker> mClusterMatchManager;

    private MyClusterManagerRenderer mClusterManagerRenderer;
    private MatchClusterManagerRenderer mClusterMatchManagerRenderer;

    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private ArrayList<ClusterMatchMarker> mClusterMatchMarkers = new ArrayList<>();
    private ArrayList<MatchLocation> matchLocations = new ArrayList<>();
    private ArrayList<String> filterList = new ArrayList<>();

    RelativeLayout rel_lay_search_Map;
    ImageButton btnAddMatchOnMap, btnSearchMatchOnMap, searchButton_Map;
    EditText searchField_Map;
    HorizontalScrollView filters_HorizontalView;
    Button btn_FilterAll_Map, btn_FilterAvailable_Map, btn_FilterSingle_Map, btn_FilterTournament_Map, btn_FilterCharityTournament_Map, btn_FilterFriendlyTournament_Map;
    String filterTerm = "";
    String filterAtribut = "";

    private boolean controllFilterAvailable, controlFilterSingle, controlFilterTour, controlFilterCharTour, controlFilterFrTour = false;
    private boolean controlFilterAll = true;

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
        btnSearchMatchOnMap = view.findViewById(R.id.btnSearchMatchOnMap);

        btn_FilterAll_Map = view.findViewById(R.id.btn_FilterAll_Map);
        btn_FilterAvailable_Map = view.findViewById(R.id.btn_FilterAvailable_Map);
        btn_FilterSingle_Map = view.findViewById(R.id.btn_FilterSingle_Map);
        btn_FilterCharityTournament_Map = view.findViewById(R.id.btn_FilterCharityTournament_Map);
        btn_FilterTournament_Map = view.findViewById(R.id.btn_FilterTournament_Map);
        btn_FilterFriendlyTournament_Map = view.findViewById(R.id.btn_FilterFriendlyTournament_Map);

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
                    addMatchMarkers();
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

        rel_lay_search_Map = view.findViewById(R.id.rel_lay_search_Map);
        filters_HorizontalView = view.findViewById(R.id.filters_HorizontalView);

        btnSearchMatchOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rel_lay_search_Map.getVisibility() == View.VISIBLE) {
                    rel_lay_search_Map.setVisibility(View.GONE);
                    btnSearchMatchOnMap.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_search));
                    filters_HorizontalView.setVisibility(View.VISIBLE);
//                    mClusterMatchManager.clearItems();
//                    mClusterMatchMarkers.clear();
                    clearMatches();
                    addMatchMarkers();
                }

                else {
                    rel_lay_search_Map.setVisibility(View.VISIBLE);
                    filters_HorizontalView.setVisibility(View.VISIBLE);
                    btnSearchMatchOnMap.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.collapse_arrow));
                }
            }
        });

        searchButton_Map = view.findViewById(R.id.searchButton_Map);
        searchField_Map = view.findViewById(R.id.searchField_Map);

        searchButton_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //ovde ubaci searchTerm = editdfsfsf
                if(searchField_Map.getText().toString().length() > 0) {
                    searchTerm = searchField_Map.getText().toString();
                    //searchMatchesByTerm(searchTerm);
                    Log.d("204 TAG", "filterTerm: " + filterTerm);
                    Log.d("205 TAG", "filterAtribut: " + filterAtribut);

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
                else {
                    searchTerm = "";
//                    clearMatches();
//                    addMatchMarkers();
                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
            }
        });

        btn_FilterAll_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(controlFilterAll) {
                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#ffffff"));
                    controlFilterAll = false;

                } else {
                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpoljeen));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#000000"));

                    btn_FilterAvailable_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAvailable_Map.setTextColor(Color.parseColor("#ffffff"));

                    btn_FilterSingle_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterSingle_Map.setTextColor(Color.parseColor("#ffffff"));

                    btn_FilterCharityTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterCharityTournament_Map.setTextColor(Color.parseColor("#ffffff"));

                    btn_FilterTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterTournament_Map.setTextColor(Color.parseColor("#ffffff"));

                    btn_FilterFriendlyTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterFriendlyTournament_Map.setTextColor(Color.parseColor("#ffffff"));

                    filterTerm = "";
                    filterAtribut = "";
                    controlFilterAll = true;
                    controlFilterSingle = false;
                    controlFilterTour = false;
                    controlFilterCharTour = false;
                    controlFilterFrTour = false;
                    controllFilterAvailable = false;
                    filteredMatchLocations.clear();

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
            }
        });

        btn_FilterAvailable_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(controllFilterAvailable) {
                    btn_FilterAvailable_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAvailable_Map.setTextColor(Color.parseColor("#ffffff"));
                    controllFilterAvailable = false;
                    filterTerm = "";
                    filterAtribut = "";
                    clearSpecFilter("available", "location_status");

                } else {
                    btn_FilterAvailable_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpoljeen));
                    btn_FilterAvailable_Map.setTextColor(Color.parseColor("#000000"));

                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#ffffff"));

                    filterTerm = "available";
                    filterAtribut = "location_status";
                    controllFilterAvailable = true;

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
            }
        });

        btn_FilterSingle_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(controlFilterSingle) {
                    btn_FilterSingle_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterSingle_Map.setTextColor(Color.parseColor("#ffffff"));
                    controlFilterSingle = false;
                    filterTerm = "";
                    filterAtribut = "";
                    clearSpecFilter("Single Match", "location_type");

                } else {
                    btn_FilterSingle_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpoljeen));
                    btn_FilterSingle_Map.setTextColor(Color.parseColor("#000000"));

                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#ffffff"));

                    filterTerm = "Single Match";
                    filterAtribut = "location_type";
                    controlFilterSingle = true;

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
            }
        });
        btn_FilterCharityTournament_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(controlFilterCharTour) {
                    btn_FilterCharityTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterCharityTournament_Map.setTextColor(Color.parseColor("#ffffff"));
                    controlFilterCharTour = false;
                    filterTerm = "";
                    filterAtribut = "";

                    clearSpecFilter("Charity Tournament", "location_type");

                } else {
                    btn_FilterCharityTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpoljeen));
                    btn_FilterCharityTournament_Map.setTextColor(Color.parseColor("#000000"));

                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#ffffff"));

                    filterTerm = "Charity Tournament";
                    filterAtribut = "location_type";
                    controlFilterCharTour = true;

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }

            }
        });
        btn_FilterTournament_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(controlFilterTour) {
                    btn_FilterTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterTournament_Map.setTextColor(Color.parseColor("#ffffff"));
                    controlFilterTour = false;
                    filterTerm = "";
                    filterAtribut = "";
                    clearSpecFilter("Tournament", "location_type");

                } else {
                    btn_FilterTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpoljeen));
                    btn_FilterTournament_Map.setTextColor(Color.parseColor("#000000"));

                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#ffffff"));

                    filterTerm = "Tournament";
                    filterAtribut = "location_type";
                    controlFilterTour = true;

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
            }
        });
        btn_FilterFriendlyTournament_Map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(controlFilterFrTour) {
                    btn_FilterFriendlyTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterFriendlyTournament_Map.setTextColor(Color.parseColor("#ffffff"));
                    controlFilterFrTour = false;
                    filterTerm = "";
                    filterAtribut = "";
                    clearSpecFilter("Friendly Tournament", "location_type");

                } else {
                    btn_FilterFriendlyTournament_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpoljeen));
                    btn_FilterFriendlyTournament_Map.setTextColor(Color.parseColor("#000000"));

                    btn_FilterAll_Map.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.btnpolje));
                    btn_FilterAll_Map.setTextColor(Color.parseColor("#ffffff"));

                    filterTerm = "Friendly Tournament";
                    filterAtribut = "location_type";
                    controlFilterFrTour = true;

                    filterMatchesAndSearch(searchTerm, filterTerm, filterAtribut);
                }
            }
        });

        //get My matches
        setAllMatches();

        //get All matches (mine and my friends)
        getMatches();
        setUpFont();

        return view;
    }

    private ArrayList<MatchLocation> helper = new ArrayList<>();
    private ArrayList<String> helperTerm = new ArrayList<>();

    private void clearSpecFilter(String filterTerm, String filterAtribut) {

        Log.d("467 TAG", "clearSpecFilter: " + filteredMatchLocations);

        if(filteredMatchLocations.size() > 0) {
            if(filterAtribut.equals("location_type"))
                filteredMatchLocations.removeIf(matchLocation -> matchLocation.getMatch().getType().equals(filterTerm));

            else if(filterAtribut.equals("location_status")) {

                if(controlFilterTour)
                    helperTerm.add("Tournament");

                if(controlFilterCharTour)
                    helperTerm.add("Charity Tournament");

                if(controlFilterFrTour)
                    helperTerm.add("Friendly Tournament");

                if(controlFilterSingle)
                    helperTerm.add("Single Match");



//                for(MatchLocation matchLocation : filteredMatchLocations) {
//                    if(helperTerm.get)
//                    helperTerm.add(matchLocation.getMatch().getType());
//                }
            }
        }

        clearMatches();

        if(filteredMatchLocations.size() > 0) {
            for(MatchLocation matchLocation: filteredMatchLocations) {

                try{
                    String snippet = "";
                    if(matchLocation.getMatch().getCreator().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                        snippet = "This is your match: ";
                    }
                    else{
                        snippet = "This is your friend's match: ";
                    }

                    Uri avatar = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a"); // set the default avatar

                    try{
                        if(matchLocation.getMatch().getImage_url().length() > 0)
                            avatar = Uri.parse(matchLocation.getMatch().getImage_url());

                    } catch (NumberFormatException e){
                        Log.d("TAG", "addMapMarkers: no avatar for " + matchLocation.getMatch().getCreator().getUsername() + ", setting default.");
                    }

                    ClusterMatchMarker newClusterMarker = new ClusterMatchMarker(
                            new LatLng(matchLocation.getGeoPoint().getLatitude(), matchLocation.getGeoPoint().getLongitude()),
                            matchLocation.getMatch().getName(),
                            snippet,
                            avatar,
                            matchLocation.getMatch()
                    );

                    mClusterMatchManager.addItem(newClusterMarker);
                    mClusterMatchMarkers.add(newClusterMarker);

                } catch (NullPointerException e){
                    Log.e("TAG", "addMapMarkers: NullPointerException: " + e.getMessage() );
                }
            }
        } else {
            if(searchTerm.length() > 0) {
                filterMatchesAndSearch(searchTerm, "", "");
            } else addMatchMarkers();
        }

        mClusterMatchManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMatchMarker>() {
            @Override
            public boolean onClusterItemClick(ClusterMatchMarker item) {

                Log.d("522 TAG", "Item: " + item.getTitle() + " /// " + item.getMatch().toString());

                try {

                    Intent intent = new Intent(getContext(), MatchActivity.class);
                    intent.putExtra("match_id", item.getMatch().getId());

                    startActivity(intent);
                    getActivity().overridePendingTransition(0,0);

                    return true;

                } catch (Exception e) {
                    return false;
                }
            }
        });

        mClusterMatchManager.cluster();

        Log.d("477 TAG", "clearSpecFilter: " + filteredMatchLocations);
    }

    private ArrayList<MatchLocation> prevMatchLocations = new ArrayList<>();
    private ArrayList<MatchLocation> filteredMatchLocations = new ArrayList<>();

    private void filterMatchesAndSearch(final String searchTerm, final String filterTerm, final String atribut) {

        if(googleMap != null) {

            clearMatches();

            if(mClusterMatchManager == null) {
                mClusterMatchManager = new ClusterManager<ClusterMatchMarker>(getContext(), googleMap);
            }

            if(mClusterMatchManagerRenderer == null) {
                mClusterMatchManagerRenderer = new MatchClusterManagerRenderer(
                        getContext(), googleMap,
                        mClusterMatchManager
                );
                mClusterMatchManager.setRenderer(mClusterMatchManagerRenderer);
            }

            //userLocations()
            db = FirebaseFirestore.getInstance();

            db.collection("Match Locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot documentSnapshot : task.getResult()) {

                            MatchLocation _matchLocation = documentSnapshot.toObject(MatchLocation.class);
                            matchLocations.add(_matchLocation);
                        }

                        if(searchTerm.length() > 0) {

                            //TODO -- it has searchTerm, ask for filters

                            Log.d("532 TAG", "searchTerm: " + searchTerm);

                            if (matchLocations.size() > 0)
                                matchLocations.removeIf(mLoc -> !mLoc.getMatch().getName().equals(searchTerm));


                            if (filteredMatchLocations.size() > 0)
                                filteredMatchLocations.removeIf(mLoc -> !mLoc.getMatch().getName().equals(searchTerm));

                            if (filterTerm.length() > 0) {
                                //TODO -- it has filter, filter matchLocations by that filter

                                if (atribut.equals("location_type")) {

                                    for(MatchLocation matchLocation : matchLocations) {
                                        if (matchLocation.getMatch().getType().equals(filterTerm))
                                            filteredMatchLocations.add(matchLocation);
                                    }

                                } else if (atribut.equals("location_status")) {

                                    if (filteredMatchLocations.size() > 0) {

                                        prevMatchLocations = filteredMatchLocations;
                                        filteredMatchLocations.removeIf(mLoc -> !mLoc.getMatch().getStatus().equals(filterTerm));
                                    }
                                    else {

                                        prevMatchLocations.clear();
                                        //prevMatchLocations.addAll(matchLocations);

                                        matchLocations.removeIf(matchLocation -> !matchLocation.getMatch().getStatus().equals(filterTerm));
                                        filteredMatchLocations.addAll(matchLocations);

                                        matchLocations.clear();
                                        matchLocations.addAll(prevMatchLocations);
                                    }
                                }
                            }
                        }
                        
                        else if (filterTerm.length() > 0) {
                            //TODO -- it has filter, filter matchLocations by that filter

                            Log.d("341 TAG", "filterTerm: " + filterTerm);
                            Log.d("342 TAG", "atribut: " + atribut);

                            if (atribut.equals("location_type")) {

                                for(MatchLocation matchLocation : new ArrayList<MatchLocation>(matchLocations)) {
                                    if (matchLocation.getMatch().getType().equals(filterTerm))
                                        filteredMatchLocations.add(matchLocation);
                                }

                            } else if (atribut.equals("location_status")) {

                                if (filteredMatchLocations.size() > 0)
                                    filteredMatchLocations.removeIf(matchLocation -> !matchLocation.getMatch().getStatus().equals(filterTerm));

                                else matchLocations.removeIf(matchLocation -> !matchLocation.getMatch().getStatus().equals(filterTerm));
                            }
                        }

                        if(filteredMatchLocations.size() > 0) {
                            for(MatchLocation matchLocation: filteredMatchLocations) {

                                try{
                                    String snippet = "";
                                    if(matchLocation.getMatch().getCreator().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                                        snippet = "This is your match: ";
                                    }
                                    else{
                                        snippet = "This is your friend's match: ";
                                    }

                                    Uri avatar = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a"); // set the default avatar

                                    try{
                                        if(matchLocation.getMatch().getImage_url().length() > 0)
                                            avatar = Uri.parse(matchLocation.getMatch().getImage_url());

                                    } catch (NumberFormatException e){
                                        Log.d("TAG", "addMapMarkers: no avatar for " + matchLocation.getMatch().getCreator().getUsername() + ", setting default.");
                                    }

                                    ClusterMatchMarker newClusterMarker = new ClusterMatchMarker(
                                            new LatLng(matchLocation.getGeoPoint().getLatitude(), matchLocation.getGeoPoint().getLongitude()),
                                            matchLocation.getMatch().getName(),
                                            snippet,
                                            avatar,
                                            matchLocation.getMatch()
                                    );

                                    mClusterMatchManager.addItem(newClusterMarker);
                                    mClusterMatchMarkers.add(newClusterMarker);

                                } catch (NullPointerException e){
                                    Log.e("TAG", "addMapMarkers: NullPointerException: " + e.getMessage() );
                                }
                            }
                        }
                        else if(matchLocations.size() > 0) {
                            for(MatchLocation matchLocation: matchLocations) {

                                try{
                                    String snippet = "";
                                    if(matchLocation.getMatch().getCreator().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                                        snippet = "This is your match: ";
                                    }
                                    else{
                                        snippet = "This is your friend's match: ";
                                    }

                                    Uri avatar = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a"); // set the default avatar

                                    try{
                                        if(matchLocation.getMatch().getImage_url().length() > 0)
                                            avatar = Uri.parse(matchLocation.getMatch().getImage_url());

                                    } catch (NumberFormatException e){
                                        Log.d("TAG", "addMapMarkers: no avatar for " + matchLocation.getMatch().getCreator().getUsername() + ", setting default.");
                                    }

                                    ClusterMatchMarker newClusterMarker = new ClusterMatchMarker(
                                            new LatLng(matchLocation.getGeoPoint().getLatitude(), matchLocation.getGeoPoint().getLongitude()),
                                            matchLocation.getMatch().getName(),
                                            snippet,
                                            avatar,
                                            matchLocation.getMatch()
                                    );

                                    mClusterMatchManager.addItem(newClusterMarker);
                                    mClusterMatchMarkers.add(newClusterMarker);

                                } catch (NullPointerException e){
                                    Log.e("TAG", "addMapMarkers: NullPointerException: " + e.getMessage() );
                                }
                            }
                        }

                        mClusterMatchManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMatchMarker>() {
                            @Override
                            public boolean onClusterItemClick(ClusterMatchMarker item) {

                                Log.d("522 TAG", "Item: " + item.getTitle() + " /// " + item.getMatch().toString());

                                try {

                                    Intent intent = new Intent(getContext(), MatchActivity.class);
                                    intent.putExtra("match_id", item.getMatch().getId());

                                    startActivity(intent);
                                    getActivity().overridePendingTransition(0,0);

                                    return true;

                                } catch (Exception e) {
                                    return false;
                                }
                            }
                        });

                        mClusterMatchManager.cluster();
                    }

                    //mClusterMatchManager.cluster();
                }
            });
        }

        //for()
    }

//    private void filterMatches(ArrayList<String> filterList) {
//
//        for(String filterTerm : filterList) {
//            switch (filterTerm) {
//                case "All" :
//                    clearMatches();
//                    addMatchMarkers();
//                    break;
//
//                case "Single Match" :
//                    clearMatches();
//                    filterMatchesByTerm(filterTerm, "location_type");
//                    break;
//
//                case "Tournament" :
//                    clearMatches();
//                    filterMatchesByTerm(filterTerm, "type");
//                    break;
//
//                case "Charity Tournament" :
//                    clearMatches();
//                    filterMatchesByTerm(filterTerm, "type");
//                    break;
//
//                case "Friendly Tournament" :
//                    clearMatches();
//                    filterMatchesByTerm(filterTerm, "type");
//                    break;
//
//                case "available" :
//                    clearMatches();
//                    filterMatchesByTerm(filterTerm, "status");
//                    break;
//
//                case "played" :
//                    clearMatches();
//                    filterMatchesByTerm(filterTerm, "status");
//                    break;
//            }
//        }
//    }
//
//    private void filterMatchesByTerm(String filterTerm, String atribut) {
//        if(filterTerm.equals("All")) {
//
//        }
//    }

    private void searchMatchesByTerm(final String searchTerm) {

        if(googleMap != null) {

            clearMatches();

            Log.d("206 TAG", "searchMatchesByTerm: " + mClusterMatchMarkers + " " + mClusterMatchManager);

            if(mClusterMatchManager == null) {
                mClusterMatchManager = new ClusterManager<ClusterMatchMarker>(getContext(), googleMap);
            }

            if(mClusterMatchManagerRenderer == null) {
                mClusterMatchManagerRenderer = new MatchClusterManagerRenderer(
                        getContext(), googleMap,
                        mClusterMatchManager
                );
                mClusterMatchManager.setRenderer(mClusterMatchManagerRenderer);
            }

            //userLocations()
            db = FirebaseFirestore.getInstance();

            db.collection("Match Locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot documentSnapshot : task.getResult()) {

                            MatchLocation _matchLocation = documentSnapshot.toObject(MatchLocation.class);

                            Log.d("347 TAG", "matchLocation: " + _matchLocation.getMatch().getName());

                            matchLocations.add(_matchLocation);

                        }

                        for(MatchLocation matchLocation: matchLocations) {

                            Log.d("352 TAG", "matchLocation: " + matchLocation.getMatch().getName());
                            if(matchLocation.getMatch().getName().equals(searchTerm)) {

                                Log.d("354 TAG", "matchLocation: " + matchLocation.getMatch().getName());
                                try{
                                    String snippet = "";
                                    if(matchLocation.getMatch().getCreator().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                                        snippet = "This is your match: ";
                                    }
                                    else{
                                        snippet = "This is your friend's match: ";
                                    }

                                    Uri avatar = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a"); // set the default avatar

                                    try{
                                        if(matchLocation.getMatch().getImage_url().length() > 0)
                                            avatar = Uri.parse(matchLocation.getMatch().getImage_url());

                                    } catch (NumberFormatException e){
                                        Log.d("TAG", "addMapMarkers: no avatar for " + matchLocation.getMatch().getCreator().getUsername() + ", setting default.");
                                    }

                                    ClusterMatchMarker newClusterMarker = new ClusterMatchMarker(
                                            new LatLng(matchLocation.getGeoPoint().getLatitude(), matchLocation.getGeoPoint().getLongitude()),
                                            matchLocation.getMatch().getName(),
                                            snippet,
                                            avatar,
                                            matchLocation.getMatch()
                                    );

                                    mClusterMatchManager.addItem(newClusterMarker);
                                    mClusterMatchMarkers.add(newClusterMarker);

                                } catch (NullPointerException e){
                                    Log.e("TAG", "addMapMarkers: NullPointerException: " + e.getMessage() );
                                }

                            }
                        }

                        mClusterMatchManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMatchMarker>() {
                            @Override
                            public boolean onClusterItemClick(ClusterMatchMarker item) {

                                Log.d("522 TAG", "Item: " + item.getTitle() + " /// " + item.getMatch().toString());

                                try {

                                    Intent intent = new Intent(getContext(), MatchActivity.class);
                                    intent.putExtra("match_id", item.getMatch().getId());

                                    startActivity(intent);
                                    getActivity().overridePendingTransition(0,0);

                                    return true;

                                } catch (Exception e) {
                                    return false;
                                }
                            }
                        });

                        mClusterMatchManager.cluster();
                    }

                    //mClusterMatchManager.cluster();
                }
            });
        }

    }

    private void clearMatches() {

        if(googleMap != null) {
            mClusterMatchMarkers.clear();
            mClusterMatchManager.clearItems();
            matchLocations.clear();
            //mClusterMatchManagerRenderer = null;

            Log.d("420 TAG", mClusterMatchMarkers.toString());
        }
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

    private void addMatchMarkers() {

        if(googleMap != null){

            if(mClusterMatchManager != null && mClusterMatchMarkers != null) {
                clearMatches();
            }

            if(mClusterMatchManager == null){
                mClusterMatchManager = new ClusterManager<ClusterMatchMarker>(getContext(), googleMap);

            }
            if(mClusterMatchManagerRenderer == null){
                mClusterMatchManagerRenderer = new MatchClusterManagerRenderer(
                        getContext(), googleMap,
                        mClusterMatchManager
                );
                mClusterMatchManager.setRenderer(mClusterMatchManagerRenderer);
            }

            //userLocations()
            db = FirebaseFirestore.getInstance();

            db.collection("Match Locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot documentSnapshot : task.getResult()) {
                            matchLocations.add(documentSnapshot.toObject(MatchLocation.class));
                        }

                        for(MatchLocation matchLocation: matchLocations) {

                            try{
                                String snippet = "";
                                if(matchLocation.getMatch().getCreator().getUser_id().equals(FirebaseAuth.getInstance().getUid())){
                                    snippet = "This is your match: ";
                                }
                                else{
                                    snippet = "This is your friend's match: ";
                                }

                                Uri avatar = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a"); // set the default avatar

                                try{
                                    if(matchLocation.getMatch().getImage_url().length() > 0)
                                        avatar = Uri.parse(matchLocation.getMatch().getImage_url());

                                } catch (NumberFormatException e){
                                    Log.d("TAG", "addMapMarkers: no avatar for " + matchLocation.getMatch().getCreator().getUsername() + ", setting default.");
                                }

                                ClusterMatchMarker newClusterMarker = new ClusterMatchMarker(
                                        new LatLng(matchLocation.getGeoPoint().getLatitude(), matchLocation.getGeoPoint().getLongitude()),
                                        matchLocation.getMatch().getName(),
                                        snippet,
                                        avatar,
                                        matchLocation.getMatch()
                                );

                                mClusterMatchManager.addItem(newClusterMarker);
                                mClusterMatchMarkers.add(newClusterMarker);

                            } catch (NullPointerException e){
                                Log.e("TAG", "addMapMarkers: NullPointerException: " + e.getMessage() );
                            }
                        }

                        mClusterMatchManager.cluster();

                        mClusterMatchManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMatchMarker>() {
                            @Override
                            public boolean onClusterItemClick(ClusterMatchMarker item) {

                                Log.d("522 TAG", "Item: " + item.getTitle() + " /// " + item.getMatch().toString());

                                try {

                                    Intent intent = new Intent(getContext(), MatchActivity.class);
                                    intent.putExtra("match_id", item.getMatch().getId());

                                    startActivity(intent);
                                    getActivity().overridePendingTransition(0,0);

                                    return true;

                                } catch (Exception e) {
                                    return false;
                                }
                            }
                        });

                        prevMatchLocations = matchLocations;
                    }
                }
            });

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

        btn_FilterAll_Map = view.findViewById(R.id.btn_FilterAll_Map);
        btn_FilterAvailable_Map = view.findViewById(R.id.btn_FilterAvailable_Map);
        btn_FilterSingle_Map = view.findViewById(R.id.btn_FilterSingle_Map);
        btn_FilterTournament_Map = view.findViewById(R.id.btn_FilterTournament_Map);
        btn_FilterCharityTournament_Map = view.findViewById(R.id.btn_FilterCharityTournament_Map);
        btn_FilterFriendlyTournament_Map = view.findViewById(R.id.btn_FilterFriendlyTournament_Map);

        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue.ttf");
        TextView txt_Map = (TextView) view.findViewById(R.id.txt_Map);
        txt_Map.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");

        btn_FilterAll_Map.setTypeface(typeface2);
        btn_FilterAvailable_Map.setTypeface(typeface2);
        btn_FilterSingle_Map.setTypeface(typeface2);
        btn_FilterTournament_Map.setTypeface(typeface2);
        btn_FilterCharityTournament_Map.setTypeface(typeface2);
        btn_FilterFriendlyTournament_Map.setTypeface(typeface2);

        searchField_Map = view.findViewById(R.id.searchField_Map);
        searchField_Map.setTypeface(typeface2);
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