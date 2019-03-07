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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import application.picturevoice.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    //firebase auth
    private FirebaseAuth mAuth;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

    private void createUser(){
        //show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //login data
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // sign in success, and start main activity
                            Log.d(TAG, "createUserWithEmail: success");
                            FirebaseUser user = mAuth.getCurrentUser();
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
