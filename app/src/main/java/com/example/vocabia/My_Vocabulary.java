package com.example.vocabia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class My_Vocabulary extends AppCompatActivity {

    private ArrayList<add_words> list;
    GoogleSignInClient m;
    private RecyclerView recyclerView;
    add_words add_words;
    SearchView searchView;
    private TextView noresult;
    int size;
    private ImageView voice;
    Bottom bottom;
    private TextToSpeech speech;





    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my__vocabulary);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        searchView = findViewById(R.id.search);
        searchView.setIconifiedByDefault(false);
        voice = findViewById(R.id.voice);

        noresult = findViewById(R.id.noresult);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak");

                try {
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(My_Vocabulary.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        m = GoogleSignIn.getClient(this, gso);
        add_words = new add_words();
        bottom = new Bottom();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Vocabulary");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.profile) {
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    intent.putExtra("size", String.valueOf(size));
                    startActivity(intent);
                }
                if (item.getItemId() == R.id.signOut) {
                    m.signOut()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(My_Vocabulary.this, "Logout Successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(My_Vocabulary.this, LoginPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    FirebaseAuth.getInstance().signOut();


                }


                if (item.getItemId() == R.id.add) {
                    Bottom bottomSheet = new Bottom();
                    bottomSheet.show(getSupportFragmentManager(), "TAG");
                }

                return false;
            }
        });

        recyclerView = findViewById(R.id.recycle_list);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Total User")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int res = speech.setLanguage(Locale.ENGLISH);

                    if(res == TextToSpeech.LANG_NOT_SUPPORTED || res == TextToSpeech.LANG_MISSING_DATA){
                        Toast.makeText(My_Vocabulary.this,"Language not supported",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    public void speak(String word){
        speech.speak(word,TextToSpeech.QUEUE_FLUSH,null);
    }
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            final int pos = viewHolder.getAdapterPosition();
            switch (direction){

                case ItemTouchHelper.RIGHT:
                    final String delete_word = list.get(pos).getNewWord();
                    final  String delete_me = list.get(pos).getMeaning();
                    Query query = databaseReference.orderByChild("newWord").equalTo(delete_word);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                ds.getRef().removeValue();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
//                    Toast.makeText(My_Vocabulary.this,String.valueOf(pos),Toast.LENGTH_SHORT).show();

                    Snackbar.make(recyclerView, delete_word + " Archived. ",Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    add_words.setNewWord(delete_word);
                                    add_words.setMeaning(delete_me);

                                    databaseReference.child(delete_word).setValue(add_words);

                                }
                            }).show();


                    break;

                case ItemTouchHelper.LEFT:

                    final  String share = "Word: "+list.get(pos).getNewWord()+";; "+"Meaning: "+list.get(pos).getMeaning();

                    Intent myIn = new Intent(Intent.ACTION_SEND);
                    myIn.setType("text/plain");
                    myIn.putExtra(Intent.EXTRA_TEXT,share);
                    myIn.putExtra(Intent.EXTRA_SUBJECT,"Word/Meaning");

                    startActivity(Intent.createChooser(myIn,"Share Using"));
            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightActionIcon(R.drawable.ic_delete_black_24dp)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(My_Vocabulary.this,R.color.delete))
                    .addSwipeLeftActionIcon(R.drawable.ic_share_black_24dp)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(My_Vocabulary.this,R.color.delete))
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case 1:
                if(resultCode == RESULT_OK && data != null) {
                    ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchView.setQuery(res.get(0),false);                                              // new added
                    search(res.get(0));
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        list = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            list.add(ds.getValue(add_words.class));

                        }
                        size = list.size();
                        if(size >0){
                            searchView.setVisibility(View.VISIBLE);

                        }
                        Adapter adapter = new Adapter(list, getApplicationContext());
                        recyclerView.setAdapter(adapter);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(My_Vocabulary.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        if(searchView!=null){
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                search(newText);

                                return true;
                            }
                        });
                    }


    }

    private void search(String str){

        ArrayList<add_words> mylist=new ArrayList<>();
        for(add_words obj: list){

            if(obj.getNewWord().toLowerCase().contains(str.toLowerCase()) || obj.getMeaning().toLowerCase().contains(str.toLowerCase())){
                mylist.add(obj);
            }
        }
        if(mylist.isEmpty()){
            noresult.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
        else {
            noresult.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

        }
        Adapter adapter=new Adapter(mylist,getApplicationContext());
        recyclerView.setAdapter(adapter);
    }


}
