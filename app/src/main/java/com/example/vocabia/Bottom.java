package com.example.vocabia;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Bottom extends BottomSheetDialogFragment {
    private DatabaseReference databaseReference;
    private EditText editText_1;
    private EditText editText;
    private Button button;
    private FirebaseAuth auth;
    private add_words add_words;


    public Bottom() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_fragment, container, false);
        button = v.findViewById(R.id.add);
        editText = v.findViewById(R.id.new1);
        editText_1 = v.findViewById(R.id.meaning);
        add_words = new add_words();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editText.getText().toString().isEmpty() || editText_1.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Fields are Empty", Toast.LENGTH_SHORT).show();
                } else {

                        String temp_mea = editText_1.getText().toString().trim();
                        String temp_word = editText.getText().toString().trim();
                    add_words.setMeaning(temp_mea.substring(0,1).toUpperCase()+temp_mea.substring(1));
                    add_words.setNewWord(temp_word.substring(0,1).toUpperCase()+temp_word.substring(1));

                    FirebaseDatabase.getInstance().getReference().child("Total User").child(auth.getCurrentUser().getUid())
                            .child(temp_word.substring(0,1).toUpperCase()+temp_word.substring(1)).setValue(add_words);
                }
                Toast.makeText(getContext(), "Word Added Successfully", Toast.LENGTH_SHORT).show();

                dismiss();
            }
        });

        return v;
    }




}


