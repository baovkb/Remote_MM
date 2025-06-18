package com.vkbao.remotemm.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.adapter.JsonTreeAdapter;
import com.vkbao.remotemm.databinding.ConfigValueBinding;
import com.vkbao.remotemm.databinding.FragmentEditConfigBinding;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.model.JsonNode;
import com.vkbao.remotemm.model.ModuleModel;
import com.vkbao.remotemm.viewmodel.WebsocketViewModel;
import com.vkbao.remotemm.views.activities.MainActivity;
import com.vkbao.remotemm.views.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EditConfigFragment extends Fragment {
    private FragmentEditConfigBinding binding;
    private ModuleModel module;
    private WebsocketViewModel websocketViewModel;
    private JsonNode root;

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

        websocketViewModel = new ViewModelProvider(requireActivity()).get(WebsocketViewModel.class);

        if (module != null) {
            initMenuToolbar();
            binding.moduleName.setText(module.getModule());
            initPosition();
//            initConfig(module.getConfig());
            initJson();
        }
    }

    private void initJson() {
        RecyclerView recyclerView = binding.configTree;
        JsonTreeAdapter adapter = new JsonTreeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        JsonNode rootNode = buildJsonNodeTree(module.getConfig());
        adapter.setRoot(rootNode);
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
                    showConfirmDialog();
                } else if (menuItem.getItemId() == android.R.id.home) {
                    getParentFragmentManager().popBackStack();
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    public void showConfirmDialog() {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setMessage(getString(R.string.confirm_save_config));
        confirmDialog.setPositiveBtn(() -> {
            Gson gson = new Gson();
            if (root != null) {
                Map<String, Object> newConfig = (Map<String, Object>) jsonNodeToObject(root);
                Log.d(TAG, newConfig.toString());

                Map<String, Object> payload = new LinkedTreeMap<>();
                payload.put("action", "request save config");
                module.setConfig(newConfig);
                payload.put("data", module);

                websocketViewModel.sendMessage(gson.toJson(payload));
            }
        });

        confirmDialog.show(getChildFragmentManager(), null);
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

        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(getActivity(), R.layout.position_item, positionList);
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.positionSpn.setAdapter(positionAdapter);

        binding.positionSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                module.setPosition(positionList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //set selected module
        if (positionList.contains(module.getPosition())) {
            binding.positionSpn.setSelection(positionList.indexOf(module.getPosition()));
        }

    }

    public JsonNode buildJsonNodeTree(Map<String, Object> map) {
        root = new JsonNode();
        root.key = "config";
        root.type = JsonNode.Type.OBJECT;
        root.expanded = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            JsonNode child = mapToJsonNode(entry.getKey(), entry.getValue(), root);
            root.children.add(child);
        }

        return root;
    }

    private JsonNode mapToJsonNode(String key, Object value, JsonNode parent) {
        JsonNode node = new JsonNode();
        node.key = key;
        node.parent = parent;

        if (value instanceof Map) {
            node.type = JsonNode.Type.OBJECT;
            node.value = null;
            node.expanded = false;
            node.children = new ArrayList<>();
            Map<String, Object> subMap = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : subMap.entrySet()) {
                node.children.add(mapToJsonNode(entry.getKey(), entry.getValue(), node));
            }
        } else if (value instanceof List) {
            node.type = JsonNode.Type.ARRAY;
            node.value = null;
            node.expanded = false;
            node.children = new ArrayList<>();
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                Object element = list.get(i);
                node.children.add(mapToJsonNode(String.valueOf(i), element, node));
            }
        } else if (value instanceof Boolean) {
            node.type = JsonNode.Type.BOOLEAN;
            node.value = value;
        } else if (value instanceof Integer || value instanceof Double) {
            node.type = JsonNode.Type.NUMBER;
            node.value = value;
        } else if (value == null) {
            node.type = JsonNode.Type.NULL;
            node.value = null;
        } else {
            node.type = JsonNode.Type.STRING;
            node.value = value.toString();
        }

        return node;
    }

    public Object jsonNodeToObject(JsonNode node) {
        switch (node.type) {
            case OBJECT:
                Map<String, Object> map = new LinkedHashMap<>();
                for (JsonNode child : node.children) {
                    map.put(child.key, jsonNodeToObject(child));
                }
                return map;

            case ARRAY:
                List<Object> list = new ArrayList<>();
                for (JsonNode child : node.children) {
                    list.add(jsonNodeToObject(child));
                }
                return list;

            case BOOLEAN:
            case NUMBER:
            case STRING:
                return node.value;

            case NULL:
            default:
                return null;
        }
    }

}