package com.example.myapplication.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;


public class SearchHistory extends RecyclerView.Adapter<SearchHistory.ViewHolder> {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistory.ViewHolder holder, int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        private TextView locationName, temperature, weatherName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            locationName = itemView.findViewById(R.id.locationName);
        temperature = itemView.findViewById(R.id.temperature);
          weatherName = itemView.findViewById(R.id.weatherName);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
