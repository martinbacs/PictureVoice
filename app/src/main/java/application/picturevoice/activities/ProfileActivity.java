package application.picturevoice.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import application.picturevoice.R;
import application.picturevoice.adapters.ProfileAdapter;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] mDataset = new String[101];
    //= {"hola mundo", "hello world", "hej världen"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mRecyclerView = findViewById(R.id.recyclerview);

        for (int i = 0; i < 100; i++){
            mDataset[i] = String.valueOf(i) + ". filename";
        }
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);



        // specify an adapter (see also next example)
        mAdapter = new ProfileAdapter(mDataset);
        mRecyclerView.setAdapter(mAdapter);

    }
}
