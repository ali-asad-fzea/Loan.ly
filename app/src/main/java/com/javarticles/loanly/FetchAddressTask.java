package com.javarticles.loanly;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//URL of Documentation
//https://developer.android.com/codelabs/advanced-android-training-device-location#4

class FetchAddressTask extends AsyncTask<Location, Void, String> {

    private Context mContext;
    private OnTaskCompleted mListener;



    FetchAddressTask(Context applicationContext, OnTaskCompleted listener) {
        mContext = applicationContext;
        mListener = listener;
    }


    @Override
    protected String doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext,
                Locale.getDefault());
        Location location = params[0];
        List<Address> addresses = null;
        String resultMessage = "";
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address
                    1);
        }
        catch (IOException ioException) {
            // Catch network or other I/O problems
            Log.e("Address error","Failed to fetch address");
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values
            Log.e("Coordinate error",   "Invalid coordinates were supplied " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            Log.d("No address","No address were found");
        }
        else{
            // If an address is found, read it into resultMessage
            Address address = addresses.get(0);
            ArrayList<String> addressParts = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressParts.add(address.getAddressLine(i));
            }

            resultMessage = TextUtils.join("\n", addressParts);

        }


        return resultMessage;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onTaskCompleted(address);
        super.onPostExecute(address);
    }

    interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }

}
