package amst.g1.labsec.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

import amst.g1.labsec.R;
import amst.g1.labsec.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN = 123;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();

        mAuth = FirebaseAuth.getInstance();
        binding.fabLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        binding.fabGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLogin();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser, false);
    }

    private void updateUI(FirebaseUser currentUser, final boolean withRegister) {
        if (currentUser != null) {
            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Instance", "getInstanceId failed",
                                    task.getException());
                            return;
                        }
                        String token = Objects.requireNonNull(task.getResult()).getToken();
                        if (withRegister)
                            sendRegistrationToServer(token);
                        Intent intent = new Intent(getApplicationContext(),
                                LabsListActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        }
    }

    private void googleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                if (e.getStatusCode() == 12501)
                    Toast.makeText(mContext, "No google account selected",
                            Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean showError = true;
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (Objects.requireNonNull(singleSnapshot.getValue()).toString()
                            .equals(acct.getEmail())) {
                        binding.pbLogin.setVisibility(View.VISIBLE);
                        showError = false;
                        AuthCredential credential = GoogleAuthProvider.getCredential(
                                acct.getIdToken(), null);
                        mAuth.signInWithCredential(credential).addOnCompleteListener(
                                (Activity) mContext,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        updateUI(user, true);
                                    } else {
                                        System.out.println("error");
                                    }
                                }
                            });
                    }
                }
                if (showError) {
                    Toast.makeText(mContext, "User has no access", Toast.LENGTH_SHORT).show();
                    mGoogleSignInClient.signOut();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void login() {
        String mail = Objects.requireNonNull(binding.etLoginEmail.getText()).toString();
        String password = Objects.requireNonNull(binding.etLoginPassword.getText()).toString();

        if (mail.isEmpty()) {
            binding.etLoginEmail.setError("This field is required");
        }

        if (password.isEmpty()) {
            binding.etLoginPassword.setError("This field is required");
        }

        if (mail.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter your crendentials",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        binding.fabLogin.setEnabled(false);
        binding.pbLogin.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(mail, password)
            .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user, true);
                    binding.fabLogin.setEnabled(true);
                    binding.pbLogin.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (Objects.equals(e.getMessage(), "An internal error has occurred. [ 7: ]")) {
                        Toast.makeText(getApplicationContext(), "No internet connection",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please check your credentials",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null, false);
                    }
                    binding.fabLogin.setEnabled(true);
                    binding.pbLogin.setVisibility(View.GONE);
                }
            })
            .addOnCanceledListener(this, new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.v("CANCELED", "CANCELED");
                }
            });
    }

    private void sendRegistrationToServer(String token) {
        FirebaseDatabase.getInstance().getReference().child("tokens").child(token).setValue(true);
        Toast.makeText(getApplicationContext(), "token registered",
                Toast.LENGTH_SHORT).show();
    }


}
