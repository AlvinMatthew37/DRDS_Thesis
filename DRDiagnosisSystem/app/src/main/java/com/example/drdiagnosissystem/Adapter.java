package com.example.drdiagnosissystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    Context context;
    ArrayList<Result> list;

    public Adapter(Context context, ArrayList<Result> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.historycard, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Result res = list.get(position);
        String base64 = res.getImage();
        byte[] image = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

        holder.retina.setImageBitmap(bitmap);
        holder.diagnosis.setText(res.getStage());
        holder.date.setText(res.getDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView retina;
        TextView diagnosis, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            retina = itemView.findViewById(R.id.retina);
            diagnosis = itemView.findViewById(R.id.diagnosis);
            date = itemView.findViewById(R.id.date);
        }
    }
}
