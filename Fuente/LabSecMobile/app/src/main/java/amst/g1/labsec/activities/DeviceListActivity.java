package amst.g1.labsec.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.Locale;
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
                                if (snapshot.hasChild("id") && snapshot.hasChild("name")
                                        && snapshot.hasChild("brand") &&
                                        snapshot.hasChild("model") &&
                                        snapshot.hasChild("state") &&
                                        snapshot.hasChild("battery")) {
                                    String id = Objects.requireNonNull(snapshot
                                            .child("id").getValue()).toString();
                                    String name = Objects.requireNonNull(snapshot
                                            .child("name").getValue()).toString();
                                    String brand = Objects.requireNonNull(snapshot
                                            .child("brand").getValue()).toString();
                                    String model = Objects.requireNonNull(snapshot
                                            .child("model").getValue()).toString();
                                    String state = Objects.requireNonNull(snapshot
                                            .child("state").getValue()).toString();
                                    String batteryString = Objects.requireNonNull(snapshot
                                            .child("battery").getValue()).toString();
                                    int battery = 0;
                                    try {
                                        battery = Integer.decode(batteryString);
                                    } catch (Exception ignored) {}
                                    Device device = new Device(id, name, brand, model, state, battery);
                                    if (device.getState().equals("Borrowed")) {
                                        device.setBorrower(Objects.requireNonNull(
                                                snapshot.child("borrower").getValue()).toString());
                                        device.setReturnDate(Objects.requireNonNull(
                                                snapshot.child("returnDate").getValue()).toString());
                                    }
                                    return device;
                                }
                                return new Device();
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
                if (model.getId() == null) {
                    holder.cvRoot.setVisibility(View.GONE);
                } else {
                    holder.tvName.setText(model.getName());
                    holder.tvBrand.setText(model.getBrand());
                    holder.tvModel.setText(model.getModel());
                    holder.pbBattery.setProgress(model.getBattery());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int battery = model.getBattery();
                        ColorStateList colorStateList = ColorStateList.valueOf(
                                Color.parseColor("#092354"));
                        if (battery > 0 && battery <= 15) {
                            colorStateList = ColorStateList.valueOf(
                                    Color.parseColor("#BE0002"));
                        } else if (battery > 15 && battery <= 40) {
                            colorStateList = ColorStateList.valueOf(
                                    Color.parseColor("#D5D700"));
                        } else if (battery > 40) {
                            colorStateList = ColorStateList.valueOf(
                                    Color.parseColor("#00BE11"));
                        }
                        holder.pbBattery.setProgressTintList(colorStateList);
                    }

                    String next = "";
                    switch (model.getState()) {
                        case "Available":
                            holder.ivState.setImageResource(R.drawable.ic_check_green);
                            next = "Borrowed";
                            break;
                        case "Borrowed":
                            holder.ivState.setImageResource(R.drawable.ic_access_time_yellow);
                            holder.tvBorrower.setVisibility(View.VISIBLE);
                            holder.tvReturnDate.setVisibility(View.VISIBLE);
                            holder.tvBorrower.setText(model.getBorrower());
                            holder.tvReturnDate.setText(model.getReturnDate());
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
                            buildConfirmationDialog(nextState, model);
                        }
                    });
                }
            }

        };

    }

    private void buildConfirmationDialog(final String nextState, final Device model) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setPositiveButton(mContext.getString(R.string.yes),
            new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int id) {
                    if (!nextState.equals("Borrowed")) {
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
                    } else {
                        buildBorrowInfoDialog(model);
                    }
                }
            });

        builder.setNegativeButton(mContext.getString(R.string.no),
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

        builder.setMessage(String.format("Change %s %s state to %s?",
                model.getName(), model.getBrand(), nextState));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void buildBorrowInfoDialog(final Device model) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_borrow, null);

        final EditText etReturnDate = view.findViewById(R.id.etBorrowReturnDate);
        etReturnDate.setFocusable(false);
        etReturnDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                final String selectedDate = String.format(Locale.getDefault(),
                                        "%d/%d/%d", dayOfMonth, monthOfYear + 1,
                                        year);
                                etReturnDate.setText(selectedDate);
                                etReturnDate.setError(null);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.setMinDate(Calendar.getInstance());
                    dpd.show(getFragmentManager(), "Datepickerdialog");
                }
            }
        );

        final EditText etBorrower = view.findViewById(R.id.etBorrowBorrower);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setNegativeButton(mContext.getString(R.string.cancel),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

        builder.setPositiveButton(mContext.getString(R.string.borrow), null);

        builder.setTitle("Borrow details");
        builder.setCancelable(false);
        builder.setView(view);

        AlertDialog dialog2 = builder.create();
        dialog2.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String borrower = etBorrower.getText().toString();
                        String returnDate = etReturnDate.getText().toString();
                        if (borrower.equals(""))
                            etBorrower.setError("This field is required");
                        if (returnDate.equals(""))
                            etReturnDate.setError("This field is required");
                        if (borrower.equals("") || returnDate.equals(""))
                            return;
                        try {
                            model.setState("Borrowed");
                            model.setBorrower(borrower);
                            model.setReturnDate(returnDate);
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("labs")
                                .child(labId).child("devices").child(model.getId());

                            reference.child("state").setValue(model.getState());
                            reference.child("borrower").setValue(model.getBorrower());
                            reference.child("returnDate").setValue(model.getReturnDate());

                            dialogInterface.dismiss();

                        } catch (Exception ex) {
                            Toast.makeText(mContext, ex.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                });
            }
        });
        dialog2.show();
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
