package amst.g1.labsec.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import amst.g1.labsec.R;
import amst.g1.labsec.databinding.ActivityDeviceFormBinding;
import amst.g1.labsec.models.Device;

public class DeviceFormActivity extends AppCompatActivity {

    private final Device device = new Device();
    private String labId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDeviceFormBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_device_form);

        labId = getIntent().getStringExtra(DeviceListActivity.LABID);

        binding.setDevice(device);
        binding.fabDeviceForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device.isValid())
                    createDevice();
                else
                    Toast.makeText(getApplicationContext(), "All fields are required",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDevice() {
        if (labId != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                    .child("labs").child(labId).child("devices").push();
            Map<String, Object> map = new HashMap<>();
            map.put("id", databaseReference.getKey());
            map.put("name", device.getName());
            map.put("model", device.getModel());
            map.put("brand", device.getBrand());
            map.put("state", device.getState());
            map.put("battery", Integer.toHexString(device.getBattery()));
            databaseReference.setValue(map);
            Toast.makeText(getApplicationContext(), "Device created",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "No lab id provided",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
