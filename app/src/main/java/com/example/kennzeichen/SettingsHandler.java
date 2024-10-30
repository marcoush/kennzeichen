package com.example.kennzeichen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class SettingsHandler {
    private static final String TAG = "SettingsHandler";
    static ImageButton dayscreen, nightscreen;
    static TextView systemscreen, germanlanguage, englishlanguage, systemlanguage;

    //static Boolean isDarkmodeActivated = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
    static int whichScreenmodeActivated; //1: dayActivated, 2:nightActivated, 9:systemScreenActivated;
    static int whichLanguageActivated; //1: germanActivated, 2:englishActivated, 9: systemLangActivated;

    static SharedPreferences generalPrefs, languagePrefs; //TODO geht das mit static???? muss ja wenn ühaupt ALLES static sein
    public static void showSettingsDialog(Context context, Activity activity) {
         generalPrefs = context.getSharedPreferences("generalPrefs", Context.MODE_PRIVATE);
         languagePrefs = context.getSharedPreferences("languagePreferences", Context.MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
       // builder.setTitle(R.string.settings); //TODO (settings) entweder keinen title oder keine message, message = kleiner, daher finde ich es gerade feiner 01:09 7.9. - werde ich geghostet? xD23
        builder.setMessage(R.string.configureappsettings);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            // TODO: Save settings here - it seems to be never used bc. on button click everythnig happens instantly and doesn*'t have to be confirmed using save-button
            //1 screen:
            if (whichScreenmodeActivated == 1) {
                //change to day in SP
                SharedPreferences.Editor editor = generalPrefs.edit();
                editor.putString("selectedScreenmode", "day");
                editor.apply();
                Log.d(TAG,"Screenmode in screenmodePreferences gesetzt als: day UND auf smartphone eingestellt");
                //change to day
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else if (whichScreenmodeActivated == 2) {
                //change to night in SP
                SharedPreferences.Editor editor = generalPrefs.edit();
                editor.putString("selectedScreenmode", "night");
                editor.apply();
                Log.d(TAG,"Screenmode in screenmodePreferences gesetzt als: night UND auf smartphone eingestellt");
                //change to night
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else if (whichScreenmodeActivated == 9) {
                //change to system screen in SP
                SharedPreferences.Editor editor = generalPrefs.edit();
                editor.putString("selectedScreenmode", "system");
                editor.apply();
                //change to system screen
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                if (isDarkModeEnabled(context)) Log.d(TAG,"Screenmode in screenmodePreferences gesetzt als: system UND auf smartphone NIGHT eingestellt");
                else Log.d(TAG,"Screenmode in screenmodePreferences gesetzt als: system UND auf smartphone DAY eingestellt"); //ifsatz soltanto per lo log
            }


            //2 language:
            //wenn bspw. DE angeklickt wurde (langActivated=1), dann soll nur auf Dt. geändert werden, wenn vorher in generalPrefs noch nicht "de" als Sprache festgelegt war!
            if (whichLanguageActivated == 1 && !languagePrefs.getString("selectedLanguage", "system default").equals("germanSP")) {
                //lang change to de in SP
                SharedPreferences.Editor editor = languagePrefs.edit();
                editor.putString("selectedLanguage", "germanSP");
                editor.apply();
                Log.d(TAG,"Language in languagePrefs gesetzt als: de UND auf smartphone eingestellt");
                //change to de
                setLocale(activity, "de");
            }
            else if (whichLanguageActivated == 2 && !languagePrefs.getString("selectedLanguage", "system default").equals("englishSP")) {
                //lang change to en in SP
                SharedPreferences.Editor editor = languagePrefs.edit();
                editor.putString("selectedLanguage", "englishSP");
                editor.apply();
                Log.d(TAG,"Language in languagePrefs gesetzt als: en UND auf smartphone eingestellt");
                //change to en
                setLocale(activity, "en");
            }
            else if (whichLanguageActivated == 9 && !languagePrefs.getString("selectedLanguage", "system default").equals("system default")) {
                //lang change to system in SP
                SharedPreferences.Editor editor = languagePrefs.edit();
                editor.putString("selectedLanguage", "system default");
                editor.apply();
                //change to system lang -> 2.9 die system lang muss erstmal rausgefunden werden:
                String systemLanguageinspformat = LanguageUtils.getSystemLanguageFromSP(context); //applyLanguage (Settings)
                String languageCode = systemLanguageinspformat.equals("de") ? "de" : "en"; //TODO (future) add more cases if more languages are in the app}
                Log.d(TAG, "languageCode gemäß getSystemLanguageInSPFormat() ist: " +  languageCode);
                setLocale(activity, languageCode);
                Log.d(TAG,"Language in languagePrefs gesetzt als: system UND auf smartphone " + languageCode + " eingestellt");
            }

        });
        builder.setNegativeButton(R.string.cancel, null);

        // Inflate the settings layout XML file
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_settings, null);
        builder.setView(view);

        //ui initializations
        dayscreen = view.findViewById(R.id.dayscreenid);
        nightscreen = view.findViewById(R.id.nightscreenid);
        systemscreen = view.findViewById(R.id.systemscreenid);
        germanlanguage = view.findViewById(R.id.germanlanguageid);
        englishlanguage = view.findViewById(R.id.englishlanguageid);
        systemlanguage = view.findViewById(R.id.systemlanguageid);

        //basierend auf generalPrefs: Einstellen des farblich unterlegten Screenmodes
        String screenmode = generalPrefs.getString("selectedScreenmode", "system");
        switch (screenmode) {
            case "day": dayscreen.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);break;
            case "night": nightscreen.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);break;
            case "system": systemscreen.setTextColor(Color.CYAN);break;
        }

        //basierend auf generalPrefs: Einstellen der farblich unterlegten Language
        String language = languagePrefs.getString("selectedLanguage", "system default");
        switch (language) {
            case "germanSP": germanlanguage.setTextColor(Color.CYAN);break;
            case "englishSP": englishlanguage.setTextColor(Color.CYAN);break;
            case "system default": systemlanguage.setTextColor(Color.CYAN);break;
        }

        // Set click listeners for day/night screen and German/English language buttons
        dayscreen.setOnClickListener(buttonClickListener);
        nightscreen.setOnClickListener(buttonClickListener);
        systemscreen.setOnClickListener(buttonClickListener);
        germanlanguage.setOnClickListener(buttonClickListener);
        englishlanguage.setOnClickListener(buttonClickListener);
        systemlanguage.setOnClickListener(buttonClickListener);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


//ONCLICKER
    private static View.OnClickListener buttonClickListener = v -> {
    //TODO final???
    //(info) DER ONCLICKER MACHT NUR VISUELLE ÄNDERUNGEN!! WIRKLICH WAS SICH ÄNDERN TUT SICH ERST, WENN MAN DEN DIALOG SAVET
//0 initializations
        dayscreen = v.getRootView().findViewById(R.id.dayscreenid);
        nightscreen = v.getRootView().findViewById(R.id.nightscreenid);
        systemscreen = v.getRootView().findViewById(R.id.systemscreenid);
        germanlanguage = v.getRootView().findViewById(R.id.germanlanguageid);
        englishlanguage = v.getRootView().findViewById(R.id.englishlanguageid);
        systemlanguage = v.getRootView().findViewById(R.id.systemlanguageid);
        //textcolors
       // int white = v.getResources().getColor(R.color.white, null);
       // int black = v.getResources().getColor(R.color.black, null);
    int DARKdaymodetext = v.getResources().getColor(R.color.dark_text_daymode, null);
    int LIGHTnightmodetext = v.getResources().getColor(R.color.light_text_nightmode, null);

//1 screenmode:
        if (v.getId() == R.id.dayscreenid) {
            whichScreenmodeActivated = 1; //1: day

            //wenn day neu angeklickt wurde (dann müsste der color filter null sein gerade)
            if (dayscreen.getColorFilter() == null) {
                Log.d(TAG,"dayscreen wird neu aktiviert!");
                dayscreen.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);
                nightscreen.setColorFilter(null);
                if (isDarkModeEnabled(v.getContext())) systemscreen.setTextColor(LIGHTnightmodetext);
                    //if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) systemscreen.setTextColor(white);
                else systemscreen.setTextColor(DARKdaymodetext); //TODO (darkmode) wenn textcolors sich ändern -> hier auch bidde!
            }
        }
        else if (v.getId() == R.id.nightscreenid) {
            whichScreenmodeActivated = 2; //2: night

            //wenn night neu angeklickt wurde (dann müsste der color filter null sein gerade)
            if (nightscreen.getColorFilter() == null) {
                Log.d(TAG,"nightscreen wird neu aktiviert!");
                nightscreen.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);
                dayscreen.setColorFilter(null);
                Log.d(TAG,"AppCompatDelegate.getDefaultNightMode() ist: " + AppCompatDelegate.getDefaultNightMode());
                Log.d(TAG,"isDarkModeEnabled ist: " + isDarkModeEnabled(v.getContext()));
                //problem: der AppCompatDelegate.getDefaultNightMode() gibt -100 für "system-setting" aus. daher eignet sich ned gut um day/night mode zu
                // bestimmen: chat sagt nimm UiModeManager class :) 7.7.23
                if (isDarkModeEnabled(v.getContext())) systemscreen.setTextColor(LIGHTnightmodetext);
                    //if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) systemscreen.setTextColor(Color.WHITE);
                else systemscreen.setTextColor(DARKdaymodetext); //TODO (darkmode) wenn textcolors sich ändern -> hier auch bidde!
            }
        }
        else if (v.getId() == R.id.systemscreenid) {
            whichScreenmodeActivated = 9; //9: system screen

            Log.d(TAG, "current text color von 'system' (screen) is: " + systemscreen.getCurrentTextColor());
            //wenn systemscreen neu angeklickt wurde (dann müsste der color filter null sein gerade)
            if (systemscreen.getCurrentTextColor() == LIGHTnightmodetext && isDarkModeEnabled(v.getContext()) ||
                    systemscreen.getCurrentTextColor() == DARKdaymodetext && !isDarkModeEnabled(v.getContext())) {
                Log.d(TAG,"systemscreen wird neu aktiviert!");
                //TODO (darkmode) wenn textcolors sich ändern -> hier auch bidde!
                nightscreen.setColorFilter(null);
                dayscreen.setColorFilter(null);
                systemscreen.setTextColor(Color.CYAN);
            }
        }


//2 language:
        else if (v.getId() == R.id.germanlanguageid) {
            whichLanguageActivated = 1; //1: de

            Log.d(TAG, "current text color von 'DE' is: " + germanlanguage.getCurrentTextColor());
            //wenn german neu angeklickt wurde (dann ist schrift black in daymode und white in darkmode)
            if (!isDarkModeEnabled(v.getContext()) && germanlanguage.getCurrentTextColor() == DARKdaymodetext) {
                Log.d(TAG,"nightmode OFF und textcolor black: german wird neu aktiviert!");
                //night mode OFF
                germanlanguage.setTextColor(Color.CYAN);
                englishlanguage.setTextColor(DARKdaymodetext);
                systemlanguage.setTextColor(DARKdaymodetext);
            } else if (isDarkModeEnabled(v.getContext()) && germanlanguage.getCurrentTextColor() == LIGHTnightmodetext) {
                Log.d(TAG,"nightmode ON und textcolor white: german wird neu aktiviert!");
                //night mode ON
                germanlanguage.setTextColor(Color.CYAN);
                englishlanguage.setTextColor(LIGHTnightmodetext);
                systemlanguage.setTextColor(LIGHTnightmodetext);
            }
        }
        else if (v.getId() == R.id.englishlanguageid) {
            whichLanguageActivated = 2; //2: en

            //wenn german neu angeklickt wurde (dann ist schrift black in daymode und white in darkmode)
            Log.d(TAG, "current text color von 'EN' is: " + englishlanguage.getCurrentTextColor());
            if (!isDarkModeEnabled(v.getContext()) && englishlanguage.getCurrentTextColor() == DARKdaymodetext) {
                Log.d(TAG,"nightmode OFF und textcolor black: english wird neu aktiviert!");
                //night mode OFF
                germanlanguage.setTextColor(DARKdaymodetext);
                englishlanguage.setTextColor(Color.CYAN);
                systemlanguage.setTextColor(DARKdaymodetext);
            } else if (isDarkModeEnabled(v.getContext()) && englishlanguage.getCurrentTextColor() == LIGHTnightmodetext) {
                Log.d(TAG,"nightmode ON und textcolor black: english wird neu aktiviert!");
                //night mode ON
                germanlanguage.setTextColor(LIGHTnightmodetext);
                englishlanguage.setTextColor(Color.CYAN);
                systemlanguage.setTextColor(LIGHTnightmodetext);
            }
        }
        else if (v.getId() == R.id.systemlanguageid) {
            whichLanguageActivated = 9; //9: system lang

            Log.d(TAG, "current text color von 'System' is: " + systemlanguage.getCurrentTextColor());
            //wenn german neu angeklickt wurde (dann ist schrift black in daymode und white in darkmode)
            if (!isDarkModeEnabled(v.getContext()) && systemlanguage.getCurrentTextColor() == DARKdaymodetext) {
                Log.d(TAG,"system lang wird neu aktiviert! (night mode OFF)");
                //night mode OFF
                germanlanguage.setTextColor(DARKdaymodetext);
                englishlanguage.setTextColor(DARKdaymodetext);
                systemlanguage.setTextColor(Color.CYAN);
            } else if (isDarkModeEnabled(v.getContext()) && systemlanguage.getCurrentTextColor() == LIGHTnightmodetext) {
                Log.d(TAG,"system lang wird neu aktiviert! (night mode ON)");
                //night mode ON
                germanlanguage.setTextColor(LIGHTnightmodetext);
                englishlanguage.setTextColor(LIGHTnightmodetext);
                systemlanguage.setTextColor(Color.CYAN);
            }
        }
};

    static private void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());

        activity.recreate(); // Restart the activity to apply the new language //TODO (A) ist hat necessario=?
    }


    /*public static boolean isDarkModeEnabled(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            int currentNightMode = uiModeManager.getNightMode();
            return currentNightMode == UiModeManager.MODE_NIGHT_YES;
        }
        return false; // Default to false if unable to determine the mode
    } //geht erst ab Android 10 (API lvl 29)*/
    public static boolean isDarkModeEnabled(Context context) {
        Configuration config = context.getResources().getConfiguration();
        int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }


}
