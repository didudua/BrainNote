package Lop48K14_1.group2.brainnote.ui.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import Lop48K14_1.group2.brainnote.R;

public class HomeFragment extends Fragment {
    private ImageView avatarImageView;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        avatarImageView = view.findViewById(R.id.avatarImageView);

        avatarImageView.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.user_popup, null);

            // Create PopupWindow
            int width = (int) (200 * getResources().getDisplayMetrics().density); // 200dp -> px
            int height = ViewGroup.LayoutParams.WRAP_CONTENT; // Chiều cao vẫn là WRAP_CONTENT

            PopupWindow popupWindow = new PopupWindow(popupView, width, height);
            popupWindow.setFocusable(true);

            popupWindow.showAsDropDown(avatarImageView, 5, 5);

            // Set item click listeners
            LinearLayout profileBtn = popupView.findViewById(R.id.item_user);
            LinearLayout notificationsBtn = popupView.findViewById(R.id.item_notify);
            LinearLayout trashBtn = popupView.findViewById(R.id.item_trash);

            profileBtn.setOnClickListener(v1 -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_profile);
                popupWindow.dismiss();
            });
            notificationsBtn.setOnClickListener(v1 -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_notification);
                popupWindow.dismiss(); // Close the PopupWindow
            });

            trashBtn.setOnClickListener(v1 -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_trashcan);
                popupWindow.dismiss(); // Close the PopupWindow
            });

            // Show PopupWindow below the avatar image
            popupWindow.showAsDropDown(avatarImageView, 0, 0);
        });

        return view;

    }

}