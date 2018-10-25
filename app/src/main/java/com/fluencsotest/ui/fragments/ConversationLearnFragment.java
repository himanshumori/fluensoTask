package com.fluencsotest.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fluencsotest.R;
import com.fluencsotest.Util;
import com.fluencsotest.models.ConversationItem;
import com.fluencsotest.models.PersonaConvITem;
import com.fluencsotest.models.UserConvItem;
import com.fluencsotest.speechhandler.PlayerUtility;
import com.fluencsotest.speechhandler.RecorderUtility;
import com.fluencsotest.ui.adapters.ConversationLearnAdapter;

import java.util.ArrayList;

public class ConversationLearnFragment extends Fragment {

    // User states
    private int U_IDLE = 1;
    private int U_SPEAKING = 2;
    private int U_DATA_ANALYSIS = 3;
    private int U_SPOKEN_TEXT = 4;

    private int recordingState = U_IDLE; // default
    private String recordedPath; // default

    private ArrayList<ConversationItem> conversationItems = new ArrayList<>();
    private ConversationLearnAdapter conversationLearnAdapter;
    private RecyclerView recyclerView;
    private ImageButton mic_ib, stop_ib;
    private TextView mic_status_text;
    private LinearLayout mic_rl;
    private LinearLayout speak_progress_rl;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_conversation_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
    }

    private void initView(View view) {

        speak_progress_rl = view.findViewById(R.id.speak_progress_rl);
        mic_rl = view.findViewById(R.id.mic_rl);
        mic_status_text = view.findViewById(R.id.mic_status_text);
        mic_ib = view.findViewById(R.id.mic_ib);
        stop_ib = view.findViewById(R.id.stop_ib);

        recyclerView = view.findViewById(R.id.conversation_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        conversationLearnAdapter = new ConversationLearnAdapter(conversationItems);
        recyclerView.setAdapter(conversationLearnAdapter);

        view.findViewById(R.id.dummy_persona_button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                addPersonaDummyItem();
            }
        });

        mic_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                recordingState = U_SPEAKING;
                displayMicStatus();
                recordedPath = Util.Companion.generateRandomWAVFileName();
                RecorderUtility.Companion.getInstance().startRecording(recordedPath);
            }
        });

        stop_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlayerUtility.Companion.getInstance().stopPlaying();
                recordingState = U_DATA_ANALYSIS;
                displayMicStatus();
                analyseVoiceData();
            }
        });

        displayMicStatus();
    }

    private void displayMicStatus() {

        if (recordingState == U_IDLE) {

            mic_status_text.setText(R.string.tap_to_speak);
            mic_rl.setVisibility(View.VISIBLE);
            speak_progress_rl.setVisibility(View.GONE);

        } else if (recordingState == U_SPEAKING) {

            mic_rl.setVisibility(View.GONE);
            speak_progress_rl.setVisibility(View.VISIBLE);

        } else if (recordingState == U_DATA_ANALYSIS) {

            mic_status_text.setText(R.string.analysing);
            mic_rl.setVisibility(View.VISIBLE);
            speak_progress_rl.setVisibility(View.GONE);

        } else if (recordingState == U_SPOKEN_TEXT) {

            // add Dummy Item with recorded data
            addUserDummyItem();
        }
    }

    private void analyseVoiceData() {

        // analyse anything dummy for few seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                recordingState = U_SPOKEN_TEXT;
                displayMicStatus();
            }
        }, 4000);
    }

    // DUMMY BLOCKs
    private void addPersonaDummyItem() {

        ConversationItem conversationItem = new ConversationItem();
        PersonaConvITem personaConvITem = new PersonaConvITem();
        personaConvITem.setVoiceFilePath("/storage/emulated/0/fluenso/Final_intro_session.wav");
        personaConvITem.setVoiceText("Please speak OnePlus");

        conversationItems.add(conversationItem);
        conversationLearnAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(conversationLearnAdapter.getItemCount());
    }

    private void addUserDummyItem() {

        ConversationItem conversationItem = new ConversationItem();
        UserConvItem personaConvITem = new UserConvItem();
        personaConvITem.setVoiceFilePath(recordedPath);
        personaConvITem.setVoiceText("OnePlus");
        personaConvITem.setRating(3);
        personaConvITem.setFeedBackText("good job");

        conversationItems.add(conversationItem);
        conversationLearnAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(conversationLearnAdapter.getItemCount());

        recordingState = U_IDLE;
        displayMicStatus();
    }

}
