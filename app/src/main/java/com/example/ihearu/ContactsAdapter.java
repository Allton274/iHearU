package com.example.ihearu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private String[] dataSet;


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView listHolder;
        //TODO: Finish implementing this from Android Docs
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(itemView.getContext(), "" + getAdapterPosition(), Toast.LENGTH_SHORT).show();

                }
            });
            listHolder = itemView.findViewById(R.id.contactText);


        }

        public TextView getListHolder(){
            return listHolder;
        }
    }

    public ContactsAdapter(String[] data){
        dataSet = data;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_adapter, parent, false);
//TODO: Implement all the images and set as GONE if not in database.
        ImageView emailIcon = view.findViewById(R.id.email);



        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ContactsAdapter.ViewHolder holder, int position) {
        holder.getListHolder().setText(dataSet[position]);

    }

    @Override
    public int getItemCount() {
        return dataSet.length;
    }
}
