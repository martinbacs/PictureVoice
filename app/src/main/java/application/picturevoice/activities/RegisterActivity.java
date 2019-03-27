package application.picturevoice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import application.picturevoice.R;
import application.picturevoice.classes.User;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    // ui references
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = FirebaseDatabase.getInstance();

        //init firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        //init gui
        mSignUpButton = findViewById(R.id.email_sign_in_button2);
        mEmailView = findViewById(R.id.email);
        mProgressBar = findViewById(R.id.login_progress);
        mProgressBar.setVisibility(View.GONE);
        mPasswordView = (EditText) findViewById(R.id.password);


        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
    }


    private void createUser() {
        // show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        // user data
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // write new user to database
                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference ref = database.getReference("Users/" + user.getUid());
                            User u = new User(user.getUid(), user.getEmail());
                            ref.setValue(u);

                            // sign in success, and start main activity
                            Log.d(TAG, "createUserWithEmail: success");
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            // if sign in fails, display a message to the user
                            Log.w(TAG, "createUserWithEmail: failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
