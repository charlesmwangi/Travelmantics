package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    //deeclare variables
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //get menu inflator object to use the insert deal menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        //create the insert option menu
        MenuItem insertMenu = menu.findItem(R.id.insert_menu);
        //show it to admin only
        if(FirebaseUtil.isAdmin == true){
            insertMenu.setVisible(true);
        }
        else {
            insertMenu.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //check the menu item selected
        switch (item.getItemId()){
            case R.id.insert_menu:
                //send an intent
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User Logged Out");
                                FirebaseUtil.attachListener();

                            }


                        });
                FirebaseUtil.detachListener();
                return true;

        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //detch AuthListener
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //call the firebase utility class
        FirebaseUtil.openFbReference("traveldeals", this);
        //ref the recyclerView
        RecyclerView rvDeals = findViewById(R.id.rvDeals);
        //declare the dealAdapter
        final DealAdapter adapter = new DealAdapter();
        //set the adapter on the recycler view
        rvDeals.setAdapter(adapter);
        //Declare a LinearLayout manager
        LinearLayoutManager dealsLayoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL,false);
        //set the LayoutManager on the recycler view
        rvDeals.setLayoutManager(dealsLayoutManager);
        //attach AuthListener
        FirebaseUtil.attachListener();
    }
    //create a method to hide field used by admin only
    public void showMenu(){
        //call the invalidate method
        invalidateOptionsMenu();
    }
}
