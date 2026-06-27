package com.union_test.toutiao;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.union_test.toutiao.activity.MainActivity;
import com.union_test.toutiao.mediation.java.MediationMainActivity;

public class SelectActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediation_activity_start);
        findViewById(R.id.bt_csj).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.bt_csjm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectActivity.this, MediationMainActivity.class);
                startActivity(intent);
            }
        });
    }
}
