package cn.dragableview;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import cn.library.SwipeDismissTouchListener;
import cn.library.SwipeableFrameLayout;

/**
 * Created by cloudist on 2017/2/8.
 */

public class DemoDialog extends DialogFragment {

    SwipeableFrameLayout layout;

    Window window;

    public static DemoDialog newInstance() {
        DemoDialog fragment = new DemoDialog();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.demo_dialog, container);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setCanceledOnTouchOutside(true);

        layout = (SwipeableFrameLayout) view.findViewById(R.id.layout);

        layout.setSwipeDismissTouchListener(new SwipeDismissTouchListener(new SwipeDismissTouchListener.DismissCallbacks() {

            @Override
            public void onDismiss(View view, boolean toRight) {
                dismiss();
            }

            @Override
            public void onSwiping(float degree) {
                WindowManager.LayoutParams windowParams = window.getAttributes();
                windowParams.dimAmount = 0.8f * degree;
                window.setAttributes(windowParams);
            }
        }));
    }

    public void onResume() {
        window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawableResource(R.color.transparent);
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.8f;
        window.setAttributes(windowParams);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
    }

}
