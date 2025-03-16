package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.FolderFaceAdapter;
import com.vkbao.remotemm.databinding.FragmentFileBinding;
import com.vkbao.remotemm.helper.ApiState;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.helper.PathStandard;
import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.viewmodel.FileViewModel;
import com.vkbao.remotemm.viewmodel.PathFaceFileViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileFragment extends Fragment {
    private FragmentFileBinding binding;
    private FileViewModel fileViewModel;
    private PathFaceFileViewModel pathFaceFileViewModel;

    public FileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileViewModel = new ViewModelProvider(requireActivity()).get(FileViewModel.class);
        pathFaceFileViewModel = new ViewModelProvider(requireActivity()).get(PathFaceFileViewModel.class);

        initPath();
        initListFile();
        initDeleteFile();
    }

    private void initListFile() {
        fileViewModel.getList(pathFaceFileViewModel.getPath().getValue());

        fileViewModel.getFileListLiveData().observe(getViewLifecycleOwner(), response -> {
            switch (response.status) {
                case SUCCESS:
                    List<FileInfo> fileInfoList = response.data.getData();
                    String path = pathFaceFileViewModel.getPath().getValue();
                    boolean shouldAddBack = false;
                    if (!path.isEmpty()) {
                        shouldAddBack = true;
                    }

                    FolderFaceAdapter adapter = new FolderFaceAdapter(
                            fileInfoList,
                            item -> {
                                String oldPath = pathFaceFileViewModel.getPath().getValue();
                                String newPath = oldPath.isEmpty() ? item.getName() : oldPath + "/" + item.getName();
                                pathFaceFileViewModel.updatePath(newPath);
                            },
                            (view, item) -> {
                                PopupMenu popupMenu = new PopupMenu(requireContext(), view);
                                popupMenu.getMenuInflater().inflate(R.menu.face_file_menu, popupMenu.getMenu());
                                popupMenu.setGravity(Gravity.END);

                                popupMenu.setOnMenuItemClickListener(menuItem -> {
                                    if (menuItem.getItemId() == R.id.action_delete) {
                                        String oldPath = pathFaceFileViewModel.getPath().getValue();
                                        String newPath = PathStandard.concatPath(oldPath, item.getName());

                                        Log.d("path", newPath);
                                        fileViewModel.deleteFile(newPath);
                                        return true;
                                    } else return false;
                                });

                                popupMenu.show();
                            },
                            shouldAddBack,
                            () -> {
                                String newPath = PathStandard.getBackPath(path);
                                pathFaceFileViewModel.updatePath(newPath);
                            }

                    );

                    binding.fileList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                    binding.fileList.setAdapter(null);
                    binding.fileList.setAdapter(adapter);
                    break;
                case ERROR:
                    break;
                default:
            }
        });
    }

    private void initPath() {
        pathFaceFileViewModel.getPath().observe(getViewLifecycleOwner(), path -> {
            fileViewModel.getList(path);
        });
    }

    public void initDeleteFile() {
        fileViewModel.getDeleteFileLiveData().observe(getViewLifecycleOwner(), apiState -> {
            if (apiState.status == ApiState.Status.SUCCESS) {
                fileViewModel.getList(pathFaceFileViewModel.getPath().getValue());
            } else {
                Log.d("delete error", apiState.message);
            }
        });
    }
}