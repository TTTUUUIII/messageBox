package com.autolink.msbox.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.autolink.msbox.DataRepository;
import com.autolink.msbox.MainActivity;
import com.autolink.msbox.MessageHandler;
import com.autolink.msbox.MsgBoxViewModel;
import com.autolink.msbox.R;
import com.autolink.msbox.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SendCastFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendCastFragment extends BaseFragment {
    private static final String TAG = SendCastFragment.class.getSimpleName();
    private static final DataRepository repository = DataRepository.getInstance();
    public static final String USERINFO = "USERINFO";
    private String userInfo;
    private RecyclerView showMsgRecyclerView;
    private EditText mMsgEditText;
    private Button mSendBtn;
    private MListAdapter mAdapter;

    public static SendCastFragment newInstance() {
        SendCastFragment instance = new SendCastFragment();
        Bundle bundle = new Bundle();
        instance.setArguments(bundle);
        return instance;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        userInfo = getArguments().getString(USERINFO);
        mAdapter = new MListAdapter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setWindowTitle("广播消息");
        ((MainActivity) requireActivity()).getSupportActionBar().setSubtitle("");
        View view = inflater.inflate(R.layout.fragment_send_cast, container, false);
        mMsgEditText = view.findViewById(R.id.msg_content_edit);
        mSendBtn = view.findViewById(R.id.send_btn);
        showMsgRecyclerView = view.findViewById(R.id.show_msg_recyclerView);
        showMsgRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        showMsgRecyclerView.setAdapter(mAdapter);
        mAdapter.submitList(broadcastBox);
        setListener();
        return view;
    }

    private void setListener() {
        mSendBtn.setOnClickListener(view -> {
            String req = String.format("client:broadcast~%s", mMsgEditText.getText().toString());
            repository.write(this.viewModel.getOut(), req);
            mMsgEditText.setText("");
        });
    }

    @Override
    public boolean msgHandle(String msg) {
        super.msgHandle(msg);
        boolean isHandle = true;
        String[] resp = msg.split("~");
        // 指令
        String order = resp[0];
        switch (order) {
            case "SERVER:BRO":
                mAdapter.notifyDataSetChanged();
                break;
            default:
                isHandle = false;
        }
        return isHandle;
    }

    private static class MViewHolder extends RecyclerView.ViewHolder {
        private TextView mShowInfoTextView;
        private TextView mShowMsgTextView;

        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            mShowInfoTextView = itemView.findViewById(R.id.show_info_text_view);
            mShowMsgTextView = itemView.findViewById(R.id.show_msg_text_view);
        }

        public void bind(String msg) {
            String[] split = msg.split(":");
            String newMsg;
            if (itemView.getId() == R.id.self_msg_view){
                newMsg = String.format(" :%s[%s]", split[0], split[1]);
            }else {
                newMsg = String.format("%s[%s]: ", split[1], split[0]);
            }
            mShowInfoTextView.setText(newMsg);
            mShowMsgTextView.setText(split[2]);
        }

        public static MViewHolder form(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View itemView;
            switch (viewType){
                case 0:
                    itemView = inflater.inflate(R.layout.item_msg_self, viewGroup, false);
                    break;
                default:
                    itemView = inflater.inflate(R.layout.item_msg, viewGroup, false);
            }
            return new MViewHolder(itemView);
        }
    }

    private class MListAdapter extends ListAdapter {

        protected MListAdapter() {
            super(new DiffUtil.ItemCallback() {
                @Override
                public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
                    return ((String) oldItem).equals(newItem);
                }

            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return MViewHolder.form(parent, viewType);
        }

        @Override
        public int getItemViewType(int position) {
            String[] split = broadcastBox.get(position).split(":");
            if (split[1].equals(userInfo)){
                return 0;
            }
            return 1;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((MViewHolder) holder).bind(broadcastBox.get(position));
        }
    }
}