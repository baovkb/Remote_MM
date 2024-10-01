package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.ConfigAdapter;
import com.vkbao.remotemm.databinding.FragmentEditConfigBinding;
import com.vkbao.remotemm.model.ModuleModel;
import com.vkbao.remotemm.viewmodel.ModuleListViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditConfigFragment extends Fragment {
    private FragmentEditConfigBinding binding;
    private ModuleModel module;

    private static final String TAG = "EditConfigFragment";

    public EditConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            module = (ModuleModel) getArguments().getSerializable("module");
            Log.d(TAG, "module: " + module);
        }

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        if (module != null) {
            initMenuToolbar();
            binding.moduleName.setText(module.getModule());
            initPosition();
            initConfig();
        }


//        moduleListViewModel.getModuleLiveData().observe(getViewLifecycleOwner(), moduleModels -> {
//            if (module == null) return;
//
//            ModuleModel currentModule = null;
//            for (ModuleModel module: moduleModels) {
//                if (module.getModule().equals(this.module)) {
//                    currentModule = module;
//                    break;
//                }
//            }
//
//            if (currentModule == null) return;
//
//
//
//        });
    }

   public void initMenuToolbar() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.edit_config_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.save_config) {

                } else if (menuItem.getItemId() == android.R.id.home) {
                    getParentFragmentManager().popBackStack();
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
   }

    public void initPosition() {
        List<String> positionList = new ArrayList<>();
        positionList.add("top_bar");
        positionList.add("top_left");
        positionList.add("top_center");
        positionList.add("top_right");
        positionList.add("upper_third");
        positionList.add("middle_center");
        positionList.add("lower_third");
        positionList.add("bottom_left");
        positionList.add("bottom_center");
        positionList.add("bottom_right");
        positionList.add("bottom_bar");
        positionList.add("fullscreen_above");
        positionList.add("fullscreen_below");

        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, positionList);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.positionSpn.setAdapter(positionAdapter);

        //set selected module
        if (positionList.contains(module.getPosition())) {
            binding.positionSpn.setSelection(positionList.indexOf(module.getPosition()));
        }
    }

    public void initConfig() {
        ConfigAdapter adapter = new ConfigAdapter(module.getConfig());
        binding.configs.setAdapter(null);
        binding.configs.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        binding.configs.setAdapter(adapter);
    }
}