package amst.g1.labsec.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import amst.g1.labsec.R;
import amst.g1.labsec.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private final Context mContext = this;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityProfileBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();

        assert mCurrentUser != null;
        Date lastLogin = new Date(Objects.requireNonNull(mCurrentUser.getMetadata())
                .getLastSignInTimestamp());

        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss",
                Locale.getDefault());

        String testDisplayName = mCurrentUser.getDisplayName();
        String testPhoneNumber = mCurrentUser.getPhoneNumber();

        String phoneNumber = mCurrentUser.getPhoneNumber() != null ?
                mCurrentUser.getPhoneNumber() : "No phone number";

        String displayName = mCurrentUser.getDisplayName() != null ?
                mCurrentUser.getDisplayName() : "No display name";

        Uri photoUrl = mCurrentUser.getPhotoUrl();
        if (photoUrl != null)
            Picasso.get().load(photoUrl).into(binding.ivProfileImage);
        else
            binding.ivProfileImage.setImageResource(R.drawable.ic_account_circle);

        binding.tvProfileDisplayName.setText(displayName);
        binding.tvProfileEmail.setText(mCurrentUser.getEmail());
        binding.tvProfilePhoneNumber.setText(phoneNumber);
        binding.tvProfileLastLogin.setText(formatter.format(lastLogin));

        binding.fabProfileLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    private void logout() {
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
                    FirebaseDatabase.getInstance().getReference().child("tokens")
                            .child(token).removeValue();

                    mAuth.signOut();

                    GoogleSignInOptions gso = new GoogleSignInOptions
                            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail().build();
                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn
                            .getClient(mContext, gso);
                    mGoogleSignInClient.signOut();

                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
    }
}
