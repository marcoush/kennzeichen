package com.example.kennzeichen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quiz2 extends AppCompatActivity {

/*
    private static final int TOTAL_QUESTIONS = 40;

    private Map<String, List<String[]>> gameMap;
    private int currentIndex = 0;
    private CountDownTimer timer;
    private long elapsedTimeMillis;
    private long startTimeMillis;

    private TextView numberplatetv, timecountertv, bundeslandhinttv;
    private EditText fillintownedittext;
    private ImageButton hintbutton;



    private List<String> fullnumberPlatesList, guessednumberPlatesList, skippednumberPlatesList, selectedNumberPlatesList; //, unguessednumberPlatesList


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz1);

        //jkklasse hier einlesen();
        // InputStream inputStream = new File(context.getResources().openRawResource(R.raw.kfz_kennz_fertig)); //trash
        // FileInputStream fileInputStream = null; //trash

        InputStream inputStream = getResources().openRawResource(R.raw.kfz_kennz_fertig); // Assuming you have placed your Excel file in the raw folder
        ExcelDataReader excelDataReader = new ExcelDataReader();
        Map<String, List<String[]>> fullMap = excelDataReader.readExcelFile(inputStream);

        //pick out 40 numberplate-town-bundesland combinations from the fullMap
        generateGameMap(fullMap);


        // Initialize views
        numberplatetv = findViewById(R.id.numberplatetextviewid);
        bundeslandhinttv = findViewById(R.id.bundeslandhinttextviewid);
        fillintownedittext = findViewById(R.id.fillintownedittextid);
        hintbutton = findViewById(R.id.hintbuttonid);
        timecountertv = findViewById(R.id.timecountertextviewid);

        //initalize the game
        showNextNumberPlate(); //oncreate

        //initalize the timer
        startTimeMillis = System.currentTimeMillis();
        startTimer();

        // Add TextWatcher to the fillInTownEditText
        fillintownedittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String townGuess = editable.toString();
                checkAnswer(townGuess);
            }
        });

        //hint click
        hintbutton.setOnClickListener(v -> onHintClick());




    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }


    //1 generate game map
    public void generateGameMap(Map<String, List<String[]>> dataMap) {
        // Create a set to keep track of selected number plates
        selectedNumberPlatesList = new ArrayList<>(); //initialize
        gameMap  = new HashMap<>(); //initialize

        // Select distinct number plates until we reach the desired number of entries
        while (gameMap.size() < TOTAL_QUESTIONS && selectedNumberPlatesList.size() < dataMap.size()) {
            // Get a random number plate from the dataMap
            List<String> numberPlates = new ArrayList<>(dataMap.keySet());
            Collections.shuffle(numberPlates);
            String numberPlate = numberPlates.get(0); // Get the first (random) number plate

            // Add the number plate to the gameMap if it hasn't been selected before
            if (!selectedNumberPlatesList.contains(numberPlate)) {
                List<String[]> townBundeslandList = dataMap.get(numberPlate);
                if (townBundeslandList != null) {
                    gameMap.put(numberPlate, townBundeslandList);
                    selectedNumberPlatesList.add(numberPlate);
                }
            }
        }
    }


    //2 show next number plate
    private void showNextNumberPlate() {
        if (currentIndex < selectedNumberPlatesList.size()) {
            String numberPlate = selectedNumberPlatesList.get(currentIndex);
            numberplatetv.setText(numberPlate);
            currentIndex++;
        } else {
            // Quiz completed, stop the timer
            stopTimer();
            // Game over, display a message or perform any other action
            Toast.makeText(this, "Quiz completed!", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkAnswer(String townGuess) {
        String numberPlate = numberplatetv.getText().toString();
        List<String[]> townBundeslandList = gameMap.get(numberPlate);
        if (townBundeslandList != null && !townBundeslandList.isEmpty()) {
            String correctTown = townBundeslandList.get(0)[0];
            if (townGuess.equalsIgnoreCase(correctTown)) {
                // User's answer is correct, show next number plate
                showNextNumberPlate(); //checkanswer
                // Clear the EditText
                fillintownedittext.getText().clear();
            }
        }
    }



    //3 hints
    public void onHintClick() {
        // Display the Bundesland hint
        String numberPlate = numberplatetv.getText().toString();
        List<String[]> townBundeslandList = gameMap.get(numberPlate);
        if (townBundeslandList != null && townBundeslandList.size() > 0) {
            String bundesland = townBundeslandList.get(0)[1]; // Get Bundesland from the first entry
            // Display the hint (bundesland)
            bundeslandhinttv.setText(bundesland);
            //Toast.makeText(this, "Bundesland: " + bundesland, Toast.LENGTH_SHORT).show();
        }
    }



    //4 timer
    private void startTimer() {
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) { // CountDownTimer with maximum possible time
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer text with the elapsed time
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                // Update the timertext with the elapsed time
                updateTimerText();
            }

            @Override
            public void onFinish() {
                // This method will not be called as we're using Long.MAX_VALUE
                // You can handle any finishing logic here if needed
            }
        }.start();
    }


    private void stopTimer() {
        long endTimeMillis = System.currentTimeMillis();
        elapsedTimeMillis = endTimeMillis - startTimeMillis;

        // Convert milliseconds to seconds
        long elapsedTimeSeconds = elapsedTimeMillis / 1000;

        // Display the elapsed time
        timecountertv.setText("Total time taken: " + elapsedTimeSeconds + " seconds");
    }

    private void updateTimerText() {
        int minutes = (int) (elapsedTimeMillis / 1000) / 60;
        int seconds = (int) (elapsedTimeMillis / 1000) % 60;
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timecountertv.setText(timeLeftFormatted);
    }


}*/ //19.5.24 kommt alles in Quiz rein


}