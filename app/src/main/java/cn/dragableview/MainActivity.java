package cn.dragableview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import cn.library.SwipeDismissTouchListener;
import cn.library.SwipeableFrameLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeableFrameLayout layout = (SwipeableFrameLayout) findViewById(R.id.layout);
        ImageView logoCloudist = (ImageView) findViewById(R.id.logo_cloudist);

        layout.setSwipeDismissTouchListener(new SwipeDismissTouchListener(new SwipeDismissTouchListener.DismissCallbacks() {
            @Override
            public void onDismiss(View view, boolean toRight) {

            }

            @Override
            public void onSwiping(float degree) {

            }
        }));

        logoCloudist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoDialog.newInstance().show(getSupportFragmentManager(), "");
            }
        });
    }

}
