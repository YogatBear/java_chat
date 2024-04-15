package com.arnacon.java_chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arnacon.chat_library.ChatSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private final List<ChatSession> sessions; // Make it non-final for updating
    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    private OnItemClickListener onItemClickListener;

    public SessionAdapter(Context context, List<ChatSession> sessions) {
        this.sessions = new ArrayList<>(sessions); // Initialize with a copy of the sessions list
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.private_session, parent, false);
        return new SessionViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.sessionNameTextView.setText(session.getSessionName());
        holder.lastMessageTextView.setText(dateFormat.format(session.getLastMessage()));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void updateSessions(List<ChatSession> newSessions) {
        this.sessions.clear(); // Clear the existing sessions
        this.sessions.addAll(newSessions); // Add all the new sessions
        notifyDataSetChanged(); // Notify the adapter of data change to refresh the RecyclerView
    }

    public class SessionViewHolder extends RecyclerView.ViewHolder {
        final TextView sessionNameTextView;
        final TextView lastMessageTextView;

        public SessionViewHolder(@NonNull View itemView, SessionAdapter adapter) {
            super(itemView);
            sessionNameTextView = itemView.findViewById(R.id.sessionNameTextView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && adapter.onItemClickListener != null) {
                    adapter.onItemClickListener.onItemClick(sessions.get(position).getSessionId());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String sessionId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
