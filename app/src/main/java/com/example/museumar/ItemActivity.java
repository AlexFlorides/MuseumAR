package com.example.museumar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemActivity extends AppCompatActivity implements NotesAdapter.OnItemClickListener, HorizontalImageAdapter.OnItemClickListener, Comparator {

    ImageView mImage;
    EditText mText;
    TextView mtvName;

    Button mbuttonAddNote;
    Button mbuttonAddMedia;
    Button mbuttonDelete;
    Button mbuttonBack;

    private FirebaseStorage mStorage;

    private List<Upload> mUploads;
    private List<String> mUploadsAlbum;
    private List<String> mUploadsNotes;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabaseExhibits;
    private DatabaseReference mDatabaseAlbum;
    private DatabaseReference mDatabaseNotes;
    
    private NotesAdapter mAdapterNotes;
    private HorizontalImageAdapter mAdapterHorizontal;

    private ValueEventListener mDBListener;
    private ValueEventListener mDBListenerEx;
    private ValueEventListener mDBListenerAl;
    private ValueEventListener mDBListenerNotes;

    private  RecyclerView recycler_view_test;
    private RecyclerView recycler_view_horizontal;
    
    private TextView textViewItem;
    private TextView textViewAlbum;
    private TextView textViewNotes;

    private View vertical_line;
    private View notes_line;

    public String itemselected;

    public static final int CAMERA_PIC_REQUEST = 5;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        mImage = findViewById(R.id.item_image);
        mText = findViewById(R.id.textViewItem);
        mtvName = findViewById(R.id.text_view_item_name);

        mbuttonAddNote = findViewById(R.id.button_AddNote);
        mbuttonAddMedia = findViewById(R.id.button_AddMedia);
        mbuttonDelete = findViewById(R.id.button_Delete);
        mbuttonBack = findViewById(R.id.button_Back);

        mUploads = new ArrayList<>();
        mUploadsAlbum = new ArrayList<>();
        mUploadsNotes = new ArrayList<>();

        mStorage = FirebaseStorage.getInstance();

        textViewItem = findViewById(R.id.textViewItem);
        textViewAlbum = findViewById(R.id.text_view_album);
        textViewNotes = findViewById(R.id.textViewNotes);

        vertical_line = findViewById(R.id.vertical_line);
        //notes_line = findViewById(R.id.notes_line);

        recycler_view_test = findViewById(R.id.recycler_view_test);
        recycler_view_test.setHasFixedSize(true);
        recycler_view_test.setLayoutManager(new LinearLayoutManager(this));

        recycler_view_horizontal = findViewById(R.id.recycler_view1);
        recycler_view_horizontal.setHasFixedSize(true);
        recycler_view_horizontal.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mAdapterNotes = new NotesAdapter(ItemActivity.this, mUploadsNotes);
        mAdapterHorizontal = new HorizontalImageAdapter(ItemActivity.this, mUploadsAlbum);
        
        recycler_view_test.setAdapter(mAdapterNotes);
        recycler_view_horizontal.setAdapter(mAdapterHorizontal);
        
        mAdapterNotes.setOnItemClickListener(ItemActivity.this);
        mAdapterHorizontal.setOnItemClickListener(ItemActivity.this);

        // check is Storage permission is granted, else request from the user
        if (ContextCompat.checkSelfPermission(ItemActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(ItemActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PIC_REQUEST);

        }

        // check if network is available
        if (!isNetworkAvailable()){
            ItemActivity.this.finish();
        }

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // action on Add Note button click
        mbuttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isNetworkAvailable()){
                    Toast.makeText(ItemActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // create dialog to add note
                AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
                builder.setTitle("Add Note");

                // Set up the input
                final EditText input1 = new EditText(ItemActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(input1);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (input1.getText().toString().trim().equals("")){
                            return;
                        }
                        Toast.makeText(ItemActivity.this, input1.getText().toString().trim(), Toast.LENGTH_SHORT).show();

                        Bundle extras = getIntent().getExtras();
                        if (extras!=null){
                            final int position = extras.getInt("position");

                            mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    mUploads.clear();

                                    int count = 0;

                                    // upload added note on database
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                        Upload upload = postSnapshot.getValue(Upload.class);
                                        upload.setKey(postSnapshot.getKey());
                                        mUploads.add(upload);
                                        if (count == position) {
                                            Upload selectedItem = mUploads.get(position);
                                            final String selectedKey = selectedItem.getKey();

                                            mDatabaseNotes = FirebaseDatabase.getInstance().getReference("/Notes/"+user.getUid()+"/"+selectedKey).push().child("noteId");
                                            mDatabaseNotes.setValue(input1.getText().toString().trim());
                                        }
                                        count++;
                                    }
                                    mUploadsNotes.clear();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        // open camera when Add Photo button pressed ony when there is internet connection
        mbuttonAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isNetworkAvailable()){
                    Toast.makeText(ItemActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, CAMERA_PIC_REQUEST);
            }
        });

        // delete item on Delete button click
        mbuttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()){
                    Toast.makeText(ItemActivity.this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle extras = getIntent().getExtras();
                if (extras!=null){
                    final int position = extras.getInt("position");

                    mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            mUploads.clear();

                            int count = 0;

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                Upload upload = postSnapshot.getValue(Upload.class);
                                upload.setKey(postSnapshot.getKey());
                                mUploads.add(upload);
                                if (count == position) {
                                    Upload selectedItem = mUploads.get(position);
                                    final String selectedKey = selectedItem.getKey();

                                    postSnapshot.getRef().removeValue();
                                    Toast.makeText(ItemActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                                count++;
                            }
                            mAdapterHorizontal.notifyItemRemoved(position);
                            ItemActivity.this.finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        // on Back button click go to previous activity
        mbuttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItemActivity.this.finish();
            }
        });

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            final int position = extras.getInt("position");

            mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int count = 0;

                    mUploads.clear();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){

                        mDatabaseExhibits = FirebaseDatabase.getInstance().getReference("Exhibits");

                        final String key1 = postSnapshot.getKey();

                        final int finalCount = count;
                        mDBListenerEx = mDatabaseExhibits.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // add photo of opened item using picasso library
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                    String key2 = postSnapshot.getKey();
                                    if (key1.equals(key2)) {
                                        Upload upload = postSnapshot.getValue(Upload.class);
                                        upload.setKey(postSnapshot.getKey());
                                        mUploads.add(upload);
                                        if (finalCount == position) {
                                            Upload selectedItem = mUploads.get(position);
                                            mtvName.setText(selectedItem.getName());
                                            String info = postSnapshot.child("info").getValue().toString();
                                            mText.setText(info);
                                            Picasso.with(ItemActivity.this)
                                                    .load(selectedItem.getImageUrl())
                                                    .placeholder(R.mipmap.ic_launcher)
                                                    .fit()
                                                    .rotate(90)
                                                    .centerInside()
                                                    .into(mImage);

                                            mUploadsAlbum.clear();
                                            mAdapterHorizontal.notifyDataSetChanged();

                                            // add each photo taken on album
                                            mDatabaseAlbum = FirebaseDatabase.getInstance().getReference("/Album/"+user.getUid()+"/"+selectedItem.getKey());
                                            mDBListenerAl = mDatabaseAlbum.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                                        String imageUrl = postSnapshot.child("imageUrl").getValue(String.class);
                                                        mUploadsAlbum.add(imageUrl);
                                                    }
                                                    
                                                    if (mUploadsAlbum.isEmpty()){
                                                        recycler_view_horizontal.setVisibility(View.GONE);
                                                        textViewAlbum.setVisibility(View.GONE);
                                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                                                                textViewItem.getLayoutParams();
                                                        params.weight = 2.0f;
                                                        textViewItem.setLayoutParams(params);
                                                    }
                                                    else{
                                                        mAdapterHorizontal.notifyDataSetChanged();

                                                        recycler_view_horizontal.setVisibility(View.VISIBLE);
                                                        textViewAlbum.setVisibility(View.VISIBLE);
                                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                                                                textViewItem.getLayoutParams();
                                                        params.weight = 1.0f;
                                                        textViewItem.setLayoutParams(params);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            mUploadsNotes.clear();
                                            mAdapterNotes.notifyDataSetChanged();

                                            // add note added on notes list
                                            mDatabaseNotes = FirebaseDatabase.getInstance().getReference("/Notes/"+user.getUid()+"/"+selectedItem.getKey());
                                            mDBListenerNotes = mDatabaseNotes.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                                        String imageUrl = postSnapshot.child("noteId").getValue(String.class);
                                                        mUploadsNotes.add(imageUrl);
                                                    }

                                                    if (mUploadsNotes.isEmpty()){
                                                        recycler_view_test.setVisibility(View.GONE);
                                                        textViewNotes.setVisibility(View.GONE);
                                                        vertical_line.setVisibility(View.GONE);
                                                        //notes_line.setVisibility(View.GONE);
                                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                                                                mImage.getLayoutParams();
                                                        params.weight = 3.0f;
                                                        mImage.setLayoutParams(params);
                                                        mtvName.setLayoutParams(params);
                                                    }
                                                    else{
                                                        mAdapterNotes.notifyDataSetChanged();

                                                        recycler_view_test.setVisibility(View.VISIBLE);
                                                        textViewNotes.setVisibility(View.VISIBLE);
                                                        vertical_line.setVisibility(View.VISIBLE);
                                                        //notes_line.setVisibility(View.VISIBLE);
                                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                                                                mImage.getLayoutParams();
                                                        params.weight = 2.0f;
                                                        mImage.setLayoutParams(params);
                                                        mtvName.setLayoutParams(params);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                            break;
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        count++;
                    }
                    mAdapterHorizontal.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    // check if network is not available, show appropriate message
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // update photos of album and notes
    public void getImageID(Bundle bundle, final Uri filepath){
        final int position = bundle.getInt("position");
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    mDatabaseExhibits = FirebaseDatabase.getInstance().getReference("Exhibits");
                    final String key1 = postSnapshot.getKey();
                    mDBListenerEx = mDatabaseExhibits.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int count = 0;
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                String key2 = postSnapshot.getKey();
                                if (key1.equals(key2)) {
                                    Upload upload = postSnapshot.getValue(Upload.class);
                                    upload.setKey(postSnapshot.getKey());
                                    mUploads.add(upload);
                                    if (count == position) {
                                        itemselected = mUploads.get(position).getKey();
                                        uploadAlbum(itemselected, filepath);
                                        mAdapterHorizontal.notifyDataSetChanged();
                                        break;
                                    }
                                }
                                count++;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    // compress the photo taken and get its uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    // upload photos taken on storage
    public void uploadAlbum(final String itemId, Uri filePath){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        if(filePath != null && itemId != null)
        {
            final StorageReference ref = storageReference.child("album/" + user.getUid() + "/" + itemId + "/" + System.currentTimeMillis()
                    + "." + getFileExtension(filePath));
            ref.putFile(filePath).continueWithTask(
                        new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException(); }
                            return ref.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();

                                Toast.makeText(ItemActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                mDatabaseAlbum = FirebaseDatabase.getInstance().getReference("/Album/"+user.getUid()+"/"+itemId).push().child("imageUrl");
                                mDatabaseAlbum.setValue(downloadUri.toString());

                                mUploadsAlbum.clear();
                                mAdapterHorizontal.notifyDataSetChanged();

                            }
                            else { Toast.makeText(ItemActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ItemActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            Toast.makeText(ItemActivity.this, "SAD", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_PIC_REQUEST){

            if (resultCode == RESULT_OK){

                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri filePath = getImageUri(getApplicationContext(), imageBitmap);

                Bundle bun = getIntent().getExtras();
                getImageID(bun, filePath);

            }
        }
    }


    @Override
    public int compare(Object o, Object t1) {
        return 0;
    }

    @Override
    public void onItemClickHorizontal(int position) {
        Toast.makeText(this, "Horizontal: Normal click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    // on Delete click on action menu of album delete the selected photo
    @Override
    public void onDeleteClickHorizontal(final int position) {
        Toast.makeText(this, "Horizontal: Delete click at position: " + position, Toast.LENGTH_SHORT).show();
        final String item = mUploadsAlbum.get(position);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseAlbum = FirebaseDatabase.getInstance().getReference("/Album/"+user.getUid());
        mDBListenerAl = mDatabaseAlbum.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    for (final DataSnapshot snapchild : postSnapshot.getChildren()){
                        for (final DataSnapshot lola : snapchild.getChildren()){
                            if (lola.getValue().equals(item)){
                                StorageReference imageRef = mStorage.getReferenceFromUrl(item);
                                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDatabaseAlbum.child(postSnapshot.getKey()).child(snapchild.getKey()).child(lola.getKey()).removeValue();
                                        Toast.makeText(ItemActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                mUploadsAlbum.clear();
                                mAdapterHorizontal.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClickNotes(int position) {
        Toast.makeText(this, "Notes: Normal click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    // on Edit click on action menu of notes list edit the selected note
    @Override
    public void onEditClickNotes(int position) {
        Toast.makeText(this, "Notes: Edit click at position: " + position, Toast.LENGTH_SHORT).show();

        final String item = mUploadsNotes.get(position);
        final AlertDialog.Builder builder = new AlertDialog.Builder(ItemActivity.this);
        builder.setTitle("Add Note");

        final EditText input1 = new EditText(ItemActivity.this);
        builder.setView(input1);

        // open dialog box to enter new text and update the database
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                mDatabaseNotes = FirebaseDatabase.getInstance().getReference("/Notes/"+user.getUid());
                mDBListenerNotes = mDatabaseNotes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                            for (final DataSnapshot snapchild : postSnapshot.getChildren()){
                                for (final DataSnapshot lola : snapchild.getChildren()){
                                    if (lola.getValue().equals(item)){

                                        String note = lola.getValue().toString();

                                        builder.setMessage(note);

                                        if (input1.getText().toString().trim().equals("")){
                                            Toast.makeText(ItemActivity.this, "Can't add blank note", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        mDatabaseNotes.child(postSnapshot.getKey()).child(snapchild.getKey()).child(lola.getKey()).setValue(input1.getText().toString().trim());

                                        mUploadsNotes.clear();
                                        mAdapterNotes.notifyDataSetChanged();

                                        Toast.makeText(ItemActivity.this, "Item Edited", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // on Delete click on action menu of notes list delete the selected note
    @Override
    public void onDeleteClickNotes(int position) {
        Toast.makeText(this, "Notes: Delete click at position: " + position, Toast.LENGTH_SHORT).show();

        final String item = mUploadsNotes.get(position);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseNotes = FirebaseDatabase.getInstance().getReference("/Notes/"+user.getUid());
        mDBListenerNotes = mDatabaseNotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    for (final DataSnapshot snapchild : postSnapshot.getChildren()){
                        for (final DataSnapshot lola : snapchild.getChildren()){
                            if (lola.getValue().equals(item)){
                                mDatabaseNotes.child(postSnapshot.getKey()).child(snapchild.getKey()).child(lola.getKey()).removeValue();
                                Toast.makeText(ItemActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();

                                mUploadsNotes.clear();
                                mAdapterNotes.notifyDataSetChanged();
                            }
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