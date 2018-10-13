package com.appsforfun.sam.collegeapp310.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appsforfun.sam.collegeapp310.adapters.ListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class HandleTimetableData {

    private Context context;
    private ProgressBar mProgress;
    private DatabaseReference reference;
    private List<TimetableModel> result;
    private ListAdapter adapter;
    private TextView emptyText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;

    private void sortData(){
        if (!result.isEmpty()){
            Collections.sort(result, new SortByHour());
        }
    }

    private int getItemIndex(TimetableModel user){
        int index = -1;
        for (int i=0; i<result.size(); i++){
            if (result.get(i).key.equals(user.key)){
                index = i;
                break;
            }
        }
        return index;
    }

    private void checkIfEmpty(){
        if (result.isEmpty() && mProgress.getVisibility() == View.VISIBLE){
            recyclerView.setVisibility(View.INVISIBLE);
            emptyText.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.INVISIBLE);
        }
    }

    public HandleTimetableData(Context context, SwipeRefreshLayout mSwipeRefreshLayout, RecyclerView recyclerView, TextView emptyText, ProgressBar mProgress, DatabaseReference reference, List<TimetableModel> result, ListAdapter adapter){
        this.context = context;
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.recyclerView = recyclerView;
        this.emptyText = emptyText;
        this.mProgress = mProgress;
        this.reference = reference;
        this.result = result;
        this.adapter = adapter;
    }

    public void addTimetableData(int hour, String subName, String subCode, String facName, String roomNo, String time){
        mProgress.setVisibility(View.VISIBLE);
        String key = reference.push().getKey();
        TimetableModel timetableModel = new TimetableModel(hour, subName, subCode, facName, roomNo, key, time);
        reference.child(key).setValue(timetableModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgress.setVisibility(View.GONE);
                    refresh();
                }
                else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    public void deleteTimetableData(int position){
        mProgress.setVisibility(View.VISIBLE);
        reference.child(result.get(position).key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mProgress.setVisibility(View.GONE);
                }
                else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    public void updateList(){
        mProgress.setVisibility(View.VISIBLE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mProgress.setVisibility(View.GONE);
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                result.add(dataSnapshot.getValue(TimetableModel.class));
                sortData();
                adapter.notifyDataSetChanged();
                checkIfEmpty();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                TimetableModel model = dataSnapshot.getValue(TimetableModel.class);
                int index = getItemIndex(model);
                result.set(index, model);
                adapter.notifyItemChanged(index);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                TimetableModel model = dataSnapshot.getValue(TimetableModel.class);
                int index = getItemIndex(model);
                if (index > -1){
                    result.remove(index);
                    sortData();
                    adapter.notifyItemRemoved(index);
                }
                checkIfEmpty();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refresh(){
        result.clear();
        if (result.isEmpty()){
            updateList();
        }
        if (mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void deleteAll(){
        mProgress.setVisibility(View.VISIBLE);
        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    result.clear();
                    mProgress.setVisibility(View.GONE);
                    checkIfEmpty();
                }
                else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    mProgress.setVisibility(View.GONE);
                }
            }
        });
    }

    class SortByHour implements Comparator<TimetableModel>
    {
        public int compare(TimetableModel a, TimetableModel b)
        {
            return a.hour - b.hour;
        }
    }

}


