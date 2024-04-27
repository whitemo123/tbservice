package com.android.tbservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

    private List<String> logMessages;

    public LogAdapter(List<String> logMessages) {
        this.logMessages = logMessages;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_item, parent, false);
        return new LogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        String logMessage = logMessages.get(position);
        holder.logTextView.setText(logMessage);
    }

    @Override
    public int getItemCount() {
        return logMessages != null ? logMessages.size() : 0;
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView logTextView;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            logTextView = itemView.findViewById(R.id.log_item_text);
        }
    }

    // 可选：添加、删除日志条目的公共方法，供外部调用
    public void addLog(String logMessage) {
        int index = logMessages.size();
        logMessages.add(logMessage);
        if (index > 150) {
            logMessages.subList(0, 50).clear();
            notifyDataSetChanged();
        } else {
            notifyItemInserted(index);
        }
    }
}
