package com.example.hayoung.a20190507_1146;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SettingActivity extends AppCompatActivity {

    private Button safeRangeB;
    private Button soundB;
    private Button bluetoothB;
    private Button logoutB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setTitle("Setting");

        safeRangeB = (Button) findViewById(R.id.safeRangeB);
        soundB = (Button) findViewById(R.id.soundB);
        bluetoothB = (Button) findViewById(R.id.bluetoothB);
        logoutB = (Button) findViewById(R.id.logoutB);

        bluetoothB.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), BluetoothService.class);
                startActivity(intent);
            }
        });
    }

}
