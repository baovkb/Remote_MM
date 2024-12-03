package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.BackupFileAdapter;
import com.vkbao.remotemm.databinding.FragmentBackupAndRestoreBinding;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;

import java.util.List;
import java.util.Map;

public class BackupAndRestoreFragment extends Fragment {
    private FragmentBackupAndRestoreBinding binding;
    private WebsocketViewModel websocketViewModel;
    private Gson gson;

    public BackupAndRestoreFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBackupAndRestoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);
        gson = new Gson();

        getFirstTimeData();
        initAddBtn();
        initFileList();
    }

    private void initAddBtn() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.addBtn.getLayoutParams();
        int fabSize = Helper.convertDpToPx(requireContext(), 56);
        int topbarSize = Helper.convertDpToPx(requireContext(), 128);
        int screenHeight = Helper.getScreenHeight(requireContext());
        int margin = screenHeight - fabSize - topbarSize;

        params.setMargins(0, margin, Helper.convertDpToPx(requireContext(), 48), 0);
        binding.addBtn.setLayoutParams(params);

        binding.addBtn.setOnClickListener(view -> {
            Map<String, Object> cmd = new LinkedTreeMap<>();
            cmd.put("action", "request backup");
            websocketViewModel.sendMessage(gson.toJson(cmd));
        });
    }

    private void getFirstTimeData() {
        Map<String, Object> cmd = new LinkedTreeMap<>();
        cmd.put("action", "request backup files");
        websocketViewModel.sendMessage(gson.toJson(cmd));
    }

    public void initFileList() {
        websocketViewModel.getBackupFileLiveData().observe(getViewLifecycleOwner(), files -> {
            BackupFileAdapter adapter = new BackupFileAdapter(files, (view, item) -> {
                PopupMenu popupMenu = new PopupMenu(requireContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.backup_menu, popupMenu.getMenu());
                popupMenu.setGravity(Gravity.END);

                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    Map<String, Object> cmd = new LinkedTreeMap<>();

                    if (menuItem.getItemId() == R.id.action_restore) {
                        cmd.put("action", "restore backup");
                        cmd.put("data", item);
                        websocketViewModel.sendMessage(gson.toJson(cmd));

                        return true;
                    } else if (menuItem.getItemId() == R.id.action_delete) {
                        cmd.put("action", "delete backup files");
                        cmd.put("data", item);
                        websocketViewModel.sendMessage(gson.toJson(cmd));

                        return true;
                    } else return false;
                });

                popupMenu.show();
            });

            binding.fileList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            binding.fileList.setAdapter(null);
            binding.fileList.setAdapter(adapter);
        });
    }
}