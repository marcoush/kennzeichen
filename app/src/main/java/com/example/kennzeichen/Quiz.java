package com.example.kennzeichen;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Quiz extends AppCompatActivity {
private static final String TAG = "Quiz";

    private static int TOTAL_GUESSES;
    private static int TOTAL_GUESSES_DONE = 0;
    private static long TOTAL_TIME_MS;

    //fullEntryList: Entries=Numberplates in the Map. For looping through the array, this List is needed, no matter what guessmode is active.
    private List<String> fullEntryList, fullPlacesList, guessedList, skippedList, selectedList; //TODO 195..24 instead make 1 list for all - leaving Quiz activity'll cancel quiz anyway
    //private List<String> fullnumberPlatesList, guessednumberPlatesList, skippednumberPlatesList, selectedNumberPlatesList; //, unguessednumberPlatesList
    //private List<String> fullPlacesList, guessedPlacesList, skippedPlacesList, selectedPlacesList; //TODO 19.5.24 new but discarded
    //TODO wenn fixe Time zum Lösen des Quizzes, dann skipped+unguessednumberPlatesList - ansonsten bei ewiger time nur skipped
    Map<String, List<String[]>> fullMap; //map mit allen kennzeichen mit jew. stadt + bundesland
    Map<String, List<String[]>> selectionMap; //map mit 20,30 oder 50 random kennzeichen drin
    Map<String, List<String[]>> gameMap; //fullMap o. selectionMap
    private int currentIndex = 0;
    private CountDownTimer timer;
    private long elapsedTimeMillis;
    private long startTimeMillis;

    private TextView plateorplacetv, timecountertv, bundeslandhinttv, guessprogresstv;
    private EditText fillinguessedittext;
    private ImageButton hintbutton;
    private Button skipbutton, abortbutton;

    //gamerestriction
    SharedPreferences gamePrefs;
    String gamerestriction, guesswhat;



    //TODO 1 when quiz done ➝ show in popup for those who you didn't know, what would've been the answer

    //TODO  2 game should always restart when you go back, right? if yes, make a on-back-pressed and back-button notice to user that progress won't be saved

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        //jkklasse hier einlesen();
       // InputStream inputStream = new File(context.getResources().openRawResource(R.raw.kfz_kennz_fertig)); //trash
       // FileInputStream fileInputStream = null; //trash

        //guesswhat: places OR registration plates?
        guesswhat = getIntent().getStringExtra("guesswhat");
        Log.d(TAG,"guesswhat:"+ guesswhat);

        InputStream inputStream = getResources().openRawResource(R.raw.kfz_kennz_fertig); // Assuming you have placed your Excel file in the raw folder
        ExcelDataReader excelDataReader = new ExcelDataReader();
        fullMap = excelDataReader.readExcelFile(inputStream);
        fullEntryList = new ArrayList<>(fullMap.keySet()); //get fullEntryList out of it that can be shuffled (i.e. to create a selectionMap :)


        //ui
        plateorplacetv = findViewById(R.id.plateorplacetvid);
        bundeslandhinttv = findViewById(R.id.bundeslandhinttextviewid);
        fillinguessedittext = findViewById(R.id.fillinguessedittextid);
        hintbutton = findViewById(R.id.hintbuttonid);
        timecountertv = findViewById(R.id.timecountertextviewid);
        skipbutton = findViewById(R.id.skipbuttonid);
        guessprogresstv = findViewById(R.id.guessprogresstvid);
        abortbutton = findViewById(R.id.abortgamebuttonid);

        //initialize
        guessedList = new ArrayList<>();
        skippedList = new ArrayList<>();


        //gamerestriction abrufen (guess- or time-restricted)
        gamePrefs = getSharedPreferences("gamePrefs", Context.MODE_PRIVATE);
        gamerestriction = gamePrefs.getString("gamerestriction", "thirty_guesses");
        Log.d(TAG,"gamerestriction:"+ gamerestriction);
        //set the gameMap
        if (gamerestriction.contains("guesses")) {
            switch (gamerestriction) {
                case "twenty_guesses": TOTAL_GUESSES = 20; break;
                case "thirty_guesses": TOTAL_GUESSES = 30; break;
                case "fifty_guesses": TOTAL_GUESSES = 50; break;
            }
            //pick out TOTAL_GUESSES numberplate-town-bundesland combinations from the fullMap
            generateSelectionMapAndList(fullMap);
            gameMap = selectionMap;
            Log.d(TAG,"gameMap was set to selectionMap and has "+gameMap+" entries");
            //initalize the timer!
            startCountup();
        }
        else if (gamerestriction.equals("all")) {
            TOTAL_GUESSES = fullEntryList.size();
            Collections.shuffle(fullEntryList);
            gameMap = fullMap;
            //show abort game btn //TODO for now, disable abort-btn only for ALL version
            abortbutton.setText(R.string.abortgame);
            abortbutton.setEnabled(true);
            //initalize the timer!
            startCountup();
        }
        else { //contains("min")
            Collections.shuffle(fullEntryList);
            gameMap = fullMap;
            switch (gamerestriction) {
                case "one_min": TOTAL_TIME_MS = 60000; break; //1min
                case "three_min": TOTAL_TIME_MS = 120000; break; //2min
                case "five_min": TOTAL_TIME_MS = 300000; break; //5min
            }
            //initialize the countdown!
            startCountdown();
        }




        //initalize the game
        showNextNumberPlate(); //onCreate


        // Add TextWatcher to the fillInTownEditText
        fillinguessedittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String guess = editable.toString().trim();
                checkAnswer(guess);
            }
        });

        //button clicks
        hintbutton.setOnClickListener(v -> onHintClick());
        skipbutton.setOnClickListener(v -> onSkipClick());
        abortbutton.setOnClickListener(v -> showScorePopup(true));
        /**
         * pseudocode für den ablauf der app:
         *
         * [excel datei in eine map/dictionary lesen mit den kürzeln als values und den langen namen als keys (oder ein 2-dimensionales array)]
         * map kennzeichen = new map()
         * kennzeichen.fill(excel tabelle werte)
         *
         * [zieh 40 zufällige kennzeichen (nicht doppelt)]
         * list kennzeichenListe = new list(kennzeichen.values())
         * while loop (bis kennzeichenListe.size == 40):
         *      entferne zufälliges element der liste kennzeichenListe
         * [jetzt ist kennzeichenListe unsere Liste mit gesuchten kennzeichen]
         * shuffle kennzeichenListe (zufällig mischen)
         *
         * [spielablauf:]
         * wähle erstes element aus kennzeichenListe und frag spieler ab (z.b. was ist HH?)
         * spieler muss eingabe tätigen
         * wenn spieler eingabe bestätigt -> suche die map kennzeichen nach values ab, die dem namen entsprechen
         * z.b.:
         * list temporaryList = kennzeichen.keys()
         * if temporaryList.contains(eingabe):
         *      if kennzeichen.get(eingabe) == [gesuchtes kennzeichen, hier HH]:
         *           player was correct
         *           remove first element of kennzeichenListe
         *           continue with first element of kennzeichenListe (mit dem element was nun an index 0 ist)
         *      else
         *          player was wrong
         *          try again
         *
         */


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        //TODO 19.5.24 new:
        plateorplacetv.setText("");
        bundeslandhinttv.setText("");
        timecountertv.setText("");
        guessprogresstv.setText("");
    }


    //1 generate game map
    public void generateSelectionMapAndList(Map<String, List<String[]>> fullMap) {
        Log.d(TAG,"generateSelectionMap");
        // Create a set to keep track of selected number plates
        selectionMap = new HashMap<>(); //initialize, only temporary, will be replaced with gameMap anyways.
        selectedList = new ArrayList<>(); //initialize
        Collections.shuffle(fullEntryList); //shuffle  TODO kann auch hier hin, odder?

        //lol hier wird ne map erstellt, und ich muss erst später die guess-plates/places Logik implementiere, niet ici
        Log.d(TAG,"selectedList.size():"+ selectedList.size() +
                "\nfullMap.size():"+fullMap.size() +
                "\nfullEntryList.size():"+fullEntryList.size());

        // Select distinct number plates until we reach the desired number of entries (while to prevent overcookings)
        while (selectionMap.size() < TOTAL_GUESSES && selectedList.size() < fullMap.size()) {
                // Iterate through shuffled number plates
                for (String numberPlate : fullEntryList) {
                    // Add the plate or place to the selectionMap (and directly afterwards to the gameMap) if it hasn't been selected before
                    //search either for the plate or for the place in selectedList
                    String plateorplace;
                    if (guesswhat.equals("guessplaces")) plateorplace = numberPlate; //get the plate ➝ entry //TODO 19.5.24 new
                    else plateorplace = fullMap.get(numberPlate).get(0)[0]; //get the place ➝ first list entry, first String[] entry //TODO 19.5.24 new
                    if (!selectedList.contains(plateorplace)) {
                        List<String[]> townBundeslandList = fullMap.get(numberPlate);
                        //Log.d(TAG,"townBundeslandList: "+townBundeslandList);
                        if (townBundeslandList != null) {
                            selectionMap.put(numberPlate, townBundeslandList);

                            //guesswhat: unlike the Maps, the selectedGameList is created & manipulated depend. on the guessmode //TODO 19.5.24 new
                            if (guesswhat.equals("guessplaces")) selectedList.add(numberPlate); //get the plate ➝ entry //TODO 19.5.24 new
                            else selectedList.add(townBundeslandList.get(0)[0]); //get the place ➝ first list entry, first String[] entry //TODO 19.5.24 new

                            break; // Exit the loop after adding a number plate
                        }
                    }
                }
        }      //i love stares :)
        Log.d(TAG,"selectionMap was created: "+selectionMap.size()+" entries");
    }


    //2 show next plate/place
    @SuppressLint("SetTextI18n")
    private void showNextNumberPlate() {
        Log.d(TAG,"showNextNumberPlate");
        //hintbutton wieder resetten
        hintbutton.setEnabled(true);
        hintbutton.setImageResource(R.drawable.bulb);

        //guesses restricted version
        if (gamerestriction.contains("guesses")) {
            //guess progress anzeigen
            if (guesswhat.equals("guessplaces")) guessprogresstv.setText(TOTAL_GUESSES_DONE + " " + getString(R.string.of) + " " + TOTAL_GUESSES + " " + getString(R.string.numberplates_dativ)); //TODO 19.5.24 new
            else guessprogresstv.setText(TOTAL_GUESSES_DONE + " " + getString(R.string.of) + " " + TOTAL_GUESSES + " " + getString(R.string.places_dativ)); //TODO 19.5.24 new
            TOTAL_GUESSES_DONE++;
            //los geht's
            if (currentIndex < selectedList.size()) {
                String plateorplace = selectedList.get(currentIndex);
                Log.d(TAG,"plateorplace:"+plateorplace);
                plateorplacetv.setText(plateorplace);
                currentIndex++;
            } else {
                //this only applies for the guess-restriction game :D
                // Quiz completed, stop the timer
                timer.cancel();
                // Game over, show score
                showScorePopup(false);
                //Toast.makeText(this, "Quiz completed!", Toast.LENGTH_SHORT).show();
            }
        }
        //"all" versio
        else if (gamerestriction.equals("all")) {
            String plateorplace;//TODO 19.5.24 new
            String plate = fullEntryList.get(currentIndex);//TODO 19.5.24 new
            if (guesswhat.equals("guessplaces")) plateorplace = plate; //get the plate which is just the key of the map //TODO 19.5.24 new
            else plateorplace = fullMap.get(plate).get(0)[0]; //get the place which is nested in the value of the map //TODO 19.5.24 new
            Log.d(TAG,"numberPlate:"+plateorplace);
            plateorplacetv.setText(plateorplace);
            currentIndex++;
            //numberplates progress anzeigen
            if (guesswhat.equals("guessplaces")) guessprogresstv.setText(TOTAL_GUESSES_DONE + " " + getString(R.string.of) + " " + TOTAL_GUESSES + " " + getString(R.string.numberplates_dativ)); //TODO 19.5.24 new
            else guessprogresstv.setText(TOTAL_GUESSES_DONE + " " + getString(R.string.of) + " " + TOTAL_GUESSES + " " + getString(R.string.places_dativ)); //TODO 19.5.24 new
            TOTAL_GUESSES_DONE++;
        }
        //minutes restricted version
        else{
            String plateorplace;//TODO 19.5.24 new
            String plate = fullEntryList.get(currentIndex);//TODO 19.5.24 new
            if (guesswhat.equals("guessplaces")) plateorplace = plate; //get the plate which is just the key of the map
            else plateorplace = fullMap.get(plate).get(0)[0]; //get the place which is nested in the value of the map
            Log.d(TAG,"numberPlate:"+plateorplace);
            plateorplacetv.setText(plateorplace);
            currentIndex++;
        }
    }
    private void checkAnswer(String guess) {
        //this part for reading in the townBundeslandList is the same in checkAnswer & onHintClick (but in checkAnswer i need the parameters inside of it later, so can't methodize it...)
        String displayedplateorplace = plateorplacetv.getText().toString(); //NICHT DIE EINGEGEBENE, SONDERN DIE ANGEZEIGTE //TODO 19.5.24 new
        String plate, associatedplateorplace; //=null als initialization, wird eh overwritten //TODO 19.5.24 new
        if (guesswhat.equals("guessplaces")) {
            plate = displayedplateorplace; //TODO 19.5.24 new
            associatedplateorplace = gameMap.get(plate).get(0)[0];
        }
        else {
            plate = findPlateBelongingToPlace(displayedplateorplace);
            associatedplateorplace = plate;
        }
        List<String[]> townBundeslandList = gameMap.get(plate);
        //Log.d(TAG, "townBundeslandList:"+townBundeslandList);
        if (townBundeslandList != null && !townBundeslandList.isEmpty()) {
            for (String[] townBundesland : townBundeslandList) {
                String correctAnswer; //TODO 19.5.24 new
                if (guesswhat.equals("guessplaces")) correctAnswer = townBundesland[0];//TODO 19.5.24 new
                else correctAnswer = plate; //TODO 19.5.24 new
                Log.d(TAG,"guess:" + guess + ", correctAnswer:"+correctAnswer);
                // Convert both the user's input and the correct answer to lowercase before comparison
                if (guess.equalsIgnoreCase(correctAnswer)) {
                    //add to guessed arraylist
                    if (!guessedList.contains(displayedplateorplace+"\n"+associatedplateorplace)) {  //TODO 19.5.24 new
                        guessedList.add(displayedplateorplace+"\n"+associatedplateorplace); //TODO 19.5.24 new
                    }
                    // User's answer is correct, display next plate or place
                    showNextNumberPlate(); //checkAnswer
                    //hint clearen
                    bundeslandhinttv.setText("");
                    // Clear the EditText
                    fillinguessedittext.getText().clear();
                    return; // Exit the loop if a correct town is found
                }
            }
        }
    }


    //TODO reset all variables and arrays when onDestroy!!!

    //3 buttons hint + skip
    public void onHintClick() {
        //this part for reading in the townBundeslandList is the same in checkAnswer & onHintClick (but in checkAnswer i need the parameters inside of it later, so can't methodize it...)
        String displayedplateorplace = plateorplacetv.getText().toString(); //NICHT DIE EINGEGEBENE, SONDERN DIE ANGEZEIGTE //TODO 19.5.24 new
        String plate = null; //=null als initialization, wird eh overwritten //TODO 19.5.24 new
        if (guesswhat.equals("guessplaces")) plate = displayedplateorplace; //TODO 19.5.24 new
        else {
            //über den Ort das zugeh. Kennzeichen finden und daraus dann townBundeslandlist erstellen
            for (Map.Entry<String, List<String[]>> entry : gameMap.entrySet()) {
                //wenn einer der Orte zum Kennzeichen passt (String[0]=Ort), dann nimm diese plate
                for (String[] place : entry.getValue()) {
                    if (place[0].equals(displayedplateorplace)) {
                        plate = entry.getKey();
                        break;
                    }
                }
            }
        }
        List<String[]> townBundeslandList = gameMap.get(plate);
        //String numberPlate = plateorplacetv.getText().toString(); //TODO 19.5.24 new
        //List<String[]> townBundeslandList = gameMap.get(numberPlate); //TODO 19.5.24 new
        // Display the Bundesland hint
        Log.d(TAG,"townBundeslandList:"+townBundeslandList);
        if (townBundeslandList != null && townBundeslandList.size() > 0) {
            String bundesland = townBundeslandList.get(0)[1]; // Get Bundesland from the first entry
            Log.d(TAG,"bundesland:"+bundesland);
            // Display the hint (bundesland)
            bundeslandhinttv.setText(bundesland);
            //Toast.makeText(this, "Bundesland: " + bundesland, Toast.LENGTH_SHORT).show();
        }
        //hintbutton disablen + anmalen
        hintbutton.setEnabled(false);
        hintbutton.setImageResource(R.drawable.bulb_yellow);
    }


    public void onSkipClick() {
        //this part for reading in the townBundeslandList is the same in checkAnswer & onHintClick (but in checkAnswer i need the parameters inside of it later, so can't methodize it...)
        String displayedplateorplace = plateorplacetv.getText().toString(); //NICHT DIE EINGEGEBENE, SONDERN DIE ANGEZEIGTE //TODO 19.5.24 new
        String associatedplateorplace; //=null als initialization, wird eh overwritten //TODO 19.5.24 new
        if (guesswhat.equals("guessplaces")) associatedplateorplace = gameMap.get(displayedplateorplace).get(0)[0];   //TODO 19.5.24 new
        else associatedplateorplace = findPlateBelongingToPlace(displayedplateorplace);//TODO 19.5.24 new

        //add to skipped arraylist
        if (!skippedList.contains(displayedplateorplace+"\n"+associatedplateorplace)) { //TODO 19.5.24 new
            skippedList.add(displayedplateorplace+"\n"+associatedplateorplace); //TODO 19.5.24 new
        }
        //et empty maken
        fillinguessedittext.getText().clear();
        //hint clearen
        bundeslandhinttv.setText("");
        //show next
        showNextNumberPlate(); //skip
    }

    private String findPlateBelongingToPlace(String displayedplateorplace) {
        //über den Ort das zugeh. Kennzeichen finden und daraus dann townBundeslandlist erstellen
        for (Map.Entry<String, List<String[]>> entry : gameMap.entrySet()) {
            //wenn einer der Orte zum Kennzeichen passt (String[0]=Ort), dann nimm diese plate
            for (String[] place : entry.getValue()) {
                if (place[0].equals(displayedplateorplace)) {
                    return entry.getKey();
                }
            }
        }
        return "la bomba"; //this will never happen bc. the for-loop will alaways find the related plate
    }

    //4 score
    @SuppressLint("SetTextI18n")
    private void showScorePopup(boolean abort) {
        // Create and show the popup dialog
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_score);
        //if user aborted, finish timer
        if (abort) {
            timer.cancel();
            abortbutton.setEnabled(false);
        }
        //percentscore
        double correctGuesses, skippedGuesses, scorePercentage;
        correctGuesses = guessedList.size();
        skippedGuesses = skippedList.size();
        if (correctGuesses == 0) scorePercentage = 0;
        else if (skippedGuesses == 0) scorePercentage = 100;
        else scorePercentage = (correctGuesses / (correctGuesses+skippedGuesses)) * 100;
        // Round the score percentage to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        String percentscore = df.format(scorePercentage) + "%";
        TextView percentscoretv = dialog.findViewById(R.id.percentscoretvid);
        if (scorePercentage < 40) percentscoretv.setTextColor(getColor(R.color.red));
        else if (scorePercentage >= 40 && scorePercentage < 70)  percentscoretv.setTextColor(getColor(R.color.yellow));
        else percentscoretv.setTextColor(getColor(R.color.lightgreen));
        percentscoretv.setText(percentscore);
        //correct and skipped ones
        TextView amountofguessedtv = dialog.findViewById(R.id.amountofguessedtvid);
        TextView amountofskippedtv = dialog.findViewById(R.id.amountofskippedtvid);
        amountofguessedtv.setText(String.valueOf(correctGuesses));
        amountofskippedtv.setText(String.valueOf(skippedGuesses));
        //listviews
        ListView listViewCorrectGuesses = dialog.findViewById(R.id.listview_correct_guessesid);
        ArrayAdapter<String> guessedAdapter;
        if (!guessedList.isEmpty()) {
            guessedAdapter = new ArrayAdapter<>(this, R.layout.list_item_layout, guessedList);
            listViewCorrectGuesses.setAdapter(guessedAdapter);
        }
        ListView listViewSkippedGuesses = dialog.findViewById(R.id.listview_skipped_guessesid);
        ArrayAdapter<String> skippedAdapter;
        if (!skippedList.isEmpty()) {
            skippedAdapter = new ArrayAdapter<>(this, R.layout.list_item_layout, skippedList);
            listViewSkippedGuesses.setAdapter(skippedAdapter);
        }
        //backbutton+
        Button backbutton = dialog.findViewById(R.id.backbuttonid);
        backbutton.setOnClickListener(v -> {
            Intent intent = new Intent(Quiz.this, Main.class);
            startActivity(intent);
        });
        //version
        if (gamerestriction.contains("guesses") || gamerestriction.equals("all")) {
            //wenn numberplates-restricted version: totaltimetaken
            long elapsedTimeSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000;
            long minutes = elapsedTimeSeconds / 60;
            long seconds = elapsedTimeSeconds % 60;
            String totaltimetaken = minutes + "min " + seconds + "s";
            TextView totaltimetakenoramountofsolvednumberplatestv = dialog.findViewById(R.id.totaltimetakenoramountofguessestvid);
            totaltimetakenoramountofsolvednumberplatestv.setText(totaltimetaken);
        }/* else if (gamerestriction.equals("all")) {
            abortbutton.setEnabled(false);
            TextView totaltimetakenoramountofsolvednumberplatestv = dialog.findViewById(R.id.totaltimetakenoramountofsolvednumberplatestvid);
            totaltimetakenoramountofsolvednumberplatestv.setText(totaltimetaken);
        }*/ //oben im if
        else{
            //wenn minutes-restricted versioN: amount of solved numberplates
            TextView totaltimetakenoramountofguessestv = dialog.findViewById(R.id.totaltimetakenoramountofguessestvid);
            if (guesswhat.equals("guessplaces")) totaltimetakenoramountofguessestv.setText((int) correctGuesses + " " + getString(R.string.guessednumberplates));
            else totaltimetakenoramountofguessestv.setText((int) correctGuesses + " " + getString(R.string.guessedplaces));
        }
        dialog.setCanceledOnTouchOutside(false); // Set dialog to not dismiss on outside touch
        dialog.show();
    }



    //5.1 countup (numberplates-restricted game version)
    private void startCountup() {
        Log.d(TAG,"startCountup");
        timecountertv.setTextSize(25); //größere Schriftgröße bei minute-restrained quiz
        startTimeMillis = System.currentTimeMillis();
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) { //countup!
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer text with the elapsed time
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                String timeLeftFormatted = String.format(Locale.getDefault(),"%02d:%02d", (elapsedTimeMillis / 1000) / 60, (elapsedTimeMillis / 1000) % 60);
                timecountertv.setText(timeLeftFormatted);
            }
            @Override
            public void onFinish() {
                // This method will not be called as we're using Long.MAX_VALUE
            }
        }
        .start();
    }
    //5.2 countdown (minute-restricted game version)
    private void startCountdown() {
        Log.d(TAG,"startCountdown");
        timecountertv.setTextSize(20); //kleinere Schriftgröße bei numberplates-restrained quiz
        // Create a countdown timer with 1 minute duration and 1 second interval
        timer = new CountDownTimer(TOTAL_TIME_MS, 1000) { //countdown!
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the countdown text view with the remaining time
                long secondsRemaining = millisUntilFinished / 1000;
                String timeRemaining = String.format(Locale.getDefault(),"%02d:%02d", secondsRemaining / 60, secondsRemaining % 60);
                timecountertv.setText(timeRemaining);
            }
            @Override
            public void onFinish() {
                // Show a toast or trigger the popup when the timer finishes
                showScorePopup(false);
            }
        }
        .start();
    }

}