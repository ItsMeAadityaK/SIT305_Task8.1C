package com.example.llama2chatbot;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private EditText usernameInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        usernameInput = view.findViewById(R.id.editTextUsername);
        Button goButton = view.findViewById(R.id.buttonGo);

        goButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            } else {
                ChatFragment chatFragment = ChatFragment.newInstance(username);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, chatFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}
