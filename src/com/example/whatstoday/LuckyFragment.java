package com.example.whatstoday;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class LuckyFragment extends Fragment {
    private static final String KEY_CONTENT = "LuckyFragment:Content";

    public static LuckyFragment newInstance(String content) {
        LuckyFragment fragment = new LuckyFragment();

        fragment.view_type=0x1;
        
        return fragment;
    }

    private String mContent = "???";
    private char view_type = 0x0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        TextView text = new TextView(getActivity());
//        text.setGravity(Gravity.CENTER);
//        text.setText(mContent);
//        text.setTextSize(20 * getResources().getDisplayMetrics().density);
//        text.setPadding(20, 20, 20, 20);
    	View lucky = inflater.inflate(R.layout.lucky_view, null);
    	lucky.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	new Weather((TextView)lucky.findViewById(R.id.weather));
        return lucky;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
