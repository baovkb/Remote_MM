package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.ModuleAdapter;
import com.vkbao.remotemm.adapter.ModuleByPageAdapter;
import com.vkbao.remotemm.clients.WebsocketClientManager;
import com.vkbao.remotemm.databinding.FragmentMainBinding;
import com.vkbao.remotemm.model.ModuleModel;
import com.vkbao.remotemm.model.ModulesByPageModel;
import com.vkbao.remotemm.model.ModulesByPageResponse;
import com.vkbao.remotemm.model.SystemInfoResponse;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private WebsocketViewModel websocketViewModel;

    public MainFragment() {
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
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        initView();
    }

    public void initView() {
        websocketViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message.equals("failure")) {
                Toast.makeText(requireActivity(), getResources().getString(R.string.toast_connect_fail), Toast.LENGTH_SHORT).show();
            } else if (message.equals("error_url")) {
                Toast.makeText(requireActivity(), getResources().getString(R.string.toast_wrong_url), Toast.LENGTH_SHORT).show();
            } else {
                Gson gson = new Gson();
                Map<String, Object> map = gson.fromJson(message, new TypeToken<Map<String, Object>>(){}.getType());

                if (map.get("action").toString().equals("system info")) {
                    SystemInfoResponse sysInfo = gson.fromJson(message, SystemInfoResponse.class);

                    //volume
                    binding.seekbarSpeaker.setProgress(sysInfo.getSpeaker());
                    binding.seekbarRecorder.setProgress(sysInfo.getRecorder());

                    //all modules
                    initAllModules(sysInfo.getAllModules());

                    //module by page
                    ModulesByPageResponse modulesByPageResponse = sysInfo.getModulesByPage();
                    initModuleByPage(
                            modulesByPageResponse.getPageModules(),
                            modulesByPageResponse.getPage(),
                            modulesByPageResponse.getTotalPage());
                } else if (map.get("action").toString().equals("volume")) {
                    binding.seekbarSpeaker.setProgress(((Double)((Map)map.get("data")).get("speaker")).intValue());
                    binding.seekbarRecorder.setProgress(((Double)((Map)map.get("data")).get("recorder")).intValue());
                } else if (map.get("action").toString().equals("all modules")) {
                    String dataJson = gson.toJson(map.get("data"));
                    Type moduleListType = new TypeToken<List<ModuleModel>>() {}.getType();
                    List<ModuleModel> moduleList = gson.fromJson(dataJson, moduleListType);

                    initAllModules(moduleList);
                } else if (map.get("action").toString().equals("modules by page")) {

                }
            }
        });
    }

    private void initAllModules(List<ModuleModel> moduleList) {
        ModuleAdapter moduleAdapter = new ModuleAdapter(moduleList, module -> {
            //navigate to edit config fragment
            Log.d("test", "module is click: " + module);
        });
        binding.moduleList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        binding.moduleList.setAdapter(null);
        binding.moduleList.setAdapter(moduleAdapter);
    }

    public void initModuleByPage(List<ModulesByPageModel> itemList, int page, int totalPage) {
        binding.pageStart.setText(String.valueOf(page + 1));
        binding.pageEnd.setText(String.valueOf(totalPage));
        ModuleByPageAdapter adapter = new ModuleByPageAdapter(itemList);
        binding.moduleByPageList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        binding.moduleByPageList.setAdapter(null);
        binding.moduleByPageList.setAdapter(adapter);
    }
}