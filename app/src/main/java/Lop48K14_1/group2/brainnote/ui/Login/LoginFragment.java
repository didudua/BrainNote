package Lop48K14_1.group2.brainnote.ui.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import Lop48K14_1.group2.brainnote.MainActivity;
import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.Home.HomeFragment;
import Lop48K14_1.group2.brainnote.ui.MainHomeActivity;


public class LoginFragment extends Fragment {
    private static final String PREFS_NAME      = "BrainNotePrefs";
    private static final String KEY_LAST_EMAIL  = "last_email";

    private SharedPreferences prefs;
    private boolean login = true;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        EditText emailEditText = view.findViewById(R.id.email_edit_text);
        Button continueButton = view.findViewById(R.id.continue_button);
        CardView googleButton = (CardView) view.findViewById(R.id.googleSignInButton);

        TextView welcomeTextView = view.findViewById(R.id.welcomeText);
        TextView mainTitle = view.findViewById(R.id.mainTitle);
        TextView haveAccount = view.findViewById(R.id.have_Account);
        TextView signupText = view.findViewById(R.id.signup_text);

        if (getArguments() != null) {
            login = getArguments().getBoolean("login", true);
        }

        if (!login) {
            welcomeTextView.setText("Đăng Ký");
            mainTitle.setText("Đăng ký và bắt đầu những note của bạn");
            haveAccount.setText("Đã có tài khoản? ");
            signupText.setText("Đăng Nhập");
        }


        continueButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (!isValidEmail(email)) {
                emailEditText.setError("Email không hợp lệ");
                emailEditText.requestFocus();
                return;
            }

            LoginPasswordFragment loginPasswordFragment = new LoginPasswordFragment();
            Bundle bundle = new Bundle();
            bundle.putString("email", email);
            if (login){
                bundle.putBoolean("login", true);
            }
            else {
                bundle.putBoolean("login", false);
            }
            loginPasswordFragment.setArguments(bundle);
            ((MainActivity) requireActivity()).loadFragment(loginPasswordFragment);
        });

        googleButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        signupText.setOnClickListener(v -> {
            if (login) {
                LoginFragment loginFragment = new LoginFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("login", false);
                loginFragment.setArguments(bundle);
                ((MainActivity) requireActivity()).loadFragment(loginFragment);
            }
            else{
                ((MainActivity) requireActivity()).loadFragment(new LoginFragment());
            }
        });

        return view;
    }
    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("GOOGLE_SIGN_IN", "Google sign in failed", e);
                Toast.makeText(getContext(), "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveEmail(user.getEmail());
                        Toast.makeText(getContext(), "Chào mừng: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                        // Chuyển sang MainActivity
                        Intent intent = new Intent(getActivity(), MainHomeActivity.class);
                        startActivity(intent);

                    } else {
                        Toast.makeText(getContext(), "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveEmail(String email) {
        prefs.edit()
                .putString(KEY_LAST_EMAIL, email)
                .apply();
    }

}