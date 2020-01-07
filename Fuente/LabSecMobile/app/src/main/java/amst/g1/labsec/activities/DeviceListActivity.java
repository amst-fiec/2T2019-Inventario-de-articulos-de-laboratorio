package amst.g1.labsec.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

import amst.g1.labsec.R;
import amst.g1.labsec.databinding.ActivityDeviceListBinding;
import amst.g1.labsec.models.Device;
import amst.g1.labsec.viewholders.DeviceViewHolder;

public class DeviceListActivity extends AppCompatActivity {

    private final Context mContext = this;

    private ActivityDeviceListBinding binding;

    private FirebaseRecyclerAdapter adapter;

    public static final String LABID = "LABID";

    private String labId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        labId = getIntent().getStringExtra(LABID);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_device_list);

        binding.fabDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (labId != null) {
                    Intent intent = new Intent(getApplicationContext(), DeviceFormActivity.class);
                    intent.putExtra(LABID, labId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "No lab id provided",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        binding.rvDeviceList.setLayoutManager(layoutManager);

        if (labId != null)
            fetch();
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("labs").child(labId).child("devices");

        FirebaseRecyclerOptions<Device> options =
                new FirebaseRecyclerOptions.Builder<Device>()
                        .setQuery(query, new SnapshotParser<Device>() {
                            @NonNull
                            @Override
                            public Device parseSnapshot(@NonNull DataSnapshot snapshot) {
                                String id = Objects.requireNonNull(snapshot.child("id").getValue()).toString();
                                String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                String brand = Objects.requireNonNull(snapshot.child("brand").getValue()).toString();
                                String model = Objects.requireNonNull(snapshot.child("model").getValue()).toString();
//                                String borrower = snapshot.child("borrower").getValue().toString();
                                String state = Objects.requireNonNull(snapshot.child("state").getValue()).toString();
//                                Date returnDate = new Date(Long.parseLong(snapshot
//                                        .child("returnDate").getValue().toString()));
                                return new Device(id, name, brand, model, state);
                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<Device, DeviceViewHolder>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                binding.rvDeviceList.setAdapter(adapter);
                binding.pbDeviceList.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    binding.tvDeviceListNoDevices.setVisibility(View.VISIBLE);
                    binding.rvDeviceList.setVisibility(View.GONE);
                } else {
                    binding.rvDeviceList.setVisibility(View.VISIBLE);
                    binding.tvDeviceListNoDevices.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                binding.pbDeviceList.setVisibility(View.GONE);
                binding.tvDeviceListNoDevices.setText(error.toString());
                binding.tvDeviceListNoDevices.setVisibility(View.VISIBLE);
                binding.rvDeviceList.setVisibility(View.GONE);
                super.onError(error);
            }

            @NonNull
            @Override
            public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_device, parent, false);
                return new DeviceViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull DeviceViewHolder holder, final int position,
                                            @NonNull final Device model) {
                holder.tvName.setText(model.getName());
                holder.tvBrand.setText(model.getBrand());
                holder.tvModel.setText(model.getModel());

                String next = "";
                switch (model.getState()) {
                    case "Available":
                        holder.ivState.setImageResource(R.drawable.ic_check_green);
                        next = "Borrowed";
                        break;
                    case "Borrowed":
                        holder.ivState.setImageResource(R.drawable.ic_access_time_yellow);
                        next = "Available";
                        break;
                    case "Moved":
                        holder.ivState.setImageResource(R.drawable.ic_error_outline_red);
                        next = "Available";
                        break;
                }

                final String nextState = next;

                holder.cvRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                        // Add the buttons
                        builder.setPositiveButton(mContext.getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                // User clicked OK button
                                FirebaseDatabase.getInstance().getReference().child("labs")
                                    .child(labId).child("devices").child(model.getId())
                                    .child("state").setValue(nextState)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                        }
                                    });
                            }
                        });
                        builder.setNegativeButton(mContext.getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                        builder.setMessage( String.format("Change %s %s state to %s?",
                                model.getName(), model.getBrand(), nextState));

                        // Create the AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
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
