package com.evilcorp.hristo.movielibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Custom adapter for displaying an array of Planet objects. */
public class CustomArrayAdapter extends ArrayAdapter<Movie> {

    private LayoutInflater inflater;
    private List<Movie> movies;

    public CustomArrayAdapter(Context context, List<Movie> movies) {
        //super( context, R.layout.step_item, R.id.step_view, arrayList );
        super(context,R.layout.list_item,movies);
        // Cache the LayoutInflate to avoid asking for a new one each time.
        inflater = LayoutInflater.from(context) ;
        this.movies = movies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Find the child views.
        TextView titleText = (TextView) convertView.findViewById( R.id.titleText );
        TextView yearText = (TextView) convertView.findViewById( R.id.yearText );
        TextView genreText = (TextView) convertView.findViewById( R.id.genreText );
        ImageView moviePoster = (ImageView) convertView.findViewById( R.id.moviePoster );

        // Set the value of each cell
        titleText.setText(movie.Title);
        yearText.setText(movie.Year);
        genreText.setText(movie.Genre);

        // For the Image , convert to bitmap
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream)new URL(movie.Poster).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        moviePoster.setImageBitmap(bitmap);

        return convertView;
    }
}
