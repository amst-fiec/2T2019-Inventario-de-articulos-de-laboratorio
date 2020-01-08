package amst.g1.labsec.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import amst.g1.labsec.R;

public class BorrowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow);
        this.setFinishOnTouchOutside(false);
    }
}
