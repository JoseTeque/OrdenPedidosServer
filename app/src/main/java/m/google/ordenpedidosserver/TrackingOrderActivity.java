package m.google.ordenpedidosserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.common.DirectionJSONParser;
import m.google.ordenpedidosserver.remote.IGeoCoordinates;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 500;
    private GoogleMap mMap;


    private LocationCallback mLastLocationCallback;
    private FusedLocationProviderClient apiClientProvider;
    private LocationRequest locationRequest;
    private Location location;
    private Marker marker;
    private Polyline polyline;

    public String url;

    private IGeoCoordinates mServices;

    private static int UPDATE_INTERVAL = 1000;
    private static int FATEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;
    private String key= "AIzaSyDYl3qJ5QMXLf8gqtgZTi45qeIBHADGEKI";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);


        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        InitFused();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //inicializando retrofit
        mServices= new Retrofit.Builder().baseUrl("https://maps.googleapis.com")
                .addConverterFactory(ScalarsConverterFactory.create()).build().create(IGeoCoordinates.class);
    }

    private void InitFused() {
        apiClientProvider = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        builLocationRequest();
        builLocationCallback();
        apiClientProvider.requestLocationUpdates(locationRequest, mLastLocationCallback, Looper.myLooper());
    }

    public void builLocationCallback() {
        mLastLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                location = locationResult.getLocations().get(locationResult.getLocations().size() - 1);

                if (ActivityCompat.checkSelfPermission(TrackingOrderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TrackingOrderActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                apiClientProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {

                            if (marker != null) {
                                marker.remove();
                            }
                            LatLng yourLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(yourLocation)
                                    .title("YOUR LOCATION").icon(BitmapDescriptorFactory.defaultMarker()));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourLocation,15.0f));

                            // After add Marker for your location, add Marker for this order and draw route


                            drawRoute(yourLocation,Common.currentRequest.getAddress(),key);

                        }
                    }
                });
            }
        };
    }

    public void drawRoute(final LatLng yourLocation, String address, final String KEY) {

        mServices.getGeocode(address,KEY).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {
                    JSONObject jsonObject= new JSONObject(response.body().toString());

                    String lat= ((JSONArray)jsonObject.get("results") )
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lat").toString();

                    String lng= ((JSONArray)jsonObject.get("results") )
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();


                    LatLng locationOrder= new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    mMap.addMarker(new MarkerOptions().position(locationOrder).title("Order of: "+ Common.currentRequest.getPhone()));

                    // Draw route

                    try {
                        url="https://maps.googleapis.com/maps/api/directions/json?origin="+yourLocation.latitude+"," +yourLocation.longitude+"&destination="+locationOrder.latitude+"," +locationOrder.longitude+"&key="+key+"";
                        Log.e("URL",url);

                        mServices.ObtenerRuta(url).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                new ParserTask().execute(response.body());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });

                    }catch (Exception e)
                    {

                    }


                    }catch (Exception e)
                    {

                    }


                }


            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void builLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void checkPermission(String accessFineLocation, String accessCoarseLocation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) ;
            {
                ActivityCompat.requestPermissions(this, new String[]
                        {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, LOCATION_PERMISSION_REQUEST);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    InitFused();
                } else {
                    Toast.makeText(this, "ES NECESARIO DAR PERMISO", Toast.LENGTH_SHORT).show();
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        builLocationRequest();
        builLocationCallback();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        apiClientProvider.requestLocationUpdates(locationRequest, mLastLocationCallback, Looper.myLooper());
    }

    @Override
    protected void onStop() {
        apiClientProvider.removeLocationUpdates(mLastLocationCallback);
        super.onStop();
    }

    public class ParserTask extends AsyncTask<String,Integer, List<List<HashMap<String,String>>>> {

        ProgressDialog mDialog= new ProgressDialog(TrackingOrderActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please waiting...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes= null;

            try {

                jsonObject= new JSONObject(strings[0]);

                DirectionJSONParser jsonParser= new DirectionJSONParser();

                routes = jsonParser.parse(jsonObject);

            } catch (JSONException e) {
             Log.e("Error doInBackground",e.toString());
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions= null;

            for (int i= 0; i<lists.size();i++)
            {
                points= new ArrayList();
                polylineOptions= new PolylineOptions();
                List<HashMap<String,String>> path= lists.get(i);

                for (int j=0; j<path.size();j++)
                {
                    HashMap<String,String> point= path.get(j);
                    double lat= Double.parseDouble((point.get("lat")));
                    double lng= Double.parseDouble(point.get("lng"));
                    LatLng position= new LatLng(lat,lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
         polyline=  mMap.addPolyline(polylineOptions);
        }
    }

}

