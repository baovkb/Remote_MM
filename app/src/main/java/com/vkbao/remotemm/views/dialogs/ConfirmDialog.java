package com.vkbao.remotemm.views.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.vkbao.remotemm.databinding.DialogConfirmBinding;

public class ConfirmDialog extends DialogFragment {
    public interface OnButtonClickListener {
        public void onClick();
    }

    private DialogConfirmBinding binding;
    private OnButtonClickListener positiveBtn;
    private OnButtonClickListener negativeBtn;
    private String message ="";

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        binding = DialogConfirmBinding.inflate(requireActivity().getLayoutInflater(), null, false);
        builder.setView(binding.getRoot());

        binding.message.setText(this.message);
        binding.postiveBtn.setOnClickListener(view1 -> {
            if (positiveBtn != null) positiveBtn.onClick();
            this.dismiss();
        });
        binding.negativeBtn.setOnClickListener(view12 -> {
            if (negativeBtn != null) negativeBtn.onClick();
            this.dismiss();
        });

        return builder.create();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogConfirmBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setPositiveBtn(OnButtonClickListener positiveBtn) {
        this.positiveBtn = positiveBtn;
    }

    public void setNegativeBtn(OnButtonClickListener negativeBtn) {
        this.negativeBtn = negativeBtn;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
