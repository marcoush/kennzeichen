package com.example.kennzeichen.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kennzeichen.Quiz;
import com.example.kennzeichen.R;
import com.example.kennzeichen.databinding.FragmentHomeBinding;

public class HomeFrag extends Fragment {
    private static String TAG = "HomeFrag";

    private FragmentHomeBinding binding;
    private Button quiz_guessplace, quiz_guessregistrationplate, twentybutton, thirtybutton, fiftybutton, allbutton, onebutton, threebutton, fivebutton;
    private TextView loadingquizguessplacetv, loadingquizguessregistrationplatetv;
    //private ProgressBar progressbarquiz1, progressbarquiz2; //HAT NED FUNKTIONIERT WARUM AUCH IMMER fucnking poreababars i hat'em - stattdessen ranzige textviews jetzt uaufff
    private boolean isActivityStarting = false;

    //gameprefs + buttons
    SharedPreferences gamePrefs;
    String gamerestriction;



    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        //0 stuff..
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


//0 ui
        quiz_guessplace = binding.gamebuttonnummernschildid;
        quiz_guessregistrationplate = binding.gamebuttonortid;
        //progressbarquiz1 = binding.progressbarquiz1id;
        //progressbarquiz2 = binding.progressbarquiz2id;
        loadingquizguessplacetv = binding.loadingnummernschildtvid;
        loadingquizguessregistrationplatetv = binding.loadingorttvid;
        onebutton = binding.oneminbuttonid;
        threebutton = binding.threeminbuttonid;
        fivebutton = binding.fiveminbuttonid;
        twentybutton = binding.twentynumberplatesid;
        thirtybutton = binding.thirtynumberplatesid;
        fiftybutton = binding.fiftynumberplatesid;
        allbutton = binding.allnumberplatesid;


//1 shared preferences
        gamePrefs = requireActivity().getSharedPreferences("gamePrefs", Context.MODE_PRIVATE);
        gamerestriction = gamePrefs.getString("gamerestriction", "thirty_guesses");
        Log.d(TAG,"gamerestriction:"+gamerestriction);
        switch (gamerestriction) {
            case "one_min": setBackgroundOfThisButtonPressedAndAllOthersNormal(onebutton); break;
            case "three_min": setBackgroundOfThisButtonPressedAndAllOthersNormal(threebutton); break;
            case "five_min": setBackgroundOfThisButtonPressedAndAllOthersNormal(fivebutton); break;
            case "twenty_guesses": setBackgroundOfThisButtonPressedAndAllOthersNormal(twentybutton); break;
            case "thirty_guesses": setBackgroundOfThisButtonPressedAndAllOthersNormal(thirtybutton); break;
            case "fifty_guesses": setBackgroundOfThisButtonPressedAndAllOthersNormal(fiftybutton); break;
            case "all": setBackgroundOfThisButtonPressedAndAllOthersNormal(allbutton); break;
        }


//2 buttons
        quiz_guessplace.setOnClickListener(v -> {
            if (!isActivityStarting) {
                loadingquizguessplacetv.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getActivity(), Quiz.class);
                intent.putExtra("guesswhat", "guessplaces");
                startActivity(intent);
                isActivityStarting = true;
            }
        });
        quiz_guessregistrationplate.setOnClickListener(v -> {
            if (!isActivityStarting) {
                loadingquizguessregistrationplatetv.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getActivity(), Quiz.class);
                intent.putExtra("guesswhat", "guessregistrationplates");
                startActivity(intent);
                isActivityStarting = true;
            }
        });
        //game prefs:
        onebutton.setOnClickListener(v -> {
            changeGamePrefs("one_min");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(onebutton);
        });
        threebutton.setOnClickListener(v -> {
            changeGamePrefs("three_min");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(threebutton);
        });
        fivebutton.setOnClickListener(v -> {
            changeGamePrefs("five_min");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(fivebutton);
        });
        twentybutton.setOnClickListener(v -> {
            changeGamePrefs("twenty_guesses");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(twentybutton);
        });
        thirtybutton.setOnClickListener(v -> {
            changeGamePrefs("thirty_guesses");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(thirtybutton);
        });
        fiftybutton.setOnClickListener(v -> {
            changeGamePrefs("fifty_guesses");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(fiftybutton);
        });
allbutton.setOnClickListener(v -> {
            changeGamePrefs("all");
            setBackgroundOfThisButtonPressedAndAllOthersNormal(allbutton);
        });


        return root;
    }

    private void setBackgroundOfThisButtonPressedAndAllOthersNormal(Button pressedbutton) {
        Log.d(TAG,"setBackgroundOfThisButtonPressedAndAllOthersNormal, pressedbutton:"+pressedbutton);

        Button[] allButtons = {onebutton, threebutton, fivebutton, twentybutton, thirtybutton, fiftybutton, allbutton};
        //int i =1; //debug
        for (Button button : allButtons) {
            if (button != pressedbutton) {
                //Log.d(TAG,"unpressed button "+i+" is colored unpressed"); //debug
                //button.setBackground(ContextCompat.getDrawable(requireActivity().getApplicationContext(), R.drawable.button_highlight_normal));

                button.setBackgroundResource(android.R.color.transparent);
                //button.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color));

                //i++; //debug
            } else {
                Log.d(TAG,pressedbutton+" is colored pressed");
                pressedbutton.setBackground(ContextCompat.getDrawable(requireActivity().getApplicationContext(), R.drawable.button_highlight_pressed));
            }
        }
    }


    private void changeGamePrefs(String newValue) {
        SharedPreferences.Editor editor = gamePrefs.edit();
        editor.putString("gamerestriction", newValue);
        editor.apply();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        loadingquizguessplacetv.setVisibility(View.INVISIBLE);
        loadingquizguessregistrationplatetv.setVisibility(View.INVISIBLE);
        isActivityStarting = false;
        super.onStop();
    }
}