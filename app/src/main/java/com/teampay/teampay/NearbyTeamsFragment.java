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

    ListView listView;
    TeamsAdapter teamsAdapter;
    List<Team> teams;
    Location currentLocation;
    OnJoinTeamListener onJoinTeamListener;

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

        new AccurateLocationManager(20,getActivity(), new AccurateLocationManager.OnLocationAccurateListener() {
            @Override
            public void OnLocationAccurate(Location location) {
                currentLocation = location;
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(currentLocation != null)
                    new TeamGetterTask(currentLocation, 20).execute();
            }
        }, 0, 1000);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                new TeamJoinTask(teams.get(i).id).execute();
            }
        });

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
            String url = "http://teampay.esy.es/join-team.php";
            HttpPost httpPost = new HttpPost(url);
            try{
                JSONObject requestJson = new JSONObject();
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
                onJoinTeamListener.onJoinTeam(teamId);
            }
        }
    }


    class TeamGetterTask extends AsyncTask<Void,Void,JSONArray>{

        private Double range;
        private Location location;

        public TeamGetterTask(Location location, double range){
            this.range = range;
            this.location = location;
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            String request_url = "http://teampay.esy.es/GetTeams.php";
            HttpPost httpPost = new HttpPost(request_url);
            try{
                JSONObject requestJson = new JSONObject();
                requestJson.put("latitude", location.getLatitude());
                requestJson.put("longitude", location.getLongitude());
                requestJson.put("range", range);
                httpPost.setEntity(new StringEntity(requestJson.toString()));
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse response = httpClient.execute(httpPost);
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
            teams.clear();
            for(int i = 0; i < jsonArray.length(); i++){
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Team team = new Team();
                    team.name = jsonObject.getString("teamName");
                    team.price = jsonObject.getDouble("price");
                    team.id = jsonObject.getString("id");
                    teams.add(team);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            teamsAdapter.notifyDataSetChanged();
        }
    }

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

    interface OnJoinTeamListener{
        public void onJoinTeam(String teamId);
    }

}
