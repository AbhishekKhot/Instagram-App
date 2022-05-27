package com.example.instagram;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetupFragment extends Fragment {
    private NavController navController;
    private Button save;
    private EditText profileName;
    private CircleImageView circleImageView;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String uid;
    private ProgressBar progressBar;
    private boolean isPhotoSelected=false;
    private Uri mImageUri=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        save = view.findViewById(R.id.save_btn_profile);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        navController = Navigation.findNavController(view);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        circleImageView = view.findViewById(R.id.circleImageView);
        profileName = view.findViewById(R.id.profilName);

        firebaseFirestore.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String imageUrl = task.getResult().getString("image");
                        profileName.setText(name);
                        mImageUri=Uri.parse(imageUrl);
                        Glide.with(requireActivity()).load(imageUrl).into(circleImageView);
                    }
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navController.navigate(R.id.action_profileSetupFragment_to_homeFragment);
                progressBar.setVisibility(View.VISIBLE);
                String name = profileName.getText().toString();
                StorageReference imageRef = storageReference.child("Profile_pics").child(uid+".jpg");
                if(isPhotoSelected){
                    if(!name.isEmpty() && mImageUri!=null){
                        imageRef.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                               if(task.isSuccessful()){
                                   imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                       @Override
                                       public void onSuccess(Uri uri) {
                                           saveToFireStore(task,name,uri);
                                       }
                                   });
                               }
                               else{
                                   progressBar.setVisibility(View.INVISIBLE);
                                   Toast.makeText(requireContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                               }
                            }
                        });
                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(requireContext(), "Empty fields are not allowed", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    saveToFireStore(null,name,mImageUri);
                }
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImagesFromGallery.launch("image/*");
            }
        });
    }

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name, Uri downloadUri) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("image", downloadUri.toString());
        firebaseFirestore.collection("Users").document(uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(requireContext(), "Your information was saved", Toast.LENGTH_SHORT).show();
                    navController.navigate(R.id.action_profileSetupFragment_to_homeFragment);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(requireContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ActivityResultLauncher<String> pickImagesFromGallery = registerForActivityResult(new ActivityResultContracts.GetContent()
            , new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null){
                        mImageUri = result;
                        circleImageView.setImageURI(result);
                        isPhotoSelected=true;
                    }
                }
            });

}