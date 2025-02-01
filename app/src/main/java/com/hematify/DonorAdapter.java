package com.hematify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {
    private final List<User> donorList;
    private final Context context;
    private final OnRequestClickListener requestClickListener;

    public interface OnRequestClickListener {
        void onRequestClick(User donor);
    }

    public DonorAdapter(Context context, List<User> donorList, OnRequestClickListener listener) {
        this.context = context;
        this.donorList = donorList;
        this.requestClickListener = listener;
    }


    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.donor_item, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        User donor = donorList.get(position);

        if (donor != null) {
            holder.donorName.setText(donor.getFirst_name() + " " + donor.getLast_name());
            holder.donorLocation.setText(donor.getLocation());
            holder.bloodGroup.setText(donor.getBlood_group());
            holder.phoneNo.setText(donor.getPhone_no());
        } else {
            Log.d("DonorAdapter", "Donor at position " + position + " is null");
        }


        // Load image or generate initials if no image
        if (donor.getProfileImageBase64() != null && !donor.getProfileImageBase64().isEmpty()) {
            byte[] decodedString = Base64.decode(donor.getProfileImageBase64(), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.donorImage.setImageBitmap(decodedBitmap);
        } else {
            String initials = getInitials(donor.getFirst_name(), donor.getLast_name());
            int randomColor = getRandomLightColor();
            holder.donorImage.setImageBitmap(generateInitialsBitmap(initials, randomColor));
        }
        holder.itemView.setOnClickListener(v -> requestClickListener.onRequestClick(donor));
    }

    @Override
    public int getItemCount() {
        return donorList.size();
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        TextView donorName, donorLocation, bloodGroup, phoneNo;
        ImageView donorImage;
        View requestButton;

        public DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            donorName = itemView.findViewById(R.id.donorName);
            donorLocation = itemView.findViewById(R.id.donor_location);
            bloodGroup = itemView.findViewById(R.id.blood_G);
            donorImage = itemView.findViewById(R.id.donor_img);
            phoneNo=itemView.findViewById(R.id.phone_no);
        }
    }

    private String getInitials(String firstName, String lastName) {
        if (firstName != null && lastName != null) {
            return (firstName.substring(0, 1) + lastName.substring(0, 1)).toUpperCase();
        }
        return "?";
    }

    private int getRandomLightColor() {
        Random random = new Random();
        int red = 150 + random.nextInt(106);
        int green = 150 + random.nextInt(106);
        int blue = 150 + random.nextInt(106);
        return Color.rgb(red, green, blue);
    }

    private Bitmap generateInitialsBitmap(String initials, int bgColor) {
        int size = 200;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(bgColor);
        canvas.drawCircle(size / 2, size / 2, size / 2, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(64);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect textBounds = new Rect();
        paint.getTextBounds(initials, 0, initials.length(), textBounds);
        float x = size / 2;
        float y = (size / 2) - textBounds.exactCenterY();
        canvas.drawText(initials, x, y, paint);
        return bitmap;
    }
}
