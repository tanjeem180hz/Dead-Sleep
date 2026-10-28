package com.deadsleep.app.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.ActivityWhitelistBinding;
import com.deadsleep.app.model.WhitelistEntry;
import com.deadsleep.app.ui.adapter.WhitelistAdapter;
import com.deadsleep.app.util.Constants;

/**
 * Screen for managing always-allowed apps and emergency contacts.
 */
public class WhitelistActivity extends AppCompatActivity implements WhitelistAdapter.OnWhitelistRemoveListener {

    private ActivityWhitelistBinding binding;
    private WhitelistAdapter adapter;
    private DeadSleepRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWhitelistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = DeadSleepRepository.getInstance(this);

        binding.btnBackWhitelist.setOnClickListener(v -> finish());

        adapter = new WhitelistAdapter(this);
        binding.rvWhitelist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvWhitelist.setAdapter(adapter);

        repository.getWhitelistLiveData().observe(this, list -> {
            adapter.setEntries(list);
        });

        binding.btnAddWhitelist.setOnClickListener(v -> showAddWhitelistDialog());
    }

    private void showAddWhitelistDialog() {
        final EditText etName = new EditText(this);
        etName.setHint("Name (e.g. Mom or Maps)");
        final EditText etId = new EditText(this);
        etId.setHint("Phone number or Package name");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);
        layout.addView(etName);
        layout.addView(etId);

        new AlertDialog.Builder(this)
                .setTitle("Add Allowed Entry")
                .setMessage("Add an app or contact that will never be blocked during sleep mode.")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String id = etId.getText().toString().trim();
                    if (!name.isEmpty() && !id.isEmpty()) {
                        String type = id.matches("\\+?\\d+") ? Constants.WHITELIST_TYPE_CONTACT : Constants.WHITELIST_TYPE_APP;
                        WhitelistEntry entry = new WhitelistEntry(id, type, name, false);
                        repository.insertWhitelistEntry(entry);
                        Toast.makeText(this, "Added to Whitelist", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRemove(WhitelistEntry entry) {
        repository.deleteWhitelistEntry(entry);
        Toast.makeText(this, "Removed from Whitelist", Toast.LENGTH_SHORT).show();
    }
}
