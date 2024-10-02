package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.ModuleAdapter;
import com.vkbao.remotemm.adapter.ModuleByPageAdapter;
import com.vkbao.remotemm.databinding.FragmentMainBinding;
import com.vkbao.remotemm.helper.CustomJson;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private WebsocketViewModel websocketViewModel;
    private static Gson gson;
    private String url;

    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Map.class, new CustomJson());
        gson = gsonBuilder.create();
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

        if (getArguments() != null) {
            url = getArguments().getString("url");
        }

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        initView();
    }

    public void initView() {
        CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (url != null) {
                    websocketViewModel.connect(url);
                }
            }
        };

        AtomicBoolean isFirstLostConnection = new AtomicBoolean(false);

        websocketViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            switch (message) {
                case "connecting":
                    Log.d(TAG, "connecting");
                    binding.lostConnectionLayout.setVisibility(View.VISIBLE);
                    break;
                case "failure":
                    if (!isFirstLostConnection.get()) {
                        Toast.makeText(requireActivity(), getResources().getString(R.string.toast_connect_fail), Toast.LENGTH_SHORT).show();
                        isFirstLostConnection.set(true);
                    }
                    countDownTimer.start();
                    break;
                case "disconnected":
                    Toast.makeText(requireActivity(), getResources().getString(R.string.toast_disconnect), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    isFirstLostConnection.set(false);
                    countDownTimer.cancel();
                    binding.lostConnectionLayout.setVisibility(View.GONE);

                    Map<String, Object> map;
                    try {
                        map = gson.fromJson(message, new TypeToken<Map<String, Object>>() {
                        }.getType());
                    } catch (Exception e) {
                        return;
                    }

                    if (map.get("action").toString().equals("system info")) {
                        SystemInfoResponse sysInfo = gson.fromJson(message, SystemInfoResponse.class);

                        //volume
                        binding.seekbarSpeaker.setProgress(sysInfo.getSpeaker());
                        binding.speakerValue.setText(String.valueOf(sysInfo.getSpeaker()));
                        binding.seekbarRecorder.setProgress(sysInfo.getRecorder());
                        binding.recorderValue.setText(String.valueOf(sysInfo.getRecorder()));

                        //all modules
                        initAllModules(sysInfo.getAllModules());

                        //module by page
                        ModulesByPageResponse modulesByPageResponse = sysInfo.getModulesByPage();
                        initModuleByPage(
                                modulesByPageResponse.getPageModules(),
                                modulesByPageResponse.getPage(),
                                modulesByPageResponse.getTotalPage());
                    } else if (map.get("action").toString().equals("volume")) {
                        binding.seekbarSpeaker.setProgress((Integer) ((Map)map.get("data")).get("speaker"));
                        binding.seekbarRecorder.setProgress((Integer) ((Map)map.get("data")).get("recorder"));
                    } else if (map.get("action").toString().equals("all modules")) {
                        String dataJson = gson.toJson(map.get("data"));
                        Type moduleListType = new TypeToken<List<ModuleModel>>() {}.getType();
                        List<ModuleModel> moduleList = gson.fromJson(dataJson, moduleListType);

                        initAllModules(moduleList);
                    } else if (map.get("action").toString().equals("modules by page")) {
                        String dataJson = gson.toJson(map.get("data"));

                        ModulesByPageResponse modulesByPageResponse = gson.fromJson(dataJson, ModulesByPageResponse.class);
                        initModuleByPage(
                                modulesByPageResponse.getPageModules(),
                                modulesByPageResponse.getPage(),
                                modulesByPageResponse.getTotalPage());
                    }
                    break;
            }
        });

        handleSystemBtn();
        handleVolume();
        handlePage();
    }

    public void handleSystemBtn() {
        binding.restartBtn.setOnClickListener(view -> {
            Map<String, String> cmd = new LinkedTreeMap<>();
            cmd.put("action", "reboot magic mirror");
            websocketViewModel.sendMessage(gson.toJson(cmd));
        });
    }

    public void handleVolume() {
        binding.seekbarSpeaker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.speakerValue.setText(String.valueOf(binding.seekbarSpeaker.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Map<String, Object> cmd = new LinkedTreeMap<>();
                cmd.put("action", "request speaker volume");
                cmd.put("data", binding.seekbarSpeaker.getProgress());
                websocketViewModel.sendMessage(gson.toJson(cmd));
            }
        });

        binding.seekbarRecorder.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.recorderValue.setText(String.valueOf(binding.seekbarRecorder.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Map<String, Object> cmd = new LinkedTreeMap<>();
                cmd.put("action", "request recorder volume");
                cmd.put("data", binding.seekbarRecorder.getProgress());
                websocketViewModel.sendMessage(gson.toJson(cmd));
            }
        });
    }

    private void handlePage() {
        binding.previousPageBtn.setOnClickListener(view -> {
            Map<String, String> cmd = new LinkedTreeMap<>();
            cmd.put("action", "request previous page");
            websocketViewModel.sendMessage(gson.toJson(cmd));
        });

        binding.nextPageBtn.setOnClickListener(view -> {
            Map<String, String> cmd = new LinkedTreeMap<>();
            cmd.put("action", "request next page");
            websocketViewModel.sendMessage(gson.toJson(cmd));
        });
    }

    private void initAllModules(List<ModuleModel> moduleList) {
        ModuleAdapter moduleAdapter = new ModuleAdapter(moduleList, module -> {
            //navigate to edit config fragment
            Bundle bundle = new Bundle();
            bundle.putSerializable("module", module);
            EditConfigFragment editConfigFragment = new EditConfigFragment();
            editConfigFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.main, editConfigFragment)
                    .addToBackStack(null)
                    .commit();
        });
        binding.moduleList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        binding.moduleList.setAdapter(null);
        binding.moduleList.setAdapter(moduleAdapter);
    }

    public void initModuleByPage(List<ModulesByPageModel> itemList, int page, int totalPage) {
        binding.pageStart.setText(String.valueOf(page + 1));
        binding.pageEnd.setText(String.valueOf(totalPage));
        ModuleByPageAdapter adapter = new ModuleByPageAdapter(itemList, modulesByPage -> {
            Map<String, Object> payload = new LinkedTreeMap<>();
            List<ModulesByPageModel> modules = new ArrayList<>();
            modules.add(modulesByPage);

            payload.put("action", "request update modules");
            payload.put("data", modules);

            Log.d("test", gson.toJson(payload));
            websocketViewModel.sendMessage(gson.toJson(payload));
        });
        binding.moduleByPageList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
        binding.moduleByPageList.setAdapter(null);
        binding.moduleByPageList.setAdapter(adapter);
    }
}