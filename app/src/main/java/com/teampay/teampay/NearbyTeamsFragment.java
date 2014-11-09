package com.teampay.teampay;


import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by denis on 22/10/14.
 */
public class NearbyTeamsFragment extends Fragment {


    ListView listView;              //Team listView
    TeamsAdapter teamsAdapter;      //List Adapter
    List<Team> teams;               //Team list
    Location currentLocation;       //Current location
    OnJoinTeamListener onJoinTeamListener;  //Interface to handle team joining in UI

    public NearbyTeamsFragment(OnJoinTeamListener onJoinTeamListener){
        this.onJoinTeamListener = onJoinTeamListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_nearby_teams, container, false);
        listView = (ListView) rootView.findViewById(R.id.list_view);
        teams = new ArrayList<Team>();
        teamsAdapter = new TeamsAdapter(getActivity(), teams);
        listView.setAdapter(teamsAdapter);

        //Wait for 50m GPS accuracy and set currentLocation to the acquired location
        new AccurateLocationManager(50,getActivity(), new AccurateLocationManager.OnLocationAccurateListener() {
            @Override
            public void OnLocationAccurate(Location location) {
                currentLocation = location;
            }
        });

        //Update teams every 1 sec
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(currentLocation != null)
                    new TeamGetterTask(currentLocation, 50).execute();
            }
        }, 0, 1000);

        //Join team after selecting
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                new TeamJoinTask(teams.get(i).id).execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return rootView;
    }


    //AsyncTask to join teams
    class TeamJoinTask extends AsyncTask<Void,Void,Boolean>{


        String teamId;
        ProgressDialog progressDialog;

        public TeamJoinTask(String teamId){
            this.teamId = teamId;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, "JOINING TEAM...");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //New API uses /index.php for all calls, receives apiCall as a JSON param
            String url = Const.ROOT_URL + "/index.php";
            HttpPost httpPost = new HttpPost(url);
            try{
                JSONObject requestJson = new JSONObject();
                requestJson.put("apiCall", "join-team");
                requestJson.put("userId", getActivity().getSharedPreferences(Const.FILE_PREF,0).getString(Const.PREF_USER_ID,""));
                requestJson.put("teamId", teamId);
                httpPost.setEntity(new StringEntity(requestJson.toString()));
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String response_ = EntityUtils.toString(httpResponse.getEntity());
                JSONObject responseJson = new JSONObject(response_);
                return responseJson.getBoolean("success");
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            progressDialog.dismiss();
            if(success && onJoinTeamListener != null){
                //Notify parent to show in-team UI
                onJoinTeamListener.onJoinTeam(teamId);
            }
        }
    }


    //AsyncTask to load nearby teams
    class TeamGetterTask extends AsyncTask<Void,Void,JSONArray>{

        private Double range;
        private Location location;

        public TeamGetterTask(Location location, double range){
            this.range = range;
            this.location = location;
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            //For many reasons, currently returns all teams
            String request_url = Const.ROOT_URL + "/all-teams.php";
            HttpGet httpGet = new HttpGet(request_url);
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpGet);
                return new JSONArray(EntityUtils.toString(response.getEntity()));
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            if(jsonArray == null)
                return;

            //Refresh teams array
            teams.clear();
            for(int i = 0; i < jsonArray.length(); i++){
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Double distance = distance(jsonObject.getDouble("lat"), jsonObject.getDouble("lng"), location.getLatitude(), location.getLongitude(), 'M');
                    if(distance <= range) {
                        Team team = new Team();
                        team.name = jsonObject.getString("name");
                        team.price = jsonObject.getDouble("price");
                        team.id = jsonObject.getString("teamId");
                        teams.add(team);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            //Notify teamsListView adapter
            teamsAdapter.notifyDataSetChanged();
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        } else if(unit == 'M'){
            return  distance(lat1,lon1,lat2,lon2,'K')/1000;
        }
        return (dist);
    }
    //Converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    //Converts radians to decimal degrees
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    //Team struct
    class Team{
        String name;
        String id;
        Double price;
        boolean joinable;

        public Team(){}

        public Team(String name, String id, double price, boolean joinable){
            this.name = name;
            this.id = id;
            this.price = price;
            this.joinable = joinable;
        }

        public Team (JSONObject jsonObject) throws JSONException{
            this.name = jsonObject.getString("teamName");
            this.price = jsonObject.getDouble("price");
            this.id = jsonObject.getString("id");
            this.joinable = jsonObject.getBoolean("joinable");
        }

    }





    //teamsListView adapter
    class TeamsAdapter extends ArrayAdapter<Team>{

        List<Team> teams;

        public TeamsAdapter(Context context, List<Team> teams) {
            super(context, R.layout.list_item_team, R.id.name_tv,teams);
            this.teams = teams;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rootView;
            if(convertView == null)
                rootView = super.getView(position, convertView, parent);
            else
                rootView = convertView;
            TextView nameTv = (TextView) rootView.findViewById(R.id.name_tv);
            TextView priceTv = (TextView) rootView.findViewById(R.id.price_tv);
            nameTv.setText(teams.get(position).name);
            priceTv.setText(teams.get(position).price + "$");
            return rootView;
        }
    }




    //Interface to notify parent to show in-team UI
    interface OnJoinTeamListener{
        public void onJoinTeam(String teamId);
    }

}
