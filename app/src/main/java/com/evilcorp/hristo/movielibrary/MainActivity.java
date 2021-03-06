package com.evilcorp.hristo.movielibrary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Spinner yearSpin;
    private ArrayList<Movie> movies;
    private ListView movieList;
    CustomArrayAdapter customAdapter;
    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Api Call for Data
        final VolleyRequest volleyRequest = new VolleyRequest(this,getString(R.string.api_url));

        //Dropdown values
        ArrayList<String> years = new ArrayList<>();
        years.add(getString(R.string.spinner_title));
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1980; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);

        yearSpin = (Spinner)findViewById(R.id.spinner);
        yearSpin.setAdapter(adapter);

        //SearchBar
        SearchView searchView = (SearchView) findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Clear previous searches
                movieList.setAdapter(null);

                //Check for valid data
                String year = yearSpin.getSelectedItem().toString();
                if(query.length()<2)
                    MyAlertDialog(getString(R.string.error),getString(R.string.search_error));
                else if(year.contains(getString(R.string.spinner_title)))
                    MyAlertDialog(getString(R.string.error),getString(R.string.year_error));
                else
                progress = ProgressDialog.show(MainActivity.this, getString(R.string.progress_title),
                        getString(R.string.progress_message), true);
                volleyRequest.SendRequest(query,yearSpin.getSelectedItem().toString());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //ListView
        movieList = (ListView) findViewById(R.id.listview);
        movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CreateDialog(movies.get(position));
            }
        });

    }

    //Custom dialog to show movie details
    void CreateDialog(Movie movie){
        //Dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle(R.string.movie_details);
        ListView dialog_list = (ListView) dialog.findViewById(R.id.dialog_list);
        ArrayList<String> details = new ArrayList<>();
        details.add("Runtime: "+movie.Runtime);
        details.add("Director: "+movie.Director);
        details.add("Writer: "+movie.Writer);
        details.add("Country: "+movie.Country);
        details.add("Awards: "+movie.Awards);
        details.add("Actors: "+movie.Actors);
        details.add("Plot: "+movie.Plot);

        dialog_list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,CheckForNA(details)));
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Remove missing details
    ArrayList<String> CheckForNA(ArrayList<String> details){
        ArrayList<String> new_details = new ArrayList<>();
        for(String detail : details) {
            if (!detail.contains("N/A")) {
                new_details.add(detail);
            }
        }
        return new_details;
    }

    //Parse response to data object of type Movie
    void ParseData(String response){
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject)parser.parse(response);
        Type listType = new TypeToken<Movie>(){}.getType();
        movies = new ArrayList<>();
        Movie movie = gson.fromJson(json, listType);
        movies.add(movie);
        VisualizeData();
    }

    //Handle Data
    void VisualizeData(){
        if(movies!=null){
            if(movies.get(0).Response){
                customAdapter = new CustomArrayAdapter(this, movies);
                movieList.setAdapter(customAdapter);
            }
            else{
                Log.d("Response","No Movies found");
                MyAlertDialog("Sorry",movies.get(0).Error);
            }
        }
    }
    //Reusable alert
    void MyAlertDialog(String title, String message){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialoginterface, int i) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //HTTP Request using Volley
    public class VolleyRequest {
        private String url;
        private RequestQueue requestQueue;
        VolleyRequest(Context context, String url){
            requestQueue = Volley.newRequestQueue(context);
            this.url = url;
        }
        void SendRequest(final String title,final String year){
            // Request a string response from the provided URL.
           StringRequest jsonObjectRequest = new StringRequest
                    (Request.Method.POST, String.format(url,title,year), new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d("Response",response);
                            progress.dismiss();
                            ParseData(response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progress.dismiss();
                            error.printStackTrace();
                        }
                    });
            // Add the request to the RequestQueue.
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        }

    }
}
