package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.classes.ForecastArea;
import com.example.myapplication.classes.ForecastLoc;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


    ArrayList<ForecastLoc> forecasts ;



    public MyAdapter( ArrayList<ForecastLoc>  forecasts){
        this.forecasts = forecasts;

    }
    @NonNull
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.ViewHolder holder, int position) {
        holder.txtname.setText(forecasts.get(position).getName());
        holder.txtsub.setText(forecasts.get(position).getWeather().toString());
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    public class ViewHolder  extends  RecyclerView.ViewHolder{

        TextView txtname, txtsub;
        public ViewHolder(View view) {
            super(view);
            txtname = (TextView) view.findViewById(R.id.txtname);
            txtsub = (TextView) view.findViewById(R.id.txtsub);
        }
    }
}
