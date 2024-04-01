package com.arnacon.java_chat;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arnacon.chat_library.DisplayedMessage;

import java.util.List;
import java.util.function.Consumer;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DisplayedMessage> messages;
    private String currentUser;
    private Consumer<Uri> onFileClick;

    private static final int VIEW_TYPE_MY_TEXT_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_TEXT_MESSAGE = 2;
    private static final int VIEW_TYPE_MY_FILE_MESSAGE = 3;
    private static final int VIEW_TYPE_OTHER_FILE_MESSAGE = 4;
    private static final int VIEW_TYPE_MY_IMAGE_MESSAGE = 5;
    private static final int VIEW_TYPE_OTHER_IMAGE_MESSAGE = 6;

    public MessageAdapter(List<DisplayedMessage> messages, String currentUser, Consumer<Uri> onFileClick) {
        this.messages = messages;
        this.currentUser = currentUser;
        this.onFileClick = onFileClick;
    }

    @Override
    public int getItemViewType(int position) {
        DisplayedMessage message = messages.get(position);
        if (message instanceof DisplayedMessage.TextMessage) {
            return message.getSender().equals(currentUser) ? VIEW_TYPE_MY_TEXT_MESSAGE : VIEW_TYPE_OTHER_TEXT_MESSAGE;
        } else if (message instanceof DisplayedMessage.FileMessage) {
            DisplayedMessage.FileMessage fileMessage = (DisplayedMessage.FileMessage) message;
            if (isImageFile(fileMessage.getFilename())) {
                return message.getSender().equals(currentUser) ? VIEW_TYPE_MY_IMAGE_MESSAGE : VIEW_TYPE_OTHER_IMAGE_MESSAGE;
            } else {
                return message.getSender().equals(currentUser) ? VIEW_TYPE_MY_FILE_MESSAGE : VIEW_TYPE_OTHER_FILE_MESSAGE;
            }
        }
        throw new IllegalArgumentException("Invalid message type");
    }

    private boolean isImageFile(String filename) {
        return filename.toLowerCase().endsWith(".jpeg") || filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".png");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_MY_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text, parent, false);
                return new MyMessageViewHolder(view);
            case VIEW_TYPE_OTHER_TEXT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_text, parent, false);
                return new OtherMessageViewHolder(view);
            case VIEW_TYPE_MY_FILE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_file, parent, false);
                return new MyFileMessageViewHolder(view, onFileClick);
            case VIEW_TYPE_OTHER_FILE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_file, parent, false);
                return new OtherFileMessageViewHolder(view, onFileClick);
            case VIEW_TYPE_MY_IMAGE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_image, parent, false);
                return new MyImageMessageViewHolder(view, onFileClick);
            case VIEW_TYPE_OTHER_IMAGE_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_image, parent, false);
                return new OtherImageMessageViewHolder(view, onFileClick);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayedMessage message = messages.get(position);
        boolean showDate = position == 0 || !message.getFormattedDate().equals(messages.get(position - 1).getFormattedDate());

        if (holder instanceof MyMessageViewHolder) {
            ((MyMessageViewHolder) holder).bind((DisplayedMessage.TextMessage) message, showDate);
        } else if (holder instanceof OtherMessageViewHolder) {
            ((OtherMessageViewHolder) holder).bind((DisplayedMessage.TextMessage) message, showDate);
        } else if (holder instanceof MyFileMessageViewHolder) {
            ((MyFileMessageViewHolder) holder).bind((DisplayedMessage.FileMessage) message, showDate);
        } else if (holder instanceof OtherFileMessageViewHolder) {
            ((OtherFileMessageViewHolder) holder).bind((DisplayedMessage.FileMessage) message, showDate);
        } else if (holder instanceof MyImageMessageViewHolder) {
            ((MyImageMessageViewHolder) holder).bind((DisplayedMessage.FileMessage) message, showDate);
        } else if (holder instanceof OtherImageMessageViewHolder) {
            ((OtherImageMessageViewHolder) holder).bind((DisplayedMessage.FileMessage) message, showDate);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(DisplayedMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<DisplayedMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView dateText;
        private TextView timeText;

        MyMessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.text_gchat_message_me);
            dateText = view.findViewById(R.id.text_gchat_date_me);
            timeText = view.findViewById(R.id.text_gchat_timestamp_me);
        }

        void bind(DisplayedMessage.TextMessage message, boolean showDate) {
            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            messageText.setText(message.getText());
            dateText.setText(message.getFormattedDate());
            timeText.setText(message.getFormattedTime());
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView dateText;
        private TextView timeText;

        OtherMessageViewHolder(View view) {
            super(view);
            messageText = view.findViewById(R.id.text_gchat_message_other);
            dateText = view.findViewById(R.id.text_gchat_date_other);
            timeText = view.findViewById(R.id.text_gchat_timestamp_other);
        }

        void bind(DisplayedMessage.TextMessage message, boolean showDate) {
            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            messageText.setText(message.getText());
            dateText.setText(message.getFormattedDate());
            timeText.setText(message.getFormattedTime());
        }
    }

    class MyFileMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView fileNameText;
        private TextView fileSizeText;
        private TextView dateText;
        private TextView timeText;
        private Uri fileUri;

        MyFileMessageViewHolder(View itemView, Consumer<Uri> onFileClick) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.text_file_name_me);
            fileSizeText = itemView.findViewById(R.id.text_file_size_me);
            dateText = itemView.findViewById(R.id.text_gchat_date_me);
            timeText = itemView.findViewById(R.id.text_gchat_timestamp_me);

            itemView.setOnClickListener(v -> onFileClick.accept(fileUri));
        }

        void bind(DisplayedMessage.FileMessage message, boolean showDate) {
            fileUri = message.getFileUri();
            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            fileNameText.setText(message.getFilename());
            fileSizeText.setText(String.valueOf(message.getFilesize()));
            dateText.setText(message.getFormattedDate());
            timeText.setText(message.getFormattedTime());
        }
    }

    // OtherFileMessageViewHolder implementation
    class OtherFileMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView fileNameText;
        private TextView fileSizeText;
        private TextView dateText;
        private TextView timeText;
        private Uri fileUri;

        OtherFileMessageViewHolder(View itemView, Consumer<Uri> onFileClick) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.text_file_name_other);
            fileSizeText = itemView.findViewById(R.id.text_file_size_other);
            dateText = itemView.findViewById(R.id.text_gchat_date_other);
            timeText = itemView.findViewById(R.id.text_gchat_timestamp_other);

            itemView.setOnClickListener(v -> onFileClick.accept(fileUri));
        }

        void bind(DisplayedMessage.FileMessage message, boolean showDate) {
            fileUri = message.getFileUri();
            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            fileNameText.setText(message.getFilename());
            fileSizeText.setText(String.valueOf(message.getFilesize()));
            dateText.setText(message.getFormattedDate());
            timeText.setText(message.getFormattedTime());
        }
    }

    // MyImageMessageViewHolder implementation
    class MyImageMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView dateText;
        private TextView timeText;
        private Uri imageUri;

        MyImageMessageViewHolder(View itemView, Consumer<Uri> onImageClick) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_message); // Adjust ID as necessary
            dateText = itemView.findViewById(R.id.text_image_date); // Adjust ID as necessary
            timeText = itemView.findViewById(R.id.text_image_timestamp); // Adjust ID as necessary

            itemView.setOnClickListener(v -> onImageClick.accept(imageUri));
        }

        void bind(DisplayedMessage.FileMessage message, boolean showDate) {
            imageUri = message.getFileUri();
            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            imageView.setImageURI(imageUri);
            dateText.setText(message.getFormattedDate());
            timeText.setText(message.getFormattedTime());
        }
    }

    // OtherImageMessageViewHolder implementation
    class OtherImageMessageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView senderNameText;
        private TextView dateText;
        private TextView timeText;
        private Uri imageUri;

        OtherImageMessageViewHolder(View itemView, Consumer<Uri> onImageClick) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_message_other);
            senderNameText = itemView.findViewById(R.id.text_gchat_user_other);
            dateText = itemView.findViewById(R.id.text_gchat_date_other);
            timeText = itemView.findViewById(R.id.text_gchat_timestamp_other);

            itemView.setOnClickListener(v -> onImageClick.accept(imageUri));
        }

        void bind(DisplayedMessage.FileMessage message, boolean showDate) {
            imageUri = message.getFileUri();
            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            imageView.setImageURI(imageUri);
            senderNameText.setText(message.getSender());
            dateText.setText(message.getFormattedDate());
            timeText.setText(message.getFormattedTime());
        }
    }
}