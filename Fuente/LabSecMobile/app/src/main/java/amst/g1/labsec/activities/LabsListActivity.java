package amst.g1.labsec.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

import amst.g1.labsec.R;
import amst.g1.labsec.databinding.ActivityLabsListBinding;
import amst.g1.labsec.models.Lab;
import amst.g1.labsec.viewholders.LabViewHolder;

public class LabsListActivity extends AppCompatActivity {

    private final Context mContext = this;
    private ActivityLabsListBinding binding;

    private FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_labs_list);
        binding.fabLabsListAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LabFormActivity.class);
                startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);

        binding.rvLabList.setLayoutManager(layoutManager);

        fetch();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        SupportMenuInflater inflater = (SupportMenuInflater) getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuItemProfile) {
            startActivity(new Intent(mContext, ProfileActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("labs");

        FirebaseRecyclerOptions<Lab> options =
            new FirebaseRecyclerOptions.Builder<Lab>()
                .setQuery(query, new SnapshotParser<Lab>() {
                    @NonNull
                    @Override
                    public Lab parseSnapshot(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("id") && snapshot.hasChild("name") &&
                                snapshot.hasChild("description") &&
                                snapshot.hasChild("location") &&
                                snapshot.hasChild("inCharge")) {
                            String id = Objects.requireNonNull(snapshot.child("id").getValue())
                                    .toString();
                            String name = Objects.requireNonNull(snapshot.child("name").getValue())
                                    .toString();
                            String description = Objects.requireNonNull(snapshot.child("description")
                                    .getValue()).toString();
                            String location = Objects.requireNonNull(snapshot.child("location")
                                    .getValue()).toString();
                            String inCharge = Objects.requireNonNull(snapshot.child("inCharge")
                                    .getValue()).toString();
                            return new Lab(id, name, description, location, inCharge);
                        }
                        return new Lab();
                    }
                })
                .build();

        adapter = new FirebaseRecyclerAdapter<Lab, LabViewHolder>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.rvLabList.setAdapter(adapter);
                binding.pbLabsList.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    binding.tvLabsListNoLabs.setVisibility(View.VISIBLE);
                    binding.rvLabList.setVisibility(View.GONE);
                } else {
                    binding.rvLabList.setVisibility(View.VISIBLE);
                    binding.tvLabsListNoLabs.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                Log.v("TEST", error.toString());
                binding.pbLabsList.setVisibility(View.GONE);
                binding.tvLabsListNoLabs.setText(error.toString());
                binding.tvLabsListNoLabs.setVisibility(View.VISIBLE);
                binding.rvLabList.setVisibility(View.GONE);
                super.onError(error);
            }

            @NonNull
            @Override
            public LabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_lab, parent, false);
                return new LabViewHolder(view);
            }


            @Override
            protected void onBindViewHolder(@NonNull LabViewHolder holder, final int position,
                                            @NonNull final Lab model) {
                if (model.getId() == null) {
                    holder.cvRoot.setVisibility(View.GONE);
                } else {
                    holder.tvName.setText(model.getName());
                    holder.tvLocation.setText(model.getLocation());
                    holder.tvInCharge.setText(model.getInCharge());
                    holder.cvRoot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext,
                                    DeviceListActivity.class);
                            intent.putExtra(DeviceListActivity.LABID, model.getId());
                            startActivity(intent);
                        }
                    });
                }
            }

        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
