package com.teampay.teampay;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by denis on 22/10/14.
 */
public class HostTeamFragment extends Fragment{

    SharedPreferences preferences;
    EditText priceEt;
    ListView participantsListView;
    ArrayList<String> participants;
    ArrayAdapter<String> participantsAdapter;
    OnStartTeamListener onStartTeamListener;


    public HostTeamFragment(OnStartTeamListener onStartTeamListener){
        this.onStartTeamListener = onStartTeamListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_host_team, container, false);
        Button startBtn = (Button) rootView.findViewById(R.id.start_btn);
        priceEt = (EditText) rootView.findViewById(R.id.price_et);
        participantsListView = (ListView) rootView.findViewById(R.id.participants_list_view);
        preferences = getActivity().getSharedPreferences(Const.FILE_PREF, 0);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AccurateLocationManager(20, getActivity(), new AccurateLocationManager.OnLocationAccurateListener() {
                    @Override
                    public void OnLocationAccurate(Location location) {
                        new StartTeamTask(Double.parseDouble(priceEt.getText().toString()), location).execute();
                    }
                });
            }
        });


        return rootView;
    }


    class StartTeamTask extends AsyncTask<Void, Void, JSONObject>{

        private Double price;
        private Location location;
        private ProgressDialog progressDialog;

        public StartTeamTask(double price, Location location){
            this.price = price;
            this.location = location;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, "Starting team...");
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String request_url = "";
            String teamId;
            HttpPost httpPost = new HttpPost(request_url);
            try{
                User me = new User(preferences.getString(Const.PREF_NAME, ""), Double.parseDouble(preferences.getString(Const.PREF_BALANCE, "0.0")), Double.parseDouble(preferences.getString(Const.PREF_INCOME, "0.0")));
                JSONObject requestJson = me.toJson();
                requestJson.put("price", price);
                requestJson.put("longitude", location.getLongitude());
                requestJson.put("latitude", location.getLatitude());
                httpPost.setEntity(new StringEntity(requestJson.toString()));
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
                String _response = EntityUtils.toString(response.getEntity());
                JSONObject responseJson = new JSONObject(_response);
                return responseJson;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            progressDialog.dismiss();
            if(response == null)
                return;
            try {
                if (response.getBoolean("success")) {
                    if(onStartTeamListener != null){
                        onStartTeamListener.onStartTeam(response.getString("teamId"));
                    }
                }
            }catch (JSONException e){

            }
        }
    }


    interface OnStartTeamListener{
        public void onStartTeam(String teamId);
    }



}
