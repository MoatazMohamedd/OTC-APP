package app.checktdedno.otc;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


public class LoadingFragment extends Fragment {

    RelativeLayout firstScreen, secondScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_loading, container, false);

        firstScreen = view.findViewById(R.id.first_screen);
        secondScreen = view.findViewById(R.id.second_screen);


        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                firstScreen.setVisibility(View.GONE);
                secondScreen.setVisibility(View.VISIBLE);
                displayLoadingBar(view);

            }
        }, 4000);


        return view;
    }

    public void displayLoadingBar(View view) {
        ProgressBar mProgressBar;
        CountDownTimer mCountDownTimer;

        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setProgress(0);
        mCountDownTimer = new CountDownTimer(3000, 100) {

            @Override
            public void onTick(long millisUntilFinished) {
                long timeElapsed = 3000 - millisUntilFinished;
                int progress = (int) (timeElapsed * 100 / 3000);
                mProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                //Do what you want
                mProgressBar.setProgress(100);
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        };
        mCountDownTimer.start();
    }
}