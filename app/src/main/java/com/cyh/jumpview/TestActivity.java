package com.cyh.jumpview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

/**
 * Created by yh on 17-12-14.
 */

public class TestActivity extends Activity {
    
    private TextView mJumpTxt;
    
    private JumpView mJumpView;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_test);
        
        mJumpView = (JumpView) findViewById(R.id.jump_view);
        mJumpView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mJumpView.isRunning()) {
                    mJumpView.pause();
                } else {
                    mJumpView.resume();
                }
            }
        });
    }
    
}
