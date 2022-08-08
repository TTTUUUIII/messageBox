package com.autolink.msbox.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.autolink.msbox.FMCallback;
import com.autolink.msbox.MainActivity;
import com.autolink.msbox.MessageHandler;
import com.autolink.msbox.MsgBoxViewModel;
import com.autolink.msbox.R;
import com.autolink.msbox.util.Utils;

import java.util.ArrayList;

public class UserListFragment extends BaseFragment {
    private static final String TAG = UserListFragment.class.getSimpleName();
    protected static final String PAR_M_INFO = "minInfo";
    private static String mineInfo;
    private ArrayList<String> mUserList = new ArrayList<>(userSet);
    private RecyclerView mUserListRecyclerView;
    private static FMCallback callback;
    private MListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = (MainActivity)requireActivity();
        mineInfo = getArguments().getString(PAR_M_INFO);
        viewModel = new ViewModelProvider(requireActivity()).get(MsgBoxViewModel.class);
        mListAdapter = new MListAdapter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity)requireActivity()).getSupportActionBar().setWindowTitle(getString(R.string.user_list_title));
        ((MainActivity)requireActivity()).getSupportActionBar().setSubtitle("");
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        mUserListRecyclerView = view.findViewById(R.id.user_list_recycler_view);
        mUserListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserList.clear();
        mUserList.addAll(userSet);
        mListAdapter.submitList(mUserList);
        mUserListRecyclerView.setAdapter(mListAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_user_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.send_broadcast:
                SendCastFragment sendCastFragment = SendCastFragment.newInstance();
                sendCastFragment.getArguments().putString(SendCastFragment.USERINFO, mineInfo);
                callback.replace(sendCastFragment, true);
                break;
            default:
                // do nothing
        }
        return true;
    }

    @Override
    public boolean msgHandle(String msg) {
        super.msgHandle(msg);
        boolean isHandle = true;
        String[] resp = msg.split("~");
        // 指令
        String order = resp[0];
        switch (order) {
            case "SERVER:LOGIN":
            case "SERVER:LOGOUT":
                mUserList.clear();
                mUserList.addAll(userSet);
                mListAdapter.notifyDataSetChanged();
                break;
            default:
                isHandle = false;
        }
        return isHandle;
    }

    private static class MViewHolder<T> extends RecyclerView.ViewHolder{

        private TextView mUsernameTextView;
        private TextView mUserIpTextView;
        public MViewHolder(View itemView) {
            super(itemView);
        }
        public void bind(String resp){
            String[] info = resp.split(":");
            mUsernameTextView = this.itemView.findViewById(R.id.username_textview);
            mUserIpTextView = this.itemView.findViewById(R.id.user_ip_text_view);
            mUsernameTextView.setText(info[1]);
            mUserIpTextView.setText(info[0]);
            SendMsgFragment sendMsgFragment = SendMsgFragment.newInstance();
            sendMsgFragment.getArguments()
                    .putString(SendMsgFragment.PAR_USER_INFO, resp);
            sendMsgFragment.getArguments()
                    .putString(SendMsgFragment.PAR_M_INFO, mineInfo);
            this.itemView.setOnClickListener(
                    view -> callback.replace(sendMsgFragment, true)
            );
        }

        public static MViewHolder from(ViewGroup parent){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_user, parent, false);
            return new MViewHolder(view);
        }

    }
    private class MListAdapter extends ListAdapter {

        protected MListAdapter() {
            super(new DiffUtil.ItemCallback() {
                @Override
                public boolean areItemsTheSame(@NonNull Object oldItem, Object newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(Object oldItem, @NonNull Object newItem) {
                    return ((String)oldItem).equals(newItem);
                }
            });
        }

        @NonNull

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return MViewHolder.from(parent);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (userSet.size() > 0){
                ((MViewHolder)holder).bind(new ArrayList<>(userSet).get(position));
            }
        }

    }

    public static UserListFragment newInstance(){
        UserListFragment instance = new UserListFragment();
        Bundle bundle = new Bundle();
        instance.setArguments(bundle);
        return instance;
    }
}
