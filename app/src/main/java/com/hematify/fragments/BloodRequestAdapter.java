package com.hematify.fragments;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.hematify.User;
import com.hematify.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.BloodRequestViewHolder> {

    private Context context;
    private List<BloodRequest> bloodRequests;

    public BloodRequestAdapter(Context context, List<BloodRequest> bloodRequests) {
        this.context = context;
        this.bloodRequests = bloodRequests;
    }

    @NonNull
    @Override
    public BloodRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_blood_request, parent, false);
        return new BloodRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodRequestViewHolder holder, int position) {
        BloodRequest request = bloodRequests.get(position);
        holder.userName.setText(request.getPatientName());
        holder.userAddress.setText(request.getLocation());
        holder.bloodType.setText(request.getBloodType());
        holder.phoneNo.setText(request.getPhone_no());
        // Handle the deadline
        Timestamp deadline = request.getDeadline();
        if (deadline != null) {
            Date date = deadline.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(date);
            holder.lastdate.setText(formattedDate); // Show the formatted date
        }
    }

    @Override
    public int getItemCount() {
        return bloodRequests.size();
    }

    public class BloodRequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userAddress;
        TextView bloodType;
        TextView phoneNo;
        TextView lastdate;

        public BloodRequestViewHolder(View itemView) {
            super(itemView);
            userName=itemView.findViewById(R.id.userName);
            userAddress = itemView.findViewById(R.id.userAddress);
            bloodType = itemView.findViewById(R.id.bloodType);
            phoneNo=itemView.findViewById(R.id.phone_no);
            lastdate=itemView.findViewById(R.id.until);
        }
    }
}
