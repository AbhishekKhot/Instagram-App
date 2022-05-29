package com.example.instagram.ui;

import android.os.Bundle;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.instagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInFragment extends Fragment {
    private NavController navController;
    private Button sign_in;
    private TextView dAccount;
    private EditText signInEmail,signInPass;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sign_in=view.findViewById(R.id.button_sign_in);
        signInEmail=view.findViewById(R.id.signInEmailEt);
        signInPass=view.findViewById(R.id.signInPassET);

        firebaseAuth = FirebaseAuth.getInstance();


        navController = Navigation.findNavController(view);
        dAccount = view.findViewById(R.id.AintextView);

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signInEmail.getText().toString();
                String password = signInPass.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()){
                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(requireContext(), "Login Successfully !! ", Toast.LENGTH_SHORT).show();
                                navController.navigate(R.id.action_signInFragment_to_signUpFragment);
                            }
                            else{
                                Toast.makeText(requireContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(requireContext(), "Please enter email and password correctly.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_signInFragment_to_signUpFragment);
            }
        });

    }
}