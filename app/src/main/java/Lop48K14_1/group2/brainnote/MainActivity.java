package Lop48K14_1.group2.brainnote;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Lop48K14_1.group2.brainnote.ui.Home.HomeFragment;
import Lop48K14_1.group2.brainnote.ui.Login.WelcomeFragment;
import Lop48K14_1.group2.brainnote.ui.MainHomeActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra nếu đã đăng nhập
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (savedInstanceState == null) {
            if (currentUser != null) {
                // Đã đăng nhập → load HomeFragment
                Intent intent = new Intent(MainActivity.this, MainHomeActivity.class);
                startActivity(intent);
            } else {
                // Chưa đăng nhập → load WelcomeFragment
                loadFragment(new WelcomeFragment());
            }
        }
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // Phương thức để chuyển đến màn hình danh sách sổ tay
    public void loadHomeFragment() {
        loadFragment(new HomeFragment());
    }
}
