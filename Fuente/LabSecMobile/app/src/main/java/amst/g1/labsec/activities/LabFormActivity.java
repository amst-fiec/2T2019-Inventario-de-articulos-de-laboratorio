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
import amst.g1.labsec.databinding.ActivityLabFormBinding;
import amst.g1.labsec.models.Lab;

public class LabFormActivity extends AppCompatActivity {

    private final Lab lab = new Lab();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLabFormBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_lab_form);
        binding.setLab(lab);
        binding.fabLabFormOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lab.isValid())
                    createLab();
                else
                    Toast.makeText(getApplicationContext(), "All fields are required",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createLab() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("labs").push();
        Map<String, Object> map = new HashMap<>();
        map.put("id", databaseReference.getKey());
        map.put("name", lab.getName());
        map.put("description", lab.getDescription());
        map.put("location", lab.getLocation());
        map.put("inCharge", lab.getInCharge());
        databaseReference.setValue(map);
        Toast.makeText(getApplicationContext(), "Lab created", Toast.LENGTH_SHORT).show();
        finish();
    }


}
