package com.walkap.x_android.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.walkap.x_android.R;
import com.walkap.x_android.model.Scheduler;
import com.walkap.x_android.model.SchoolSubject;
import com.walkap.x_android.model.TimeSchoolSubject;

public class addScheduleActivity extends AppCompatActivity {

    private final String TAG = "addSchedulerActivity";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;

    static private int numColumn = 6;
    static private int numRow = 20;

    static private int startHour = 8;

    private String classroomName;
    private String schoolSubjectName;

    private String userUniversityKey;
    private String userFacultyKey;
    private String userDegreeCourseKey;

    private final String UNIVERSITY = "university";
    private final String FACULTY = "faculty";
    private final String DEGREE_COURSE = "degreeCourse";

    private final String SCHEDULER = "scheduler";
    private final String SCHOOOL_SUBJECT = "schoolSubject";
    private final String SCHOOL_SUBJECT_ID = "schoolSubjectId";

    private final String CLASSROOM = "classroom";


    private Boolean waitForSecondTap = false;
    private int beginning = 0;

    private String schoolSubjectKey = "";

    private int[] positionGridView = new int[] {1, 0, 0, 0, 0, 0};

    private int[][] positionListView = new int[][] {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    private GridView gridView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        gridView = (GridView) this.findViewById(R.id.schedulerGridView);
        String[] schedulerGrid = new String[]{
                "L",   "M",  "M",    "G",  "V",  "S"
        };

        ListAdapter adapterGrid = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, schedulerGrid);
        gridView.setAdapter(adapterGrid);
        gridView.setOnItemClickListener(GridClickListener);

        listView = (ListView) this.findViewById(R.id.schedulerListView);
        String[] schedulerList = new String[]{
                "8:00",  "8:15",  "8:30",  "8:45",  "9:00",  "9:15",  "9:30",  "9:45",
                "10:00", "10:15", "10:30", "10:45", "11:00", "11:15", "11:30", "11:45",
                "12:00", "12:15", "12:30", "12:45"};

        ListAdapter adapterList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, schedulerList);
        listView.setAdapter(adapterList);
        listView.setOnItemClickListener(ListClickListener);

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle != null)
        {
            classroomName =(String) bundle.get(CLASSROOM);
            schoolSubjectName = (String) bundle.get(SCHOOOL_SUBJECT);
        }

        readDataFileDb();

        findSchoolSubject(schoolSubjectName);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        for(int i = 0; i < gridView.getNumColumns(); i++) {
            if(positionGridView[i] == 0) {
                gridView.getChildAt(i).setBackgroundColor(Color.WHITE);
            }
            else {
                gridView.getChildAt(i).setBackgroundColor(Color.CYAN);
            }
        };
    }

    private void writeNewScheduler(String classroom, String schoolSubjectName, TimeSchoolSubject time) {

        Scheduler scheduler = new Scheduler(classroom, schoolSubjectName, time);
        SchoolSubject schoolSubject = new SchoolSubject(schoolSubjectName);

        String schedulerKey = mDatabase.child(SCHEDULER).push().getKey();

        mDatabase.child(SCHEDULER).child(schedulerKey).setValue(scheduler);

        if(schoolSubjectKey.isEmpty()) {
            String newSchoolSubjectKey = mDatabase.child(SCHOOOL_SUBJECT).push().getKey();

            mDatabase.child(SCHOOOL_SUBJECT).child(newSchoolSubjectKey).setValue(schoolSubject);
            mDatabase.child(SCHOOOL_SUBJECT).child(newSchoolSubjectKey).child(UNIVERSITY).child(userUniversityKey).child(userFacultyKey).child(userDegreeCourseKey).setValue(true);

            mDatabase.child(SCHEDULER).child(schedulerKey).child(SCHOOL_SUBJECT_ID).setValue(newSchoolSubjectKey);
            mDatabase.child(SCHEDULER).child(schedulerKey).child(SCHOOOL_SUBJECT).setValue(schoolSubjectName);

            schoolSubjectKey = newSchoolSubjectKey;

        }else{

            mDatabase.child(SCHOOOL_SUBJECT).child(schoolSubjectKey).child(UNIVERSITY).child(userUniversityKey).child(userFacultyKey).child(userDegreeCourseKey).setValue(true);
            mDatabase.child(SCHEDULER).child(schedulerKey).child(SCHOOL_SUBJECT_ID).setValue(schoolSubjectKey);
            mDatabase.child(SCHEDULER).child(schedulerKey).child(SCHOOOL_SUBJECT).setValue(schoolSubjectName);
        }

        mDatabase.child(SCHEDULER).child(schedulerKey).child(UNIVERSITY).setValue(userUniversityKey);
        mDatabase.child(SCHEDULER).child(schedulerKey).child(FACULTY).setValue(userFacultyKey);
        mDatabase.child(SCHEDULER).child(schedulerKey).child(DEGREE_COURSE).setValue(userDegreeCourseKey);
    }

    private int getGridViewSelected() {
        int selected = 0;

        for(int i = 0; i < gridView.getNumColumns(); i++) {
            if(positionGridView[i] == 1)
                selected = i;
        }

        return selected;
    }

    public int[] setAllLessOne(int one) {
        int[] positionGridView = new int[]{0, 0, 0, 0, 0, 0};
        positionGridView[one] = 1;
        return  positionGridView;
    }

    private void colorGridView() {
        for(int i = 0; i < gridView.getNumColumns(); i++) {
            if(positionGridView[i] == 0) {
                gridView.getChildAt(i).setBackgroundColor(Color.WHITE);
            }
            else {
                gridView.getChildAt(i).setBackgroundColor(Color.CYAN);
            }
        }
    }

    public void repaintListView(int day) {
        for(int i = 0; i < numRow; i++) {
            if(positionListView[day][i] == 0) {
                getViewByPosition(i, listView).setBackgroundColor(Color.WHITE);
            }
            else {
                getViewByPosition(i, listView).setBackgroundColor(Color.MAGENTA);
            }
        }
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void saveScheduler(View view) {
        int i,j,count;

        count = 0;

        for(i = 0; i < numColumn; i++){
            for(j = 0; j < numRow; j++) {
                if (positionListView[i][j] == 1){
                    count ++;
                }

                if(positionListView[i][j] == 0 && count !=0){
                    TimeSchoolSubject time = new TimeSchoolSubject(i, startHour + (j - count) / 4 , ((j - count) % 4) * 15, (count - 1) * 15);
                    writeNewScheduler(classroomName, schoolSubjectName, time);
                    count = 0;
                }
            }

        }

        Intent myIntent = new Intent(addScheduleActivity.this, MainActivity.class);
        addScheduleActivity.this.startActivity(myIntent);

    }

    AdapterView.OnItemClickListener GridClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view,
                                int position, long id) {

            if(positionGridView[position] == 0) {
                positionGridView = setAllLessOne(position);
                repaintListView(position);
            }

            waitForSecondTap = false;
            colorGridView();

        }

    };

    AdapterView.OnItemClickListener ListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view,
                                int position, long id) {

            int selected = getGridViewSelected();

            if(!waitForSecondTap) {
                if (positionListView[selected][position] == 0) {
                    positionListView[selected][position] = 1;
                    view.setBackgroundColor(Color.MAGENTA);
                    waitForSecondTap = true;
                    beginning = position;
                } else {
                    positionListView[selected][position] = 0;
                    view.setBackgroundColor(Color.WHITE);
                }
            }
            else {
                if (positionListView[selected][position] == 0) {
                    setPosition(beginning, position, selected);
                } else {
                    positionListView[selected][position] = 0;
                    view.setBackgroundColor(Color.WHITE);
                }
                waitForSecondTap = false;
            }
            Log.d("*** second tap ***", "" + waitForSecondTap );
        }

    };

    private void setPosition(int beginning, int end, int day){
        for(int i = beginning; i <= end; i++){
            positionListView[day][i] = 1;
            listView.getChildAt(i).setBackgroundColor(Color.MAGENTA);
        }
    }

    private void readDataFileDb(){

        mDatabase.child("users").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userUniversityKey = dataSnapshot.child(UNIVERSITY).getValue().toString();
                userFacultyKey = dataSnapshot.child(FACULTY).getValue().toString();
                userDegreeCourseKey = dataSnapshot.child(DEGREE_COURSE).getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void findSchoolSubject(final String schoolSubjectString){

        mDatabase.child(SCHOOOL_SUBJECT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    SchoolSubject schoolSubject = noteDataSnapshot.getValue(SchoolSubject.class);
                    if (schoolSubject.getName().equals(schoolSubjectString)) {
                        schoolSubjectKey = noteDataSnapshot.getKey();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
