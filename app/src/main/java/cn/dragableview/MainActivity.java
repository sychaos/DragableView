package cn.dragableview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import cn.library.DragableListener;
import cn.library.ElasticListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView logoCloudist = (ImageView) findViewById(R.id.logo_cloudist);

        logoCloudist.setOnTouchListener(new ElasticListener(logoCloudist, new ElasticListener.DismissCallbacks() {
            @Override
            public boolean canSwipe() {
                return true;
            }

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
                Toast.makeText(MainActivity.this, "sddssdsd", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
