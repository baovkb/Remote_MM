package com.vkbao.remotemm.views.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.text.InputType;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.DefaultPageAdapter;
import com.vkbao.remotemm.adapter.ProfileAdapter;
import com.vkbao.remotemm.adapter.ProfilePageAdapter;
import com.vkbao.remotemm.databinding.FragmentProfileConfigBinding;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.model.ModuleModel;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProfileConfigFragment extends Fragment {
    private FragmentProfileConfigBinding binding;
    private WebsocketViewModel websocketViewModel;
    private String[] allModules;
    private int currentPage = 0;

    private static final String TAG = "ProfileConfigFragment";

    public ProfileConfigFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        initModuleDialog();
        initDefaultPageModules();
        initProfiles();
        initPageModules();
        initSaveAllBtn();
    }

    public void showConfirmDialog() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setMessage(getString(R.string.confirm_save_config));
        confirmDialog.setPositiveBtn(() -> {
            ModuleModel moduleModel = websocketViewModel.getProfileLiveData().getValue();

            Map<String, Object> payload = new LinkedTreeMap<>();
            Gson gson = new Gson();
            payload.put("action", "request save config");
            payload.put("data", moduleModel);

            String json = gson.toJson(payload);
            websocketViewModel.sendMessage(json);
        });

        confirmDialog.show(getChildFragmentManager(), null);
    }

    private void initSaveAllBtn() {
        binding.saveAllBtn.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            float startX, startY;
            long startTime;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = event.getRawX() - view.getX();
                        dY = event.getRawY() - view.getY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        startTime = System.currentTimeMillis();

                        ViewParent parent = view.getParent();
                        while (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                            parent = parent.getParent();
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() - dX;
                        float newY = event.getRawY() - dY;
                        view.setTranslationX(newX - view.getLeft());
                        view.setTranslationY(newY - view.getTop());
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getRawX();
                        float endY = event.getRawY();
                        long endTime = System.currentTimeMillis();

                        float deltaX = Math.abs(endX - startX);
                        float deltaY = Math.abs(endY - startY);
                        long deltaTime = endTime - startTime;

                        if (deltaX < 10 && deltaY < 10 && deltaTime < 200) {
                            view.performClick();
                        }

                        parent = view.getParent();
                        while (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(false);
                            parent = parent.getParent();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });


        binding.saveAllBtn.setOnClickListener(view -> {
            showConfirmDialog();
        });
    }


    private void initPageModules() {
        websocketViewModel.getChosenProfileLiveData().observe(getViewLifecycleOwner(), chosenProfile -> {
            if (!chosenProfile.isEmpty()) {
                ModuleModel oldModuleModel = websocketViewModel.getProfileLiveData().getValue();
                Map<String, Object> config = getConfig(oldModuleModel);

                List<List<String>> pages = getPages(oldModuleModel, chosenProfile);
                ProfilePageAdapter adapter = new ProfilePageAdapter(pages, new ProfilePageAdapter.OnPageEvent<Integer, String>() {
                    @Override
                    public void onAddClick(Integer page) {
                        showSelectionModuleDialog(value -> {
                            List<List<String>> tmpPages = getPages(oldModuleModel, chosenProfile);
                            List<String> currentPage = tmpPages.get(page);
                            currentPage.add(value);

                            ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                            websocketViewModel.setProfileLiveData(newModuleModel);
                        });
                    }

                    @Override
                    public void onDeleteClick(Integer page, String module) {
                        List<List<String>> tmpPages = getPages(oldModuleModel, chosenProfile);
                        List<String> currentPage = tmpPages.get(page);

                        currentPage.remove(module);
                        ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                        websocketViewModel.setProfileLiveData(newModuleModel);
                    }
                });
                binding.profileViewPager.setUserInputEnabled(false);
                binding.profileViewPager.setAdapter(adapter);
                binding.profileViewPager.setCurrentItem(currentPage);

                binding.nextPageBtn.setOnClickListener(null);
                binding.nextPageBtn.setOnClickListener(view -> {
                    int nextPage = binding.profileViewPager.getCurrentItem() + 1;
                    if (nextPage < adapter.getItemCount()) {
                        this.currentPage = nextPage;
                        binding.profileViewPager.setCurrentItem(nextPage, true);
                    }
                });

                binding.previousPageBtn.setOnClickListener(null);
                binding.previousPageBtn.setOnClickListener(view -> {
                    int prevPage = binding.profileViewPager.getCurrentItem() - 1;
                    if (prevPage >= 0) {
                        this.currentPage = prevPage;
                        binding.profileViewPager.setCurrentItem(prevPage, true);
                    }
                });

                binding.addPage.setOnClickListener(null);
                binding.addPage.setOnClickListener(view -> {
                    List<List<String>> tmpPages = getPages(oldModuleModel, chosenProfile);
                    tmpPages.add(new ArrayList<>());

                    ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                    websocketViewModel.setProfileLiveData(newModuleModel);
                });

                binding.deletePage.setOnClickListener(null);
                binding.deletePage.setOnClickListener(view -> {
                    int position = binding.profileViewPager.getCurrentItem();
                    List<List<String>> tmpPages = getPages(oldModuleModel, chosenProfile);
                    tmpPages.remove(position);

                    ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                    websocketViewModel.setProfileLiveData(newModuleModel);
                });
            }
        });
    }

    private void initProfiles() {
        List<String> profileNames = new ArrayList<>();
        ProfileAdapter profileAdapter = new ProfileAdapter(
                profileNames,
                value -> {
                    websocketViewModel.setChosenProfile(value);
                },
                value -> {
                    ModuleModel oldModuleModel = websocketViewModel.getProfileLiveData().getValue();
                    Map<String, Object> config = getConfig(oldModuleModel);
                    List<Object> profiles = getProfiles(oldModuleModel);

                    for (Object object: profiles) {
                        if (object instanceof HashMap && ((HashMap<?, ?>) object).get("name").equals(value)) {
                            profiles.remove(object);
                        }
                    }

                    ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                    websocketViewModel.setProfileLiveData(newModuleModel);
                }
        );
        binding.profilesList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.profilesList.setAdapter(profileAdapter);

        websocketViewModel.getProfileConfigLiveData().observe(getViewLifecycleOwner(), objectList -> {
            List<String> tmp = new ArrayList<>();
            for (Object object: objectList) {
                if (object instanceof Map && ((HashMap<String, Object>)object).containsKey("name")) {
                    tmp.add((String) ((HashMap<String, Object>)object).get("name"));
                }
            }
            profileAdapter.setProfileList(tmp);
        });

        binding.profileAddBtn.setOnClickListener(view -> {
            showInputDialog(value -> {
                value = value.trim();
                if (value.isEmpty()) return;

                ModuleModel oldModuleModel = websocketViewModel.getProfileLiveData().getValue();
                List<Object> profiles = (List<Object>) oldModuleModel.getConfig().get("profiles");

                HashMap<String, Object> newProfile = new HashMap<>();
                newProfile.put("name", value);
                List<List<String>> pages = new ArrayList<>(new ArrayList<>());
                newProfile.put("pages", pages);
                profiles.add(newProfile);

                Map<String, Object> config = getConfig(oldModuleModel);
                ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                websocketViewModel.setProfileLiveData(newModuleModel);
            });
        });
    }

    private void initModuleDialog() {
        websocketViewModel.getAllModuleLiveData().observe(getViewLifecycleOwner(), moduleModels -> {
            allModules = new String[moduleModels.size()];
            for (int i = 0; i < moduleModels.size(); i++) {
                allModules[i] = moduleModels.get(i).getModule();
            }
        });
    }

    private void initDefaultPageModules() {
        websocketViewModel.getProfileLiveData().observe(getViewLifecycleOwner(), moduleModel -> {
            Log.d(TAG, "profile module changed");

            List<String> defaultPage = getDefaultPage(moduleModel);
            DefaultPageAdapter defaultPageAdapter = new DefaultPageAdapter(defaultPage, item -> {
                ModuleModel oldModuleModel = websocketViewModel.getProfileLiveData().getValue();
                Map<String, Object> config = getConfig(oldModuleModel);

                if (defaultPage.contains(item)) {
                    defaultPage.remove(item);
                    ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                    websocketViewModel.setProfileLiveData(newModuleModel);
                }

            });

            binding.defaultPageModules.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            binding.defaultPageModules.setAdapter(defaultPageAdapter);
        });

        binding.defaultPageModulesAddBtn.setOnClickListener(view -> {
            showSelectionModuleDialog(value -> {
                // add more module
                ModuleModel oldModuleModel = websocketViewModel.getProfileLiveData().getValue();
                List<String> defaultPage = getDefaultPage(oldModuleModel);
                Map<String, Object> config = getConfig(oldModuleModel);

                if (config.containsKey("defaultPage")) {
                    defaultPage.add(value);
                    config.put("defaultPage", defaultPage);

                    ModuleModel newModuleModel = new ModuleModel(config, oldModuleModel.getPosition(), oldModuleModel.getModule());
                    websocketViewModel.setProfileLiveData(newModuleModel);
                }
            });
        });
    }

    private List<Object> getProfiles(ModuleModel moduleModel) {
        Map<String, Object> config = getConfig(moduleModel);
        if (config.containsKey("profiles") && config.get("profiles") instanceof List) {
            return (List<Object>) config.get("profiles");
        } else {
            return new ArrayList<>();
        }
    }

    private List<List<String>> getPages(ModuleModel moduleModel, String chosenProfile) {
        ModuleModel oldModuleModel = websocketViewModel.getProfileLiveData().getValue();
        List<Object> profiles = getProfiles(oldModuleModel);

        List<List<String>> pages = new ArrayList<>(new ArrayList<>());

        for (Object object: profiles) {
            if (object instanceof HashMap && ((HashMap<?, ?>) object).get("name").equals(chosenProfile)) {
                pages = (List<List<String>>) ((HashMap<?, ?>)object).get("pages");
            }
        }

        return pages;
    }

    private Map<String, Object> getConfig(ModuleModel moduleModel) {
        return moduleModel.getConfig();
    }

    private List<String> getDefaultPage(ModuleModel moduleModel) {
        Map<String, Object> config = getConfig(moduleModel);
        if (config.containsKey("defaultPage") && config.get("defaultPage") instanceof List) {
            List<String> defaultPage = (List<String>) config.get("defaultPage");

            return defaultPage;
        } else {
            return new ArrayList<>();
        }
    }

    private void showSelectionModuleDialog(OnSelectionModuleClick callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose a module")
                .setItems(allModules, (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                    String selected = allModules[i];
                    callback.onClick(selected);
                })
                .show();
    }

    private void showInputDialog(Helper.Callback<String> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle("Input profile name");

        final EditText input = new EditText(getContext());
        input.setHint("Profile name");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok), (dialog, which) -> {
            String enteredText = input.getText().toString().trim();
            callback.onCall(enteredText);
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private interface OnSelectionModuleClick {
        void onClick(String value);
    }
}