package com.example.instagram;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class PostFragment extends Fragment {
    private NavController navController;
    private Button add_post;
  //  private EditText mCaptionText;
    private TextInputEditText mCaptionText;
    private ImageView mPostImage;
    private ProgressBar mProgressBar;
    private Uri postImageUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        add_post = view.findViewById(R.id.add_post_btn);
        navController = Navigation.findNavController(view);
        mCaptionText = view.findViewById(R.id.caption_edittext);
        mPostImage = view.findViewById(R.id.post_image);
        mProgressBar = view.findViewById(R.id.post_progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImagesFromGallery.launch("image/*");
            }
        });

        add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                String caption = mCaptionText.getText().toString();
                if(!caption.isEmpty() && postImageUri!=null){
                    StorageReference postRef = storageReference.child("post_images").child(FieldValue.serverTimestamp().toString()+".jpg");
                    postRef.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                postRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                       HashMap<String,Object> postMap = new HashMap<>();
                                       postMap.put("image",uri.toString());
                                       postMap.put("user",currentUserId);
                                       postMap.put("caption",caption);
                                       postMap.put("time",FieldValue.serverTimestamp());

                                       firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentReference> task) {
                                               if(task.isSuccessful()){
                                                   mProgressBar.setVisibility(View.INVISIBLE);
                                                   Toast.makeText(requireContext(),"Posted Successfully !!",Toast.LENGTH_SHORT);
                                                   navController.navigate(R.id.action_postFragment_to_homeFragment);
                                               }
                                               else{
                                                   mProgressBar.setVisibility(View.INVISIBLE);
                                                   Toast.makeText(requireContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                       });
                                    }
                                });
                            }
                            else{
                                mProgressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(requireContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                   mProgressBar.setVisibility(view.INVISIBLE);
                   Toast.makeText(requireContext(),"Please select a photo to post",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ActivityResultLauncher<String> pickImagesFromGallery = registerForActivityResult(new ActivityResultContracts.GetContent()
            , new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null){
                        postImageUri = result;
                        mPostImage.setImageURI(result);
                    }
                }
            });
}