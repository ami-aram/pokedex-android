package com.example.pokedex.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.pokedex.R;

public class ResultActivity extends AppCompatActivity {

    private TextView tResultPokemonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tResultPokemonName = findViewById(R.id.result_pokemon_name);

        if(getIntent().getStringArrayExtra("result").length > 0){
            String[] results = getIntent().getStringArrayExtra("result");
            tResultPokemonName.setText(results[0]);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
