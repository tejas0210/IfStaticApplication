package com.example.ifstaticapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ifstaticapplication.databinding.ActivityMainBinding;
import com.example.ifstaticapplication.model.ApiResponse;
import com.example.ifstaticapplication.model.Restaurant;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements LocationListener {

    ActivityMainBinding binding;
    private LocationManager locationManager;
    private ScrollView scrollView;
    private LinearLayout restaurantLayout;
    private ApiService apiService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //For status bar color
        Window window = MainActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.black));

        scrollView = findViewById(R.id.scrollView);
        restaurantLayout = findViewById(R.id.restaurantLayout);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://theoptimiz.com/restro/public/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Call the method to fetch restaurants
        fetchRestaurants();


        grantUriPermission();
        checkLocationEnabledOrNot();
        getLocation();
    }

    private void fetchRestaurants() {
        double lat = 25.22;
        double lng = 45.32;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("lat", String.valueOf(lat))
                .addFormDataPart("lng", String.valueOf(lng))
                .build();

        Call<ApiResponse> call = apiService.getRestaurants(requestBody);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.getData() != null) {
                        List<Restaurant> restaurants = apiResponse.getData();
                        displayRestaurants(restaurants);
                    }
                } else {
                    // Handle error
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void displayRestaurants(List<Restaurant> restaurants) {
        restaurantLayout.removeAllViews();

        for (Restaurant restaurant : restaurants) {
            View restaurantView = LayoutInflater.from(this).inflate(R.layout.restaurant_item, null);

            ImageView imageView = restaurantView.findViewById(R.id.imageView);
            TextView nameTextView = restaurantView.findViewById(R.id.nameTextView);
            TextView tagsTextView = restaurantView.findViewById(R.id.tagsTextView);
            TextView ratingTextView = restaurantView.findViewById(R.id.ratingTextView);
            TextView discountTextView = restaurantView.findViewById(R.id.discountTextView);
            TextView distanceTextView = restaurantView.findViewById(R.id.distanceTextView);

            Picasso.get().load(restaurant.getPrimaryImage()).into(imageView);
            nameTextView.setText(restaurant.getName());
            tagsTextView.setText(restaurant.getTags());
            ratingTextView.setText(String.valueOf(restaurant.getRating()));
            discountTextView.setText(String.valueOf(restaurant.getDiscount()));
            distanceTextView.setText(String.valueOf(restaurant.getDistance()));

            restaurantLayout.addView(restaurantView);
        }
    }


    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void checkLocationEnabledOrNot() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if(!gpsEnabled && !networkEnabled){
            new AlertDialog.Builder(MainActivity.this).setTitle("Enable GPS Service").setCancelable(false).setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("Cancel",null).show();
        }
    }

    private void grantUriPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);

        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

            binding.txtLocation.setText(addresses.get(0).getSubLocality()+", "+addresses.get(0).getLocality());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }


}