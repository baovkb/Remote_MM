package com.vkbao.remotemm.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.ConfigItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigAdapter extends RecyclerView.Adapter<ConfigAdapter.ConfigViewHolder> {
    private Map<String, Object> config;
    private List<String> keys;

    public ConfigAdapter(Map<String, Object> config) {
        this.config = config;
        keys = new ArrayList<>(config.keySet());
    }

    @NonNull
    @Override
    public ConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConfigItemBinding binding = ConfigItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ConfigViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfigViewHolder holder, int position) {
        String key = keys.get(position);
        Object value = config.get(key);

        holder.setKey(key);
        holder.setValue(value.toString());
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    public class ConfigViewHolder extends RecyclerView.ViewHolder {
        private ConfigItemBinding binding;

        public ConfigViewHolder(@NonNull ConfigItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setKey(String key) {
            binding.key.setText(key);
        }

        public void setValue(String value) {
            binding.value.setText(value);
        }
    }
}
