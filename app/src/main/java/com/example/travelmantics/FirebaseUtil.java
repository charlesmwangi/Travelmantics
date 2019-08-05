package com.example.travelmantics;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    //declare the firebase database and reference
    public static FirebaseDatabase mFirebaseDatabse;
    public static DatabaseReference mDatabseReference;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;
    //variables for login
    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;
    //ref the firebaseUtil
    private static FirebaseUtil firebaseUtil;
    //declare an arraylist of travel deals
    public static ArrayList<TravelDeal> mDeals;
    //variable for authority
    public static boolean isAdmin;

    private static ListActivity caller;
    private static final int RC_SIGN_IN = 123;
    //create an empty constructor to avoid it to be instantiated outside this class
    private FirebaseUtil(){}

    //create a generic static method to open the reference
    public static void openFbReference(String ref, final ListActivity callerActivity){
        //check if this method has already been called
        if(firebaseUtil == null){
            //create an instance of itself
            firebaseUtil = new FirebaseUtil();
            //get the instance of the db
            mFirebaseDatabse = FirebaseDatabase.getInstance();
            //initialize the firebaseAuth object and stateListener
            mFirebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    //check if there is a logged in user
                    if (firebaseAuth.getCurrentUser() == null){
                        //call the sign in method
                        FirebaseUtil.signIn();
                    }else{
                        //retrieve uid of the current user
                        String userId = firebaseAuth.getUid();
                        //check if its admin
                        checkAdmin(userId);
                    }

                    Toast.makeText(callerActivity.getBaseContext(), "Welcome back",Toast.LENGTH_LONG).show();

                }

                private void checkAdmin(String userId) {
                    //set isAdmin variable to false
                    FirebaseUtil.isAdmin = false;
                    //db ref to the administrators
                    DatabaseReference ref = mFirebaseDatabse.getReference().child("administrators")
                            .child(userId);
                    //set the listener
                    ChildEventListener listener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            //set isAdmin variable to true
                            FirebaseUtil.isAdmin = true;
                            //call the show menu method(make sure the caller is a listActivity
                            Log.d("Admin","You are an admin");
                            caller.showMenu();
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
                    //set the listener to the reference
                    ref.addChildEventListener(listener);
                }
            };
            //connect storage
            connectStorage();

        }
        //create a new empty arraylist of deals
        mDeals = new ArrayList<>();
        //ref the dbref
        mDatabseReference = mFirebaseDatabse.getReference().child(ref);
    }
//method to sign in
    private static void signIn(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    //method to attach the auth listener
    public static void attachListener(){
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }
    //metthod to detach the listener
    public static void detachListener (){
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }
    //connect with storage
    public static void connectStorage(){
        //get an instance of the firebase storage
        mStorage = FirebaseStorage.getInstance();
        //get the folder deal pictures
        mStorageRef = mStorage.getReference().child("deals_pictures");
    }
}
