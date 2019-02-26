package application.picturevoice.activities;


import android.content.Intent;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;


import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import application.picturevoice.R;


public class LoginActivity extends AppCompatActivity {

    //    <color name="colorbg">#27497f</color>
    private static final String TAG = LoginActivity.class.getSimpleName();

    //firebase auth
    private FirebaseAuth mAuth;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //init firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        //init gui
        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailView = findViewById(R.id.email);
        mProgressBar = findViewById(R.id.login_progress);
        mProgressBar.setVisibility(View.GONE);
        mPasswordView = (EditText) findViewById(R.id.password);

        //init login form
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }


    private void attemptLogin() {
        //show progress bar
        mProgressBar.setVisibility(View.VISIBLE);

        //login data
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Intent intent = new Intent();
                            //intent.putExtra("user", user.getDisplayName());
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed, please try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

}

