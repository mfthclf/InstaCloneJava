package com.mfthc.instaclonejava;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.Any;
import com.mfthc.instaclonejava.databinding.FragmentUploadBinding;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


public class UploadFragment extends Fragment {

    private FragmentUploadBinding binding;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Bitmap selectedBitmap;
    private Uri selectedUri;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public UploadFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerLauncher();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        binding.postImageView.setOnClickListener(v -> SelectImage(view));
        binding.uploadButton.setOnClickListener(v -> Upload(view));
    }

    public void Upload(View view) {

        UUID id = UUID.randomUUID();
        String imagePath = id + ".jpg";
        StorageReference reference = storage.getReference().child("images").child(imagePath);

        reference.putFile(selectedUri).addOnSuccessListener(taskSnapshot -> {
            reference.getDownloadUrl().addOnSuccessListener(uri -> {

                if (auth.getCurrentUser() != null) {

                    String downloadUrl = uri.toString();
                    HashMap<String, Object> postMap = new HashMap<>();

                    postMap.put("downloadUrl", downloadUrl);
                    postMap.put("email", auth.getCurrentUser().getEmail());
                    postMap.put("comment", binding.commentText.getText().toString());
                    postMap.put("date", Timestamp.now());

                    db.collection("Posts").add(postMap).addOnSuccessListener(documentReference -> {
                        NavDirections action = UploadFragmentDirections.actionUploadFragmentToFeedFragment();
                        Navigation.findNavController(requireView()).navigate(action);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });

                }

            });
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    public void SelectImage(View view) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(requireView(), "Permission required to go to the gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(requireView(), "Permission required to go to the gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }

        }
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                    selectedUri = intent.getData();
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(), selectedUri);
                            selectedBitmap = ImageDecoder.decodeBitmap(source);
                            binding.postImageView.setImageBitmap(selectedBitmap);
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedUri);
                            binding.postImageView.setImageBitmap(selectedBitmap);
                        }

                    } catch (Exception e) {
                        Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            } else {
                Toast.makeText(requireContext(), "Permission denied by user", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}