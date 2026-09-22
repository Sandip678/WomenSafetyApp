package com.womensefty.womensafetyapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private List<DashboardActivity.Contact> contactList;

    public ContactAdapter(List<DashboardActivity.Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        DashboardActivity.Contact contact = contactList.get(position);
        holder.contactName.setText(contact.getName());
        holder.contactNumber.setText(contact.getNumber());

        if (contact.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(contact.getImage(), 0, contact.getImage().length);
            holder.contactImage.setImageBitmap(bitmap);
        } else {
            holder.contactImage.setImageResource(R.drawable.ic_image);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView contactName;
        TextView contactNumber;
        ImageView contactImage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            contactImage = itemView.findViewById(R.id.contactImage);
        }
    }
}