package Lop48K14_1.group2.brainnote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Lop48K14_1.group2.brainnote.R;

public class MainHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
    //oce
        NavigationUI.setupWithNavController(navView, navController);
    }
}

