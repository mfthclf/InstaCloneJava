package com.mfthc.instaclonejava;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mfthc.instaclonejava.databinding.FragmentUserBinding;


public class UserFragment extends Fragment {


    FragmentUserBinding binding;
    FirebaseAuth auth;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.signUpButton.setOnClickListener(v -> SignUp(view));
        binding.signInButton.setOnClickListener(v -> SignIn(view));

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            NavDirections action = UserFragmentDirections.actionUserFragmentToFeedFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    public void SignUp(View view) {

        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    NavDirections action = UserFragmentDirections.actionUserFragmentToFeedFragment();
                    Navigation.findNavController(requireView()).navigate(action);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }


    }

    public void SignIn(View view) {
        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();
        if (!email.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    NavDirections action = UserFragmentDirections.actionUserFragmentToFeedFragment();
                    Navigation.findNavController(requireView()).navigate(action);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
