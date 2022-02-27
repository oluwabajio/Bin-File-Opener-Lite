package bin.file.opener;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import bin.file.opener.alternative.ui.activities.MainActivity;
import bin.file.opener.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.method1.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));

        });

        binding.method2.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MasterActivity.class));
             });
    }
}