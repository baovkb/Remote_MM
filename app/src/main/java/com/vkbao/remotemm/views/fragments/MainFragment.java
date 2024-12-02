package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
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
import com.vkbao.remotemm.model.VolumeModel;
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
    private String url;
    private String username;
    private String password;
    private Gson gson;

    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gson = new Gson();
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
            username = getArguments().getString("username");
            password = getArguments().getString("password");
        }

        ((MainActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        getFirstTimeData();
        initConnectionState();
        initVolume();
        initModuleByPage();
        initAllModules();
        handleSystemBtn();
    }

    private void getFirstTimeData() {
        Map<String, Object> cmd = new LinkedTreeMap<>();
        cmd.put("action", "request system info");
        websocketViewModel.sendMessage(gson.toJson(cmd));
    }

    public void initVolume() {
        websocketViewModel.getVolumeLiveData().observe(getViewLifecycleOwner(), volumeModel -> {
            binding.seekbarSpeaker.setProgress(volumeModel.getSpeaker());
            binding.speakerValue.setText(String.valueOf(volumeModel.getSpeaker()));
            binding.seekbarRecorder.setProgress(volumeModel.getRecorder());
            binding.recorderValue.setText(String.valueOf(volumeModel.getRecorder()));
        });

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

    public void initConnectionState() {
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


        websocketViewModel.getConnectionStateLiveData().observe(getViewLifecycleOwner(), connectionState -> {
            switch (connectionState) {
                case CONNECTING:
                    binding.lostConnectionLayout.setVisibility(View.VISIBLE);
                    break;
                case DISCONNECTED:
                    countDownTimer.start();
                    break;
                case UNAUTHENTICATED:
                    Map<String, Object> msg = new LinkedTreeMap<>();
                    msg.put("action", "login");
                    msg.put("username", username);
                    msg.put("password", password);
                    websocketViewModel.sendMessage(gson.toJson(msg));
                    break;

                case AUTHENTICAION_FAILED:
                    Toast.makeText(requireActivity(), getResources().getString(R.string.toast_authen_failed), Toast.LENGTH_SHORT).show();
                    FragmentManager fragmentManager = getParentFragmentManager();
                    fragmentManager
                            .beginTransaction()
                            .replace(R.id.main, new ConnectFragment())
                            .commit();
                    break;
                case CONNECTED:
                    countDownTimer.cancel();
                    binding.lostConnectionLayout.setVisibility(View.GONE);
                default:
            }
        });
    }

    private void initAllModules() {
        websocketViewModel.getAllModuleLiveData().observe(getViewLifecycleOwner(), moduleList -> {
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
        });
    }

    public void initModuleByPage() {
        websocketViewModel.getModulesByPageLiveData().observe(getViewLifecycleOwner(), modulesByPageResponse -> {
            binding.pageStart.setText(String.valueOf(modulesByPageResponse.getPage() + 1));
            binding.pageEnd.setText(String.valueOf(modulesByPageResponse.getTotalPage()));
            ModuleByPageAdapter adapter = new ModuleByPageAdapter(modulesByPageResponse.getPageModules(), modulesByPage -> {
                Map<String, Object> payload = new LinkedTreeMap<>();
                List<ModulesByPageModel> modules = new ArrayList<>();
                modules.add(modulesByPage);

                payload.put("action", "request update modules");
                payload.put("data", modules);
                websocketViewModel.sendMessage(gson.toJson(payload));
            });
            binding.moduleByPageList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            binding.moduleByPageList.setAdapter(null);
            binding.moduleByPageList.setAdapter(adapter);
        });

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

    public void handleSystemBtn() {
        binding.restartBtn.setOnClickListener(view -> {
            Map<String, String> cmd = new LinkedTreeMap<>();
            cmd.put("action", "reboot magic mirror");
            websocketViewModel.sendMessage(gson.toJson(cmd));
        });
    }
}