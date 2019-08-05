package com.example.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    //define variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    TravelDeal deal;
    ImageView imageView;
    private static final int PICTURE_RESULT = 42; //the answer to everything

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        //ref the FirebaseUtil class
        //FirebaseUtil.openFbReference("traveldeals", this);
        //populate firebase database and reference
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabse;
        mDatabaseReference = FirebaseUtil.mDatabseReference;
        //ref the edittext views
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        //get the intent received
        Intent intent = getIntent();
        //retrieve the deal that was passed
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        //check if the deals is there
        if(deal == null){
            //create a new travel deal instance
            deal = new TravelDeal();
        }
        //put the deal variable into the deal member
        this.deal = deal;
        //set the values of the view
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        imageView = findViewById(R.id.image);
        showImage(deal.getImageUrl());
        //ref the upload image button
        Button btnImage = findViewById(R.id.btnImage);
        //set onClickListener
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create new intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"),PICTURE_RESULT);
            }
        });

    }
    //override method to save when user clicks on the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                //save travel deal
                saveDeal();
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                //clean method to reset the content of the edittext views
                clean();
                //show list of deals
                backToList();
                return true;
            case R.id.delete_menu:
                //delete deal
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                //show list of deals
                backToList();
                return true;
            //set the default value
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    //override method to show the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        //check if its admin
        if (FirebaseUtil.isAdmin){
            //show delete and save menu
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            //make the text view editable
            enableEditText(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }
        else {
            //hide the two menu items
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            //make the text view not editable
            enableEditText(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }


@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
        Uri imageUri = data.getData();
        final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
        ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String pictureName = taskSnapshot.getStorage().getPath();
                deal.setImageName(pictureName);
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        deal.setImageUrl(uri.toString());
                        showImage(uri.toString());
                    }
                });
            }
        });
    }
}

    private void clean() {
        //reset the values of the views
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }

    private void saveDeal() {
        //save the content of the edittext views
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        //check if its a new deal
        if (deal.getId()==null){
            //insert data to the DB
            mDatabaseReference.push().setValue(deal);
        }
        //else if the deal already exit
        else{
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }

    }
    //delete the deal
    private void deleteDeal(){
        //check if the deal exist
        if (deal == null){
            //give an error message to the user
            Toast.makeText(this,"Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        //otherwise get the ref of the current deal
        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }
    }
    //back to list
    private void backToList (){
        //go back to the list activity
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
    //check if its admin
    private void enableEditText(boolean isEnabled){
        //set enable on the views
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }
    private void showImage (String url){
        if(url !=null && url.isEmpty() == false){
            //get size of the screen
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            //format the image
            Picasso.with(this)
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }
    }
