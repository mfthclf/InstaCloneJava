package com.mfthc.instaclonejava;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.mfthc.instaclonejava.databinding.FragmentFeedBinding;


public class FeedFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    FragmentFeedBinding binding;
    PopupMenu menu;
    FirebaseAuth auth;


    public FeedFragment() {
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
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.floatingActionButton.setOnClickListener(v -> ShowPopupMenu(view));
        menu = new PopupMenu(requireContext(), binding.floatingActionButton);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);

    }

    public void ShowPopupMenu(View view) {

        menu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.newPost) {
            NavDirections action = FeedFragmentDirections.actionFeedFragmentToUploadFragment();
            Navigation.findNavController(requireView()).navigate(action);
        } else {
            //Log-out
            auth.signOut();
            NavDirections action = FeedFragmentDirections.actionFeedFragmentToUserFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}