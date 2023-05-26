package com.android.campusmoments.Fragment;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.android.campusmoments.Service.Config.*;
import com.android.campusmoments.Activity.DetailedActivity;
import com.android.campusmoments.Activity.HomeActivity;
import com.android.campusmoments.Activity.UserHomePageActivity;
import com.android.campusmoments.Adapter.MomentAdapter;
import com.android.campusmoments.R;
import com.android.campusmoments.Service.Moment;
import com.android.campusmoments.Service.Services;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MomentsFragment extends Fragment {
    private static final String TAG = "MomentsFragment";
    public Activity mActivity;
    private SharedPreferences mPreferences;
    private boolean refreshing = false;
    public static final int TYPE_ALL = 0;
    public static final int TYPE_PERSON = 1;
    public static final int TYPE_FOLLOW = 2;
    public static final int TYPE_HOT = 3;

    private final int type;
    private int userId = -1;
    private List<Moment> mMomentList;
    private RecyclerView momentsRecyclerView;
    private MomentAdapter momentAdapter;
    private ActivityResultLauncher<Intent> detailedLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        refresh();
                    }
                }
            });
    private final Handler getMomentsHandler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == GET_MOMENTS_SUCCESS) {
                try {
                    JSONArray arr = new JSONArray(msg.obj.toString());
                    mMomentList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        Moment moment = new Moment(arr.getJSONObject(i));
                        if(moment.isBlockedByMe) // 屏蔽
                            continue;
                        if(type == TYPE_FOLLOW && !moment.isFollowedByMe) // 关注
                            continue;
                        if(type == TYPE_HOT && moment.getLikeCount()+ moment.getCommentCount()+ moment.getStarCount() < 2) // 热门
                            continue;
                        mMomentList.add(moment);
                    }
                    momentAdapter.setMoments(mMomentList);
                    momentsRecyclerView.setAdapter(momentAdapter);
                    momentAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (msg.what == GET_MOMENTS_FAIL) {
                Toast.makeText(requireActivity(), "获取动态失败", Toast.LENGTH_SHORT).show();
            }
            if (getParentFragment() != null) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) momentsRecyclerView.getLayoutManager();
                assert layoutManager != null;
                layoutManager.scrollToPositionWithOffset(mPreferences.getInt("position" + getParentFragment().getClass().getName(), 0), 0);
            }
            refreshing = false;
        }
    };
    public MomentsFragment(int type, int userId) {
        this.type = type;
        this.userId = userId;
    }

    public MomentsFragment(int type) {
        this.type = type;
    }
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        if (type == TYPE_PERSON) {
            Services.getMomentsByUser(userId, getMomentsHandler);
        } else if(type == TYPE_ALL) {
            Services.getMomentsAll(getMomentsHandler);
        } else if(type == TYPE_FOLLOW) {
            Services.getMomentsAll(getMomentsHandler);
        } else if(type == TYPE_HOT) {
            Services.getMomentsAll(getMomentsHandler);
        }
        // TODO: Services.getMomentsHot/getMomentsFollow
    }
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMomentList = new ArrayList<>();
        mPreferences = mActivity.getSharedPreferences("moment_fragment", Context.MODE_PRIVATE);
        // clear
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_moments, container, false);
        
        momentsRecyclerView = view.findViewById(R.id.user_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        momentsRecyclerView.setLayoutManager(layoutManager);
        momentAdapter = new MomentAdapter(mMomentList, new MomentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int id) {
//                Toast.makeText(getContext(), "clicked: "+position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), DetailedActivity.class);
                intent.putExtra("id", id);
//                startActivity(intent);
                detailedLauncher.launch(intent);
            }

            @Override
            public void onAvatarClick(View v, int position, int id) {
                Intent intent = new Intent(v.getContext(), UserHomePageActivity.class);
                intent.putExtra("id", id);
                v.getContext().startActivity(intent);
            }

            @Override
            public void onLikeClick(View v, int position, Moment clickedMoment) {
                ImageView likeImage = v.findViewById(R.id.likeImageView_overview);
                TextView likeCountText = v.findViewById(R.id.like_textview_overview);
                Services.likeOrStar("like", clickedMoment.getId(), !clickedMoment.isLikedByMe, new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(@NonNull android.os.Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 1) {

                            clickedMoment.isLikedByMe = !clickedMoment.isLikedByMe;
                            int likeCount = clickedMoment.getLikeCount();
                            if(clickedMoment.isLikedByMe) {
                                likeCount++;
                                likeImage.setImageResource(R.drawable.ic_moment_thumbup_red);
                            } else {
                                likeCount--;
                                likeImage.setImageResource(R.drawable.ic_moment_thumbup);
                            }
                            clickedMoment.setLikeCount(likeCount);
                            likeCountText.setText(String.valueOf(likeCount));
                        } else if (msg.what == 0) {
                            Toast.makeText(requireActivity(), "点赞失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onStarClick(View v, int position, Moment clickedMoment) {
                ImageView starImage = v.findViewById(R.id.starImageView_overview);
                TextView starCountText = v.findViewById(R.id.star_textview_overview);
                Services.likeOrStar("star", clickedMoment.getId(), !clickedMoment.isStaredByMe, new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(@NonNull android.os.Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == 1) {

                            clickedMoment.isStaredByMe = !clickedMoment.isStaredByMe;
                            int starCount = clickedMoment.getStarCount();
                            if(clickedMoment.isStaredByMe) {
                                clickedMoment.setStarCount(starCount + 1);
                                starImage.setImageResource(R.drawable.ic_moment_star_yellow);
                            } else {
                                clickedMoment.setStarCount(starCount - 1);
                                starImage.setImageResource(R.drawable.ic_moment_star);
                            }
                            starCountText.setText(String.valueOf(clickedMoment.getStarCount()));
                        } else if (msg.what == 0) {
                            Toast.makeText(requireActivity(), "收藏失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        momentAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        momentsRecyclerView.setAdapter(momentAdapter);
        return view;
    }

    // 在离开页面时保存当前的滚动位置
    @Override
    public void onPause() {
        super.onPause();
        if (getParentFragment() != null) {
            SharedPreferences.Editor editor = mPreferences.edit();
            LinearLayoutManager layoutManager = (LinearLayoutManager) momentsRecyclerView.getLayoutManager();
            assert layoutManager != null;
            int position = layoutManager.findFirstVisibleItemPosition();
            Log.d("position", String.valueOf(position));
            editor.putInt("position" + getParentFragment().getClass().getName(), position);
            editor.apply();
        }
    }
}