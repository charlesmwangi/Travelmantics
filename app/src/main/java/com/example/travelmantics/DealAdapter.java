package com.example.travelmantics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder>{
    //deeclare variables
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private Context context;
    private ImageView imageDeal;

    //create a public constructor
    public DealAdapter(){

        //ref the FirebaseUtil class
        //FirebaseUtil.openFbReference("traveldeals");
        //populate firebase database and reference
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabse;
        mDatabaseReference = FirebaseUtil.mDatabseReference;
        //ref the list of deals
        deals = FirebaseUtil.mDeals;
        //create a new ChildEventListener
        mChildListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //retrieve all travel deals and display them
                //create a travel deal using the datasnapshot
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                 Log.d("Deal: ", td.getTitle());
                 //set the id of the deal to the push id
                td.setId(dataSnapshot.getKey());
                 //add to deals array the item that was passed
                deals.add(td);
                //notify the observer when an item has been inserted
                notifyItemInserted(deals.size()-1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        //set the listener to the database reference
        mDatabaseReference.addChildEventListener(mChildListener);
    }
    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //get the context
        context = parent.getContext();
        //call the inflate method from the context
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.rv_row, parent, false);
        return new DealViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        //get deal at current position
        TravelDeal deal = deals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        //get the number of deals in the list
        return deals.size();
    }

    //create the viewHolder
    public class DealViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        //declare variable
        TextView tvTitle,tvDescription,tvPrice;
        //a constructor to define how to bind the data to the view
        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            //ref the text view
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imageDeal = itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener(this);
        }
        public void bind(TravelDeal deal){
            //set the values of the deal
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            //get position of clicked item
            int position = getAdapterPosition();
            Log.d("click", String.valueOf(position));
            //get the travel deal selected
            TravelDeal selectedDeal = deals.get(position);
            //create an intent
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            //add details of the travel deal and since we cannot pass a
            // complex object we implement the serializable class in the TravelDeal class
            intent.putExtra("Deal", selectedDeal);
            //start activity from the context
            view.getContext().startActivity(intent);

        }
        private void showImage(String url) {
            if (url != null && url.isEmpty()==false) {
                Picasso.with(imageDeal.getContext())
                        .load(url)
                        .resize(160, 160)
                        .centerCrop()
                        .into(imageDeal);
            }
        }
    }
}
