package com.example.kennzeichen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.kennzeichen.databinding.ActivityMainBinding;

import kotlin.text.MatchNamedGroupCollection;

public class Main extends AppCompatActivity {

    private ActivityMainBinding binding;
    //ui
    private ImageButton settingsbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //0 binding stuff & nav
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //BottomNavigationView navView = findViewById(R.id.nav_view); //TODO falls ich mal brauch ....
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home/*, R.id.navigation_dashboard*/, R.id.navigation_profile).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //1 uì
        settingsbutton = findViewById(R.id.settingsid);

        //2 butóns
        settingsbutton.setOnClickListener(v -> SettingsHandler.showSettingsDialog(Main.this, this));


    }



}