package com.vkbao.remotemm.views.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.FolderFaceAdapter;
import com.vkbao.remotemm.databinding.FragmentFileBinding;
import com.vkbao.remotemm.helper.ApiState;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.helper.PathStandard;
import com.vkbao.remotemm.model.FileInfo;
import com.vkbao.remotemm.model.SuccessResponse;
import com.vkbao.remotemm.viewmodel.FileViewModel;
import com.vkbao.remotemm.viewmodel.PathFaceFileViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;

public class FileFragment extends Fragment {
    private FragmentFileBinding binding;
    private FileViewModel fileViewModel;
    private PathFaceFileViewModel pathFaceFileViewModel;
    private boolean shouldFabExpand = false;
    private ActivityResultLauncher<Intent> filePickerLauncher;

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
        initAddFolder();
        initUploadImages();
        initFloatingBtn();
        initEncodeFaces();
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
                                        Log.d("old path" , oldPath);
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

    private void initDeleteFile() {
        fileViewModel.getDeleteFileLiveData().observe(getViewLifecycleOwner(), apiState -> {
            if (apiState.status == ApiState.Status.SUCCESS) {
                fileViewModel.getList(pathFaceFileViewModel.getPath().getValue());
            } else {
                Toast.makeText(requireContext(), "Error: " + apiState.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initAddFolder() {
        fileViewModel.getCreateFolderLiveData().observe(getViewLifecycleOwner(), apiState -> {
            if (apiState.status == ApiState.Status.SUCCESS) {
                fileViewModel.getList(pathFaceFileViewModel.getPath().getValue());
            } else {
                Toast.makeText(requireContext(), "Error: " + apiState.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUploadImages() {
        fileViewModel.getUploadImagesLiveData().observe(getViewLifecycleOwner(), apiState -> {
            if (apiState.status == ApiState.Status.SUCCESS) {
                fileViewModel.getList(pathFaceFileViewModel.getPath().getValue());
            } else {
                Toast.makeText(requireContext(), "Error: " + apiState.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initFloatingBtn() {
        binding.addMainBtn.setOnClickListener(view -> {
            shouldFabExpand = !shouldFabExpand;
            if (shouldFabExpand) {
                expandFab();
            } else {
                shrinkFab();
            }
        });
        if (shouldFabExpand) {
            expandFab();
        } else {
            shrinkFab();
        }

        binding.addFolderBtn.setOnClickListener(view -> {
            showInputDialog();
        });

        binding.uploadBtn.setOnClickListener(view -> {
            selectImages();
        });

        binding.encodeBtn.setOnClickListener(view -> {
            fileViewModel.encodeFaces();
        });
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.create_folder_menu_title));

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            String value = input.getText().toString();
            String currentPath = pathFaceFileViewModel.getPath().getValue();
            String path = PathStandard.concatPath(currentPath, value);
            fileViewModel.createFolder(path);
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }


    private void shrinkFab() {
        binding.addFolderBtn.setVisibility(View.GONE);
        binding.addFolderTV.setVisibility(View.GONE);
        binding.uploadBtn.setVisibility(View.GONE);
        binding.uploadTV.setVisibility(View.GONE);
        binding.encodeBtn.setVisibility(ViewGroup.GONE);
        binding.encodeTV.setVisibility(ViewGroup.GONE);
    }

    private void expandFab() {
        binding.addFolderBtn.setVisibility(View.VISIBLE);
        binding.addFolderTV.setVisibility(View.VISIBLE);
        binding.uploadBtn.setVisibility(View.VISIBLE);
        binding.uploadTV.setVisibility(View.VISIBLE);
        binding.encodeBtn.setVisibility(ViewGroup.VISIBLE);
        binding.encodeTV.setVisibility(ViewGroup.VISIBLE);
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        filePickerLauncher.launch(intent);
    }

    {
        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        List<Uri> fileUris = new ArrayList<>();
                        ClipData clipData = result.getData().getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                fileUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else {
                            fileUris.add(result.getData().getData());
                        }

                        if (!fileUris.isEmpty()) {
                            List<MultipartBody.Part> parts = Helper.prepareFileParts(requireContext(), fileUris);
                            fileViewModel.uploadImages(parts, pathFaceFileViewModel.getPath().getValue());
                        }
                    }
                }
        );
    }

    private void initEncodeFaces() {
        fileViewModel.getEncodeLiveData().observe(getViewLifecycleOwner(), apiState -> {
            if (apiState.status == ApiState.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Error: " + apiState.data.getData(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Error: " + apiState.message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}