package application.picturevoice.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import application.picturevoice.R;
import application.picturevoice.classes.CloudFile;
import application.picturevoice.listadapters.ProfileAdapter;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private Button btnDloadFile, btnDeleteFile;
    private EditText editTextFile;
    private TextView textViewFile;

    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private CloudFile[] mDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //init database
        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference("Users/" + mAuth.getUid() + "/files");

        //init cloud storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //init ui
        mRecyclerView = findViewById(R.id.recyclerview);
        btnDloadFile = findViewById(R.id.btnDload);
        btnDeleteFile = findViewById(R.id.btnDelete);
        editTextFile = findViewById(R.id.editTextFile);
        textViewFile = findViewById(R.id.textViewFile);


        //get files from database
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Count ", "" + dataSnapshot.getChildrenCount());
                mDataset = new CloudFile[Math.toIntExact(dataSnapshot.getChildrenCount())];
                int i = 0;

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CloudFile post = postSnapshot.getValue(CloudFile.class);
                    mDataset[i] = post;
                    i++;
                    Log.e("filename: ", post.getFileName());
                    Log.e("size: ", post.getFileSize());
                }

                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                mRecyclerView.setHasFixedSize(true);

                // use a linear layout manager
                mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mRecyclerView.setLayoutManager(mLayoutManager);


                // specify an adapter (see also next example)
                mAdapter = new ProfileAdapter(mDataset);
                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btnDloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference ref = mStorageRef.child(mAuth.getUid() + "/00NoEntryWhenRed-1.jpg");

                try {
                    final File localFile = File.createTempFile("images", "jpg");
                    ref.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Successfully downloaded data to local file
                                    // ...
                                    textViewFile.setText(localFile.getName());
                                    Toast.makeText(getApplicationContext(), "file successfuly downloaded", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            // ...
                            Toast.makeText(ProfileActivity.this, "file download failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        btnDeleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fileName = editTextFile.getText().toString();
                StorageReference ref = mStorageRef.child(mAuth.getUid() + "/" + fileName);

                // Delete the file
                ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        Toast.makeText(getApplicationContext(), fileName + " successfully deleted", Toast.LENGTH_SHORT).show();
                        //TODO: remove deleted file from list and update ui
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Toast.makeText(getApplicationContext(), "failed to delete file", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}
