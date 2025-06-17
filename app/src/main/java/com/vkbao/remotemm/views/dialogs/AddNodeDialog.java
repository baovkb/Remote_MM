package com.vkbao.remotemm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.DialogAddNodeBinding;

import java.util.ArrayList;
import java.util.List;

public class AddNodeDialog extends DialogFragment {
    public interface OnButtonClickListener<T> {
        public void onClick(T key, T value, T type);
    }

    private DialogAddNodeBinding binding;
    private AddNodeDialog.OnButtonClickListener<String> positiveBtn;
    private List<String> spinnerData = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        binding = DialogAddNodeBinding.inflate(requireActivity().getLayoutInflater(), null, false);
        builder.setView(binding.getRoot());

        binding.postiveBtn.setOnClickListener(view1 -> {
            if (positiveBtn != null) {
                String key = binding.keyText.getText().toString().trim();
                String valueText = binding.valueText.getText().toString().trim();

                String typeStr = binding.typeSpinner.getSelectedItem().toString();
                positiveBtn.onClick(key, valueText, typeStr);
            }
            this.dismiss();
        });
        binding.negativeBtn.setOnClickListener(view12 -> {
            this.dismiss();
        });

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireActivity(), R.layout.position_item, spinnerData);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(typeAdapter);
        binding.typeSpinner.setSelection(0);

        return builder.create();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setPositiveBtn(AddNodeDialog.OnButtonClickListener<String> positiveBtn) {
        this.positiveBtn = positiveBtn;
    }

    public void setSpinnerData(List<String> spinnerData) {
        this.spinnerData = spinnerData;
    }
}
