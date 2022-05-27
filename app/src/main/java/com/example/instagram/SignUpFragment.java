package com.example.instagram;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class SignUpFragment extends Fragment {
    private NavController navController;
    private Button sign_up;
    private TextView alreadyAccount;
    private EditText signUpEmail, signUpPass,confirmSignUpPass;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sign_up=view.findViewById(R.id.button_sign_up);
        alreadyAccount=view.findViewById(R.id.AtextView);
        firebaseAuth = FirebaseAuth.getInstance();
        signUpEmail=view.findViewById(R.id.signUpEmailEt);
        signUpPass=view.findViewById(R.id.SignUpPassET);
        confirmSignUpPass=view.findViewById(R.id.confirmPassEt);

        navController = Navigation.findNavController(view);

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signUpEmail.getText().toString();
                String password = signUpPass.getText().toString();
                String confirmPassword = confirmSignUpPass.getText().toString();

                if(!email.isEmpty() && !password.isEmpty()){
                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(requireContext(),"Successfully created account !!",Toast.LENGTH_SHORT).show();
                                navController.navigate(R.id.action_signUpFragment_to_profileSetupFragment);
                            }
                            else{
                                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else if(password!=confirmPassword){
                    Toast.makeText(requireContext(),"Make sure type the correct password both time",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(requireContext(),"Empty fields are not allowed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_signUpFragment_to_signInFragment);
            }
        });

    }
}