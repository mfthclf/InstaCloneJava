package com.mfthc.instaclonejava.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mfthc.instaclonejava.R;
import com.mfthc.instaclonejava.adapter.PostAdapter;
import com.mfthc.instaclonejava.databinding.FragmentFeedBinding;
import com.mfthc.instaclonejava.model.Post;

import java.util.ArrayList;
import java.util.List;


public class FeedFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    FragmentFeedBinding binding;
    PopupMenu menu;
    FirebaseAuth auth;
    FirebaseFirestore db;
    ArrayList<Post> posts;
    PostAdapter adapter;


    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();


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

        GetDataFromFireBase();

        adapter = new PostAdapter(posts);
        binding.feedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.feedRecyclerView.setAdapter(adapter);


    }

    private void GetDataFromFireBase() {

        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(requireContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            } else {
                if (value != null && !value.isEmpty()) {
                    posts.clear();
                    List<DocumentSnapshot> documents = value.getDocuments();
                    for (DocumentSnapshot document : documents) {

                        String email = (String) document.get("email");
                        String comment = (String) document.get("comment");
                        String downloadUrl = (String) document.get("downloadUrl");

                        Post post = new Post(email, comment, downloadUrl);
                        posts.add(post);

                    }
                    adapter.notifyDataSetChanged();

                }
            }
        });


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