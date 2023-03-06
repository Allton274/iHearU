package com.example.ihearu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private final String[] dataSet;
    private final ArrayList<Contact> contactsData;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView listHolder;
        //TODO: Finish implementing this from Android Docs
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            listHolder = itemView.findViewById(R.id.contactText);

        }

        public TextView getListHolder(){
            return listHolder;
        }
    }

    public ContactsAdapter(String[] data, ArrayList<Contact> contacts){
        dataSet = data;
        contactsData = contacts;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_adapter, parent, false);



        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {
        holder.getListHolder().setText(dataSet[position]);
        Contact contact = contactsData.get(position);

        if(!contact.doText){
            holder.itemView.findViewById(R.id.message).setVisibility(View.GONE);
        }
        if(!contact.doEmail){
            holder.itemView.findViewById(R.id.email).setVisibility(View.GONE);
        }

        if(!contact.doCall)
            holder.itemView.findViewById(R.id.call).setVisibility(View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), EditContactActivity.class);
                intent.putExtra("contact", contactsData.get(holder.getAdapterPosition()));
                view.getContext().startActivity(intent);


            }
        });



    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }
}
