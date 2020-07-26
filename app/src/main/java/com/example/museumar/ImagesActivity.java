package com.example.museumar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.text.DateFormat.getDateTimeInstance;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener, AdapterView.OnItemSelectedListener, Comparator {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    private DatabaseReference mDatabaseExhibits;

    private List<Upload> mUploads;
    public List<String> mList;

    private Spinner date_dropdown_month;
    private Spinner date_dropdown_day;

    private String month = "All";
    private String day = "All";

    private Button mButtonScan;
    private Button mButtonLogOut;

    private TextView mtvWelcome;
    private TextView tvItem;

    private String targetName;

    public static final int REQUEST_ID_CAMERA= 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        mRecyclerView = findViewById(R.id.recycler_view1);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressCircle = findViewById(R.id.progress_circle);
        tvItem = findViewById(R.id.tvItems);

        mUploads = new ArrayList<>();
        mList = new ArrayList<>();

        mAdapter = new ImageAdapter(ImagesActivity.this, mUploads);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ImagesActivity.this);

        mStorage = FirebaseStorage.getInstance();

        date_dropdown_month = findViewById(R.id.date_dropdown_month);
        date_dropdown_month.setSelection(0);
        date_dropdown_month.setOnItemSelectedListener(ImagesActivity.this);

        date_dropdown_day = findViewById(R.id.date_dropdown_day);
        date_dropdown_day.setSelection(0);

        month = date_dropdown_month.getSelectedItem().toString();
        day = date_dropdown_day.getSelectedItem().toString();

        mButtonScan = findViewById(R.id.scan_button);
        mButtonLogOut = findViewById(R.id.logout_button);

        mtvWelcome = findViewById(R.id.tvWelcome);

        // check is Camera permission is granted, else request from the user
        if (ContextCompat.checkSelfPermission(ImagesActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(ImagesActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_ID_CAMERA);

        }

        // check if network is not available, show appropriate message
        if (!isNetworkAvailable()){
            mProgressCircle.setVisibility(View.INVISIBLE);
            tvItem.setVisibility(View.VISIBLE);
            tvItem.setText("No internet connection!");
        }

        // on button scan open ArchitectActivity
        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ImagesActivity.this, ArchitectActivity.class));
            }
        });

        // on button logout, sign user out
        mButtonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Logout
                AuthUI.getInstance()
                        .signOut(ImagesActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                mButtonLogOut.setEnabled(false);
                                startActivity(new Intent(ImagesActivity.this, Login.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ImagesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
            }
        });


        //Get User
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        System.out.println(user + ", " + mDatabaseRef.getKey());
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {

            // set the name of each object
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                        String name = postSnapshot.child("name").getValue().toString();
                        setName(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // get targetName that was send from other activity
        Bundle extras = getIntent().getExtras();
        if (extras!=null) {
            targetName = extras.getString("targetName");
            System.out.println(targetName + ", " + getName());
            mDatabaseExhibits = FirebaseDatabase.getInstance().getReference("Exhibits");
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUploads.clear();
                    mAdapter.notifyDataSetChanged();
                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (targetName != null) {
                            if (!getName().contains(targetName)) {
                                mDatabaseExhibits.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot exSnapshot : dataSnapshot.getChildren()) {
                                            // show details of each object in scrapbook
                                            if (targetName.equals(exSnapshot.child("name").getValue().toString())) {
                                                mDatabaseRef.child(exSnapshot.getKey()).child("name").setValue(targetName);
                                                mDatabaseRef.child(exSnapshot.getKey()).child("imageUrl").setValue(exSnapshot.child("imageUrl").getValue());
                                                mDatabaseRef.child(exSnapshot.getKey()).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                Toast.makeText(ImagesActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    // method checking if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // on click on image of object open ItemActivity only if there is internet connection
    @Override
    public void onItemClick(int position) {
         Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();

         if (!isNetworkAvailable()){
             Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
         }
         else {
             Intent inte = new Intent(ImagesActivity.this, ItemActivity.class);
             inte.putExtra("position", position);
             startActivity(inte);
         }
    }

    // on Open click on action menu opens ItemActivity only if there is internet connection
    @Override
    public void onOpenClick(int position) {
        Toast.makeText(this, "Open click at position: " + position, Toast.LENGTH_SHORT).show();

        if (!isNetworkAvailable()){
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent inte = new Intent(ImagesActivity.this, ItemActivity.class);
            inte.putExtra("position", position);
            startActivity(inte);
        }
    }

    // on Delete click on action menu deleted the selected item of scrapbook
    // only if there is internet connection
    @Override
    public void onDeleteClick(final int position) {
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        if (!isNetworkAvailable()){
            Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }
        else {
            mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        if (postSnapshot.getKey().equals(selectedKey)) {
                            postSnapshot.getRef().removeValue();
                            Toast.makeText(ImagesActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    mUploads.remove(position);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    public List<String> getName() {
        return mList;
    }

    public void setName(String name) {
        mList.add(name);
    }

    // sort scrapbook items by day, month or both based on the dropdown menus
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

        mUploads.clear();
        mAdapter.notifyDataSetChanged();

        date_dropdown_day.setOnItemSelectedListener(ImagesActivity.this);

        // get the selected month
        if (adapterView.getId() == R.id.date_dropdown_month) {
            month = adapterView.getItemAtPosition(position).toString();
        }
        // get the selected day
        if (adapterView.getId() == R.id.date_dropdown_day) {
            day = adapterView.getItemAtPosition(position).toString();
        }

        Toast.makeText(adapterView.getContext(),
                "OnItemSelectedListener : " + day + " " + month,
                Toast.LENGTH_SHORT).show();

        //Get User
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        // show tha name of the logged-in user
        if (user.isAnonymous()){
            mtvWelcome.setText("Welcome Guest!");
        }
        else if (user.getDisplayName()==null) {
            mtvWelcome.setText("Welcome " + user.getEmail() + "!");
        }
        else {
            mtvWelcome.setText("Welcome " + user.getDisplayName() + "!");
        }

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUploads.clear();

                if (!dataSnapshot.hasChildren()){
                    mProgressCircle.setVisibility(View.INVISIBLE);
                    return;
                }

                // notify user when there are no any items on the selected date
                if (dataSnapshot.getValue() == null){
                    mProgressCircle.setVisibility(View.INVISIBLE);

                    tvItem.setVisibility(View.VISIBLE);
                    tvItem.setText("No items at the selected date!");
                }
                else {

                    tvItem.setVisibility(View.GONE);
                    mDatabaseExhibits = FirebaseDatabase.getInstance().getReference("Exhibits");
                    mUploads.clear();

                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                        if (!postSnapshot.hasChild("timestamp")){
                            mUploads.clear();
                            mAdapter.notifyDataSetChanged();
                            break;
                        }

                        // calculate the date of each item
                        Long date = (Long) postSnapshot.child("timestamp").getValue();
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        String dateString = formatter.format(new Date(date));

                        final String itemDay = dateString.substring(0, 2);
                        String itemMonth = dateString.substring(3, 5);

                        itemMonth =  new DateFormatSymbols().getMonths()[Integer.parseInt(itemMonth)-1];

                        final String finalItemMonth = itemMonth;

                        // sort based on the selections
                        if (day.equals("All") && month.equals("All")){
                            tvItem.setVisibility(View.GONE);
                            Upload upload = postSnapshot.getValue(Upload.class);
                            upload.setKey(postSnapshot.getKey());
                            mUploads.add(upload);
                        }
                        else if (day.equals(itemDay) && (month.equals(finalItemMonth))) {
                            tvItem.setVisibility(View.GONE);
                            Upload upload = postSnapshot.getValue(Upload.class);
                            upload.setKey(postSnapshot.getKey());
                            mUploads.add(upload);
                        }
                        else if (day.equals("All") && (month.equals(finalItemMonth))) {
                            tvItem.setVisibility(View.GONE);
                            Upload upload = postSnapshot.getValue(Upload.class);
                            upload.setKey(postSnapshot.getKey());
                            mUploads.add(upload);
                        }
                        else if (day.equals(itemDay) && (month.equals("All"))) {
                            tvItem.setVisibility(View.GONE);
                            Upload upload = postSnapshot.getValue(Upload.class);
                            upload.setKey(postSnapshot.getKey());
                            mUploads.add(upload);
                        }
                        else if (mUploads.isEmpty()) {
                            tvItem.setVisibility(View.VISIBLE);
                            tvItem.setText("No items at the selected date!!");
                        }
                    }
                    System.out.println("---------------------------------------------------------------------------------------------------");
                    mAdapter.notifyDataSetChanged();
                    mProgressCircle.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    @Override
    public int compare(Object o, Object t1) {
        return 0;
    }
}
