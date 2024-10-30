package com.example.kennzeichen;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import java.util.Locale;

public class LanguageUtils {
    //String languageCode; //outlined variable if one time used down in translateText ... for now, it works like this
    private static final String TAG = "LangUtils";




    public void updateLanguage(Context context) {
        Log.d(TAG,"updateLanguage called (in: LanguageUtils)");
        //damit die message userid ist ... und user na.me ...ist ... NUR EINMAL ANGEZEIGT WIRD WENN MAN APP STARTÖTÄT lo.ol..  ALL DAS NUR FÜR DIE 1 EINZIGE (UND NUR 1) MESSAGE AM ANFANG
        SharedPreferences languagePreferences = context.getSharedPreferences("languagePreferences", MODE_PRIVATE);
        boolean FirstTimeOpeningLanguageUtils = languagePreferences.getBoolean("FirstTimeOpeningLanguageUtils", true);
        if (FirstTimeOpeningLanguageUtils) {
            //system language wird bestimmt beim 1. Mal, wo man die app öffnet (gives "en" , "de" , et cetera)
            String systemLangCode;
            systemLangCode = Locale.getDefault().getLanguage(); //der ursprüngl. system language code
            Log.d(TAG,"systemLangCode ist festgelegt worden on primary app start als: " + systemLangCode + " (updateLanguage in: LanguageUtils)");
            String systemLang = systemLangCode.equals("de") ? "germanSP" : "englishSP";//lang code converted to "germanSP" or "englishSP" //TODO (future) add more cases if more languages are in the app}
       /*     switch (systemLangCode) {
                case "de":
                    systemLangCode = "germanSP";
                    break;
                case "en":
                    systemLangCode = "englishSP";
                    break; //TODO (future) add more cases if more languages are in the app}
            }*/
            //put system language in SP
            SharedPreferences.Editor editor = languagePreferences.edit();
            editor.putBoolean("FirstTimeOpeningLanguageUtils", false);//Update the flag to indicate that it's not the first time anymore
            editor.putString("systemLanguage", systemLang); //assign systemLang (germanSP or englishSP) as field "systemLanguage" in languagePreferences
            editor.apply();
            Log.d(TAG,"systemLanguage in language Preferences ist jetzt: " + systemLang + " (updateLanguage in: LanguageUtils)");
            //TODO (unzufr.) da die system language hier per SP festgesetzt wird -> wenn utente nachträglich system lang. ändert, dann wird die app als default language IMMER noch die alte default language anzeigen - ich habe aber keine Ahnung, wie ich das verhindern soll und das würde vermutlich eh nur bei 1, 2 gente passieren von daher juckt
            //editor.putString("system", LanguageUtils.getSystemLanguageInSPFormat()); //onCreate Main, wo app start passiert ... in Register/Login wird die system language bereits überprüft und der contentView entsprechend angepasst - aber man kann dort die Languages noch nicht ändern, daher sollte es passen, wenn man die System Language immer aus Main einliest
        }

        String languageCode = "en"; //initialization with "en", can't go deeply wrong with that lOOL - will never happen anyways, because the if below should always apply
        String selectedAppLanguage = languagePreferences.getString("selectedAppLanguage", "system");
        if(getSystemLanguageFromSP(context) != null) {
            //system language has been setup already
            //String systemLanguage = languagePreferences.getString("systemLanguage", "");//TODO (unzufr.) hier könnte man doch auch einfach hinschreiben: 'String systemLanguage = ""', oder?
            Log.d(TAG,"if1// systemLanguage was setup already and != null");
            Log.d(TAG,"if1// '"+ selectedAppLanguage + "' ist selectedAppLanguage in " + context);
            Log.d(TAG,"if1// '"+ getSystemLanguageFromSP(context) + "' ist eingelesene systemLanguage in " + context);

            //aktuell in der app eingestellte sprache: system
            if (selectedAppLanguage.equals("system")) {
                String systemLanguageinspformat = getSystemLanguageFromSP(context); //updateLanguage (LanguageUtils)
                languageCode = systemLanguageinspformat.equals("germanSP") ? "de" : "en";//TODO (languages) add more cases if more languages are in the app}
                Log.d(TAG,"if1// 'system' ist selectedAppLanguage und ausgelesener languageCode = '" + languageCode + "' in " + context);
            }
            //aktuell in der app eingestellte sprache: dt. / engl. / ...
            else {
                languageCode = selectedAppLanguage.equals("germanSP") ? "de" : "en"; //if selected language (SP) is "germanSP"-> language code="de", otherwise it's "en"//TODO (languages) add more cases if more languages are in the app}
                Log.d(TAG,"if1// '" + selectedAppLanguage + "' ist selectedAppLanguage und ausgelesener languageCode = '" + languageCode + "' in " + context);
            }
        } else Log.d(TAG, "getSystemLanguageFromSP(context) actually was null I don't underSTAND!");
        //TODO (häää) wie kann systemLanguage jemals null sein ??? wenn es doch im Zweifelsfall durch FirstTimeOpeningLanguageUtils erzeugt werden würde HÄÄÄÄ
       /* else {  //TODO (A) rausgenommen, i don't understand when this should ever happen
            //wenn systemLanguage "" ist (weil systemLanguage wurde nie ausgelesen wurde und defValue dann "" ist), dann Login / Register -> alte Logik mit ""
            String systemLanguage = languagePreferences.getString("systemLanguage", "");//hier kommt "" raus //TODO (unzufr.) hier könnte man doch auch einfach hinschreiben: 'String systemLanguage = ""', oder?
            Log.d(TAG,"if2// system lang wasn't setup yet and = null");
            Log.d(TAG,"if2// " + selectedAppLanguage + " ist spLanguage in " + context);

            //aktuell in der app eingestellte sprache: system
            if (selectedAppLanguage.equals("system")) {
                languageCode = systemLanguage;
                Log.d(TAG,"if2// selected lang = 'system' und ausgelesener lang code = " + languageCode + " in " + context);
            }
            //aktuell in der app eingestellte sprache: dt. / engl. / ...
            else {
                languageCode = selectedAppLanguage.equals("germanSP") ? "de" : "en"; //if selected language (SP) is "germanSP"-> language code="de", otherwise it's "en"//TODO (languages) add more cases if more languages are in the app}
                Log.d(TAG,"if2// selected lang = " + selectedAppLanguage + " und ausgelesener lang code = " + languageCode + " in " + context);
            }
        }*/
        Locale locale = new Locale(languageCode);//OBSOLETER KOMENTAR wenn hier der languageCode "" weitergegeben wird, ändert sich die Sprache nicht
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        Resources resources = context.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }





    //hier wird die system language bei app-start eingelesen (wenn app-start einen auf register/login [momentan ohc unsicher, was mehr sinn macht] verweist, dann kann man dort die system
    // language nicht beeinflussen und somit wird sie in Main immer so ausgelesen, wie sie wirklich ist)
    public static String getSystemLanguageFromSP(Context context) { //wird in onCreate gecallt
        Log.d(TAG,"getSystemLanguageFromSP ausgelöst (in: LanguageUtils)");
        SharedPreferences languagePreferences = context.getSharedPreferences("languagePreferences", MODE_PRIVATE);
        //systemLanguage ist in den preferences gespeichert als "germanSP" oder "englishSP" (oder halt "spanishSP", wenn ich irgendwann soweit bin) - defValue "" wird nie ausgelöst
        return languagePreferences.getString("systemLanguage", "");

    }


}
