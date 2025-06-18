package com.vkbao.remotemm.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.vkbao.remotemm.R;
import com.vkbao.remotemm.databinding.DialogAddNodeBinding;
import com.vkbao.remotemm.databinding.JsonNodeItemBinding;
import com.vkbao.remotemm.helper.Helper;
import com.vkbao.remotemm.model.JsonNode;
import com.vkbao.remotemm.views.dialogs.AddNodeDialog;
import com.vkbao.remotemm.views.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonTreeAdapter extends RecyclerView.Adapter<JsonTreeAdapter.JsonViewHolder> {

    private List<JsonNode> visibleNodes = new ArrayList<>();
    private JsonNode rootNode;
    private Context context;

    public void setRoot(JsonNode root) {
        this.rootNode = root;
        updateVisibleNodes();
    }

    private void flattenNode(JsonNode node, int level) {
        node.level = level;
        visibleNodes.add(node);
        if ((node.type == JsonNode.Type.OBJECT || node.type == JsonNode.Type.ARRAY) && node.expanded) {
            for (JsonNode child : node.children) {
                flattenNode(child, level + 1);
            }
        }
    }

    private void updateVisibleNodes() {
        visibleNodes.clear();
        if (rootNode != null) {
            flattenNode(rootNode, 0);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JsonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        JsonNodeItemBinding binding = JsonNodeItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new JsonViewHolder(binding);
    }

    private List<String> parseType() {
        List<String> typeList = new ArrayList<>();
        for (JsonNode.Type type: JsonNode.Type.values()) {
            typeList.add(type.name());
        }

        return typeList;
    }

    private void setupTypeSpinner(JsonViewHolder holder, JsonNode node) {
        if (node.children.isEmpty()) {
            holder.binding.typeSpinner.setVisibility(View.VISIBLE);
        } else {
            holder.binding.typeSpinner.setVisibility(View.GONE);
            return;
        }

        // setup type spinner
        holder.binding.typeSpinner.setText(node.type.name());
    }

    @Override
    public void onBindViewHolder(@NonNull JsonViewHolder holder, int position) {
        JsonNode node = visibleNodes.get(position);

        // Indentation
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.binding.indentView.getLayoutParams();
        if (node.children.isEmpty()) {
            int px = Helper.convertDpToPx(context, 12 + 9);
            params.width = node.level * px;
        } else {
            int px = Helper.convertDpToPx(context, 12);
            params.width = node.level * px;
        }
        holder.binding.indentView.setLayoutParams(params);

        holder.binding.keyText.setText(node.key);

        setupTypeSpinner(holder, node);

        if (holder.currentWatcher != null) {
            holder.binding.valueText.removeTextChangedListener(holder.currentWatcher);
        }

        if (node.type == JsonNode.Type.OBJECT || node.type == JsonNode.Type.ARRAY) {
            holder.binding.valueText.setVisibility(View.GONE);
            holder.binding.btnToggle.setVisibility(View.VISIBLE);
            holder.binding.btnToggle.setImageResource(
                    node.expanded ? R.drawable.ic_expand_less : R.drawable.ic_expand_more
            );
        } else {
            holder.binding.valueText.setVisibility(View.VISIBLE);
            holder.binding.valueText.setText(node.value != null ? node.value.toString() : "");
            holder.binding.btnToggle.setVisibility(View.GONE);
        }

        // Toggle expansion
        holder.binding.btnToggle.setOnClickListener(v -> {
            node.expanded = !node.expanded;
            setRoot(getRootNode()); // re-flatten tree
        });

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String input = s.toString();
                    Object parsedValue = parseTypedValue(input, node.type);
                    node.value = parsedValue;
                    holder.binding.valueText.setBackgroundResource(R.drawable.outline_bg);
                } catch (Exception e) {
                    holder.binding.valueText.setBackgroundResource(R.drawable.outline_error_bg);
                }
            }
        };

        if (node.type != JsonNode.Type.OBJECT && node.type != JsonNode.Type.ARRAY) {
            holder.binding.valueText.addTextChangedListener(watcher);
            holder.currentWatcher = watcher;
        }

        // Delete button logic
        holder.binding.deleteButton.setOnClickListener(v -> {
            JsonNode parent = node.parent;
            if (parent != null) {
                parent.children.remove(node);
                updateVisibleNodes();
            }
        });

        // Add button logic
        holder.binding.addButton.setOnClickListener(v -> {
            if (node.type != JsonNode.Type.OBJECT && node.type != JsonNode.Type.ARRAY) {
                Toast.makeText(context, "Cannot add to primitive type", Toast.LENGTH_SHORT).show();
                return;
            }

            showAddNoteDialog(node, holder.binding, parseType());
        });

        // Show or hide add/delete based on context
        holder.binding.addButton.setVisibility(
                (node.type == JsonNode.Type.OBJECT || node.type == JsonNode.Type.ARRAY) ? View.VISIBLE : View.GONE);

        holder.binding.deleteButton.setVisibility(
                node.parent != null ? View.VISIBLE : View.GONE); // hide root delete
    }

    private Object parseTypedValue(String input, JsonNode.Type type) throws IllegalArgumentException {
        try {
            Gson gson = new Gson();
            switch (type) {
                case NUMBER:
                    if (input.contains(".")) {
                        return Double.parseDouble(input);
                    } else {
                        return Integer.parseInt(input);
                    }

                case BOOLEAN:
                    if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                        return Boolean.parseBoolean(input);
                    }
                    throw new IllegalArgumentException("Value must be 'true' or 'false'");

                case NULL:
                    if (input.trim().isEmpty() || input.equalsIgnoreCase("null")) {
                        return null;
                    }
                    throw new IllegalArgumentException("Null type must have empty or 'null' value");

                case OBJECT: {
                    JsonElement element = JsonParser.parseString(input);
                    if (!element.isJsonObject()) {
                        throw new IllegalArgumentException("Invalid JSON object");
                    }
                    return gson.fromJson(element, Map.class);
                }

                case ARRAY: {
                    JsonElement element = JsonParser.parseString(input);
                    if (!element.isJsonArray()) {
                        throw new IllegalArgumentException("Invalid JSON array");
                    }
                    return gson.fromJson(element, List.class);
                }

                case STRING:
                default:
                    return input;
            }
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON syntax");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format");
        }
    }


    @Override
    public int getItemCount() {
        return visibleNodes.size();
    }

    public JsonNode getRootNode() {
        for (JsonNode node : visibleNodes) {
            if (node.parent == null) return node;
        }
        return null;
    }

    private void showAddNoteDialog(JsonNode node, JsonNodeItemBinding binding, List<String> spinnerData) {
        AddNodeDialog addNodeDialog = new AddNodeDialog();
        addNodeDialog.setSpinnerData(spinnerData);
        addNodeDialog.setPositiveBtn((keyText, valueText, typeStr) -> {
            try {
                JsonNode.Type type = JsonNode.Type.valueOf(typeStr);
                Object value = parseTypedValue(valueText, type);

                JsonNode newNode = new JsonNode();
                newNode.parent = node;
                newNode.key = keyText;
                newNode.type = type;
                newNode.value = value;
                newNode.children = new ArrayList<>();

                if (type == JsonNode.Type.OBJECT) {
                    Map<String, Object> map = (Map<String, Object>) value;
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        JsonNode child = new JsonNode();
                        child.parent = newNode;
                        child.key = entry.getKey();
                        child.value = entry.getValue();
                        child.type = inferTypeFromValue(entry.getValue());
                        child.children = new ArrayList<>();
                        newNode.children.add(child);
                    }
                } else if (type == JsonNode.Type.ARRAY) {
                    List<Object> list = (List<Object>) value;
                    for (int i = 0; i < list.size(); i++) {
                        JsonNode child = new JsonNode();
                        child.parent = newNode;
                        child.key = String.valueOf(i);
                        child.value = list.get(i);
                        child.type = inferTypeFromValue(list.get(i));
                        child.children = new ArrayList<>();
                        newNode.children.add(child);
                    }
                }

                node.expanded = true;
                node.children.add(newNode);
                updateVisibleNodes();
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        addNodeDialog.show(((FragmentActivity) context).getSupportFragmentManager(), null);
    }

    private JsonNode.Type inferTypeFromValue(Object value) {
        if (value == null) return JsonNode.Type.NULL;
        else if (value instanceof Map) return JsonNode.Type.OBJECT;
        else if (value instanceof List) return JsonNode.Type.ARRAY;
        else if (value instanceof Boolean) return JsonNode.Type.BOOLEAN;
        else if (value instanceof Number) return JsonNode.Type.NUMBER;
        else return JsonNode.Type.STRING;
    }

    static class JsonViewHolder extends RecyclerView.ViewHolder {
        JsonNodeItemBinding binding;
        TextWatcher currentWatcher;

        public JsonViewHolder(@NonNull JsonNodeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

