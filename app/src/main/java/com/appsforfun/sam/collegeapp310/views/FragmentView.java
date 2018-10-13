package com.appsforfun.sam.collegeapp310.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.appsforfun.sam.collegeapp310.R;
import com.appsforfun.sam.collegeapp310.adapters.ListAdapter;
import com.appsforfun.sam.collegeapp310.models.HandleTimetableData;
import com.appsforfun.sam.collegeapp310.models.TimetableModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.ArrayList;
import java.util.List;

public class FragmentView {

    private Context context;
    private View view;
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private List<TimetableModel> result;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private ProgressBar mProgress;
    private TextView emptyText;
    private HandleTimetableData handleTimetableData;

    private int hour;
    private String subName, subCode, facName, roomNo, timeFrom, timeTo;
    private EditText sName, sCode, fName, rNo;
    private TimePicker timePicker;

    public FragmentView(Context context, View view){
        this.context = context;
        this.view = view;
    }

    public void initiateProcess(String day){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference(user.getUid()).child(day);

        TextView userEmail = view.findViewById(R.id.tv_acc_email);
        userEmail.setText(user.getEmail());

        result = new ArrayList<>();
        mProgress = view.findViewById(R.id.mProgressBar);
        emptyText = view.findViewById(R.id.tv_emptyText);

        iniRecyclerView();
        iniFABs();

        handleTimetableData = new HandleTimetableData(context, mSwipeRefreshLayout, recyclerView, emptyText, mProgress, reference, result, adapter);
        handleTimetableData.updateList();
    }

    public HandleTimetableData getHandleTimetableData() {
        return handleTimetableData;
    }

    private void iniRecyclerView(){
        recyclerView = view.findViewById(R.id.mRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        adapter = new ListAdapter(result);
        recyclerView.setAdapter(adapter);

        mSwipeRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                handleTimetableData.refresh();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void iniFABs(){
        SpeedDialView sdv = view.findViewById(R.id.mSpeedDialView);
        SpeedDialActionItem item1 = new SpeedDialActionItem.Builder(R.id.addData_fab, R.drawable.ic_edit)
                .setFabBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                .setLabel("Add")
                .setLabelClickable(true)
                .create();
        SpeedDialActionItem item2 = new SpeedDialActionItem.Builder(R.id.refresh_fab, R.drawable.ic_refresh)
                .setFabBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                .setLabel("Refresh")
                .setLabelClickable(true)
                .create();
        SpeedDialActionItem item3 = new SpeedDialActionItem.Builder(R.id.deleteAll_fab, R.drawable.ic_delete_sweep)
                .setFabBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                .setLabel("Delete all")
                .setLabelClickable(true)
                .create();


        sdv.addActionItem(item1,0);
        sdv.addActionItem(item2,1);
        sdv.addActionItem(item3,2);


        sdv.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()){
                    case R.id.addData_fab:
                        initiateDialog1();
                        return false;
                    case R.id.refresh_fab:
                        handleTimetableData.refresh();
                        return false;
                    case R.id.deleteAll_fab:
                        iniDeleteAllDialog();
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    private void initiateDialog1(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_hour);
        NumberPicker numberPicker = dialog.findViewById(R.id.hour_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                hour = picker.getValue();
            }
        });
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hour > 0){
                    boolean valid = true;
                    for (int i=0; i<result.size(); i++){
                        if (hour == result.get(i).hour){
                            Toast.makeText(context, "choose different hour." + hour+ " is already present.", Toast.LENGTH_SHORT).show();
                            valid = false;
                            break;
                        }
                    }
                    if (valid){
                        dialog.dismiss();
                        initiateDialog2();
                    }
                }
                else {
                    Toast.makeText(context, "select hour properly", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hour = 0;
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initiateDialog2(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_subject_name);
        sName = dialog.findViewById(R.id.edt_subjectName);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subName = sName.getText().toString();
                if (!TextUtils.isEmpty(subName)){
                    dialog.dismiss();
                    initiateDialog3();
                }
                else {
                    sName.setError("Enter subject name");
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initiateDialog1();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initiateDialog3(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_subject_code);
        sCode = dialog.findViewById(R.id.edt_subjectCode);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subCode = sCode.getText().toString();
                if (!TextUtils.isEmpty(subCode)){
                    dialog.dismiss();
                    initiateDialog4();
                }
                else {
                    sCode.setError("Enter subject code");
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initiateDialog2();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initiateDialog4(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_faculty_name);
        fName = dialog.findViewById(R.id.edt_facultyName);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facName = fName.getText().toString();
                if (!TextUtils.isEmpty(facName)){
                    dialog.dismiss();
                    initiateDialog5();
                }
                else {
                    fName.setError("Enter faculty name");
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initiateDialog3();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initiateDialog5(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_room_no);
        rNo = dialog.findViewById(R.id.edt_roomNo);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomNo = rNo.getText().toString();
                if (!TextUtils.isEmpty(roomNo)){
                    dialog.dismiss();
                    initiateDialog6();
                }
                else {
                    rNo.setError("Enter room no");
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initiateDialog4();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initiateDialog6(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_time_from);
        timePicker = dialog.findViewById(R.id.tp_time_from);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                timeFrom = getTime(hour,minute);
                dialog.dismiss();
                initiateDialog7();
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initiateDialog5();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initiateDialog7(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_time_to);
        timePicker = dialog.findViewById(R.id.tp_time_to);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ho = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                timeTo = getTime(ho,minute);
                String time = new StringBuilder().append(timeFrom).append(" to ").append(timeTo).toString();
                dialog.dismiss();
                handleTimetableData.addTimetableData(hour,subName,subCode,facName,roomNo,time);
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                initiateDialog6();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void iniDeleteAllDialog(){
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Delete All");
        alertDialog.setMessage("Are you sure ?");
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleTimetableData.deleteAll();
            }
        });
        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private String getTime(int hr,int min) {
        String timeSet = "";
        if (hr > 12) {
            hr -= 12;
            timeSet = "PM";
        } else if (hr == 0) {
            hr += 12;
            timeSet = "AM";
        } else if (hr == 12){
            timeSet = "PM";
        }else{
            timeSet = "AM";
        }

        String minSet = "";
        if (min < 10){
            minSet = "0" + min ;
        }
        else{
            minSet = String.valueOf(min);
        }

        String aTime = new StringBuilder().append(hr).append(':').append(minSet ).append(" ").append(timeSet).toString();
        return aTime;
    }
}
