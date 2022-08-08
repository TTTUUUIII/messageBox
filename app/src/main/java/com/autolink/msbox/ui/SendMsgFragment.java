package com.autolink.msbox.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.autolink.msbox.DataRepository;
import com.autolink.msbox.MainActivity;
import com.autolink.msbox.MsgBoxViewModel;
import com.autolink.msbox.R;

import java.util.ArrayList;

public class SendMsgFragment extends BaseFragment {
    private static String TAG = SendMsgFragment.class.getSimpleName();
    private DataRepository repository = DataRepository.getInstance();
    private MsgBoxViewModel viewModel;
    private static ArrayList<String> msgBox = new ArrayList<>();
    protected static final String PAR_USER_INFO = "targetUser";
    protected static final String PAR_M_INFO = "mineInfo";
    private String mTargetUserInfo;
    private String mineInfo;
    private RecyclerView mShowUserMsgRecyclerView;
    private EditText mMsgEditText;
    private Button mSendBtn;
    private MAdapter mAdapter;

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        viewModel = new ViewModelProvider(requireActivity()).get(MsgBoxViewModel.class);
        mTargetUserInfo = getArguments().getString(PAR_USER_INFO);
        mineInfo = getArguments().getString(PAR_M_INFO);
        mAdapter = new MAdapter();
        //Log.d(TAG, mineInfo);
        filterMsg();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity)requireActivity()).getSupportActionBar().setWindowTitle(getString(R.string.send_msg));
        ((MainActivity)requireActivity()).getSupportActionBar().setSubtitle(mTargetUserInfo);
        View view = inflater.inflate(R.layout.fragment_send_msg, container, false);
        mShowUserMsgRecyclerView = view.findViewById(R.id.show_msg_recyclerView);
        mMsgEditText = view.findViewById(R.id.msg_content_edit);
        mSendBtn = view.findViewById(R.id.send_btn);
        mShowUserMsgRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mShowUserMsgRecyclerView.setAdapter(mAdapter);
        mAdapter.submitList(msgBox);
        setListener();
        return view;
    }

    @Override
    public boolean msgHandle(String msg) {
        super.msgHandle(msg);
        boolean isHandle = true;
        String[] resp = msg.split("~");
        // 指令
        String order = resp[0];
        switch (order) {
            case "SERVER:MSG":
                filterMsg();
                mAdapter.notifyDataSetChanged();
                break;
            default:
                isHandle = false;
        }
        return isHandle;
    }

    private void filterMsg(){
        msgBox.clear();
        for (String msg: messageBox){
            String[] split = msg.split(":");
            if (msgBox.size() > 0 && msgBox.get(msgBox.size() - 1).equals(msg)){
                continue;
            }
            if ((split[0]+ ":" + split[1]).equals(mTargetUserInfo) || split[1].equals(mineInfo)) {
                msgBox.add(msg);
            }
        }
    }

    private void setListener(){
        mSendBtn.setOnClickListener(view -> {
            String req = String.format("client:msg~%s:%s", mTargetUserInfo.split(":")[0], mMsgEditText.getText());
            repository.write(viewModel.getOut(), req);
            mMsgEditText.setText("");
        });
    }

    public static SendMsgFragment newInstance(){
        SendMsgFragment instance = new SendMsgFragment();
        Bundle bundle = new Bundle();
        instance.setArguments(bundle);
        return instance;
    }

    private static class MViewHolder extends RecyclerView.ViewHolder{
        private TextView mShowInfoTextView;
        private TextView mShowMsgTextView;

        public MViewHolder(@NonNull View itemView) {
            super(itemView);
            mShowInfoTextView = itemView.findViewById(R.id.show_info_text_view);
            mShowMsgTextView = itemView.findViewById(R.id.show_msg_text_view);
        }
        public void bind(String msg){
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

        public static MViewHolder from(ViewGroup parent, int viewType){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView;
            if (viewType == 0){
                itemView = inflater.inflate(R.layout.item_msg_self, parent, false);
            }else {
                itemView = inflater.inflate(R.layout.item_msg, parent, false);
            }
            return new MViewHolder(itemView);
        }
    }

    private class MAdapter extends ListAdapter<String, MViewHolder>{

        protected MAdapter() {
            super(new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                    return oldItem.equals(newItem);
                }
            });
        }

        @NonNull
        @Override
        public MViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return MViewHolder.from(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull MViewHolder holder, int position) {
            holder.bind(msgBox.get(position));
        }

        @Override
        public int getItemViewType(int position) {
            String msg = msgBox.get(position);
            String[] split = msg.split(":");
            if ((split[0] + ":" + split[1]).equals(mTargetUserInfo)){
                return 1;
            }
            return 0;
        }
    }
}
