package com.rentlar.taskmo.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.rentlar.taskmo.MainActivity;
import com.rentlar.taskmo.R;
import com.rentlar.taskmo.databinding.FragmentDashboardBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private String[] files;
    private ArrayList<RadioButton> listList;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext());
        binding.textView.setTextSize(sharedPreferences.getInt("list_size",26));

        refreshList();

        binding.floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMeThePopup(v,-1);
            }
        });
        //Hold to remove button
        binding.floatingActionButton2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                binding.floatingActionButton2.setVisibility(View.GONE);
                binding.floatingActionButton2.setEnabled(false);
                return true;
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void refreshList(){
        listList = new ArrayList<>();
        try{
            files = getContext().fileList();
        }
        catch(NullPointerException e){
            Toast.makeText(getContext(), getString(R.string.error_no_list), Toast.LENGTH_SHORT).show();
        }
        binding.listSelect.removeAllViews();

        int skipoffset = 0; // a+skipoffset will be the actual string value from file, a will be the index of the list.
        for (int a = 0; a+skipoffset < files.length; a++){
            int index = a;
            RadioButton nrb = new RadioButton(getContext());
            int fileSize = 0;

            //file must have ".list" extension at end. Remove this extension when showing on the checkboxes
            if (files[a+skipoffset].length()<5||!files[a+skipoffset].substring(files[a+skipoffset].length()-5).equals(".list")){
                a--;
                skipoffset++;
                continue;
            }
            nrb.setText(files[a+skipoffset].substring(0,files[a+skipoffset].length()-5));
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(requireContext());
            nrb.setTextSize(sharedPreferences.getInt("list_size",26));
            nrb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        MainActivity.currentList = nrb.getText().toString();
                        updateBottomBar();
                    }
                }
            });
            nrb.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showMeThePopup(v, index);//add index in list as the parameter value
                    return true;
                }
            });
            //add to view and to listList
            binding.listSelect.addView(nrb);
            listList.add(nrb);
            //filenamematches
            if(files[a+skipoffset].substring(0,files[a+skipoffset].length()-5).equals(MainActivity.currentList)){
                RadioGroup nlistSelect = binding.listSelect;
                nlistSelect.check(nrb.getId());
            }
        }
    }

    public void listCopy(String src, String dst){
        try{
            BufferedInputStream reader = new BufferedInputStream(getContext().openFileInput(src+".list"));
            FileOutputStream writer = getContext().openFileOutput(dst+".list", Context.MODE_PRIVATE);

            //add number of items

            byte[] readbuf = {0};

            while(reader.read(readbuf)!= -1){
                writer.write(readbuf);
            }
        }
        catch (Exception e){
            Toast.makeText(getContext(), getString(R.string.error_copy), Toast.LENGTH_SHORT).show();
        }

    }

    public void updateBottomBar(){
        try {
            BufferedInputStream reader = new BufferedInputStream(getContext().openFileInput(MainActivity.currentList+".list"));
            //alright plan is to read 1 byte at a time until end into an arraylist byte
            byte[] readbuf = {0,0,0,0};

            //read, check for fail.
            if(reader.read(readbuf,0,4) == -1){
                binding.textView4.setText(getString(R.string.prompt_list));
                return;
            }

            int count = (readbuf[0] & 0xFF) * 256 + (readbuf[1] & 0xFF);
            int checkCount = (readbuf[2] & 0xFF) * 256 + (readbuf[3] & 0xFF);
            int percentage;
            if(checkCount == 0){
                percentage = 0;
            }
            else{
                percentage = (int)(Math.floor(checkCount*100.0/count));
            }

            binding.textView4.setText(getString(R.string.number_completed,checkCount,count,percentage));
        }
        catch(Exception e){
            binding.textView4.setText(getString(R.string.prompt_list));
            Toast.makeText(getContext(), "DEBUG", Toast.LENGTH_SHORT).show();
        }
    }

    public void listRemove(String filename){

        try{
            getContext().deleteFile(filename+".list");
        }
        catch (Exception e){
            Toast.makeText(getContext(), getString(R.string.error_no_delete), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean listNew(String filename){
        try{
            FileOutputStream writer = getContext().openFileOutput(filename+".list", Context.MODE_PRIVATE);

            //add number of items

            byte[] readbuf = {0,0,0,0};
            writer.write(readbuf);
        }
        catch (Exception e){
            Toast.makeText(getContext(), getString(R.string.error_create), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }

    public void showMeThePopup(View view, int popup_value) {


        // inflate the layout of the popup window
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.entry_popup, null);

        //hide the plus
        binding.floatingActionButton2.setVisibility(View.INVISIBLE);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                binding.floatingActionButton2.setVisibility(View.VISIBLE);
            }
        });

        Button[] setButtons = {popupView.findViewById(R.id.button_1),popupView.findViewById(R.id.button_2),popupView.findViewById(R.id.button_3)};
        String[] phrases;

        EditText ed = popupView.findViewById(R.id.entry);

        TextView popupPrompt = popupView.findViewById(R.id.prompt);
        int buttonLimit = 1;
        String oldName = ""; //used only for edit
        //change popup based on selection type.
        if (popup_value == -1){ // add
            buttonLimit = 2;
            phrases = getResources().getStringArray(R.array.add_actions);
           
            Spinner dropdown = popupView.findViewById(R.id.spinner);
            dropdown.setVisibility(View.GONE);
            setButtons[2].setVisibility(Button.GONE);
            popupPrompt.setText(getString(R.string.new_list));
        }
        else{ // edit
            buttonLimit = 3;
            phrases = getResources().getStringArray(R.array.edit_actions);
            Spinner dropdown = popupView.findViewById(R.id.spinner);
            dropdown.setVisibility(Spinner.GONE);

            RadioButton sel = (RadioButton)binding.listSelect.getChildAt(popup_value);
            oldName = sel.getText().toString();

            popupPrompt.setText(getString(R.string.edit_list));
            ed.setText(oldName);
            setButtons[2].setVisibility(Button.VISIBLE);




        }

        popupView.findViewById(R.id.prompt2).setVisibility(View.GONE);
        //get edittext focus
        ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ed.requestFocus();
                ed.selectAll();
                ed.setOnClickListener(null);

            }
        });
        //set button actions now
        for (int a = 0; a < buttonLimit; a++){
            int iteration = a;
            setButtons[iteration].setText(phrases[iteration]);
            String finalOldName = oldName;
            setButtons[iteration].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //new variable introduced so that lastPlacement can be saved to end.

                    //first button clicked (Add or Rename)
                    if(iteration == 0){
                        EditText ed = popupView.findViewById(R.id.entry);

                        //rename clicked
                        if (popup_value > -1 && popup_value < listList.size()){
                            if(!ed.getEditableText().toString().isEmpty() && !ed.getEditableText().toString().equals(finalOldName)){

                                boolean existsAlready = false;
                                for(int a = 0 ; a < listList.size(); a++){
                                    //matches existing record
                                    if(a != popup_value && ed.getEditableText().toString().contentEquals(listList.get(a).getText())){
                                        Toast.makeText(getContext(), getString(R.string.error_file_exist), Toast.LENGTH_SHORT).show();
                                        existsAlready = true;
                                    }
                                }
                                //copy old list to new name, delete old list, as long as old list didn't also exist somewhere else.
                                if(!existsAlready){
                                    listCopy(finalOldName,ed.getEditableText().toString());
                                    listRemove(finalOldName);
                                    //update currentlist if the renamed one is the selected one
                                    if(finalOldName.equals(MainActivity.currentList)){
                                        MainActivity.currentList = ed.getEditableText().toString();
                                    }
                                }
                            }
                            refreshList();
                        }
                        //add clicked
                        else if (popup_value == -1){
                            RadioButton nrb = new RadioButton(getContext());
                            if(ed.getEditableText().toString().length() == 0){

                                int listset = 1;
                                //used first unused list # for unnamed.
                                String[] templist = requireContext().fileList();
                                for(int a = 0; a < templist.length; a++){
                                    if (templist[a].equals(getString(R.string.unnamed_list,listset)+".list")){
                                        listset++;
                                        a = 0;
                                    }
                                }
                                nrb.setText(getString(R.string.unnamed_list,listset));
                                ed.setText(getString(R.string.unnamed_list,listset));
                            }
                            else{
                                nrb.setText(ed.getEditableText().toString());
                            }
                            //check for existing list:
                            boolean existsAlready = false;
                            for(int a = 0 ; a < listList.size(); a++){
                                //matches existing record
                                if(ed.getEditableText().toString().contentEquals(finalOldName)){
                                    Toast.makeText(getContext(), getString(R.string.error_file_exist), Toast.LENGTH_SHORT).show();
                                    existsAlready = true;
                                }
                            }
                            //create new list if it doesn't already exist
                            if(!existsAlready){
                                listNew(ed.getEditableText().toString());
                                listList.add(nrb);
                                //update
                                SharedPreferences sharedPreferences =
                                        PreferenceManager.getDefaultSharedPreferences(requireContext());
                                nrb.setTextSize(sharedPreferences.getInt("list_size",26));
                                nrb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if(isChecked){
                                            MainActivity.currentList = nrb.getText().toString();
                                        }
                                    }
                                });
                                nrb.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        showMeThePopup(v, listList.size()-1);
                                        return true;
                                    }
                                });
                                nrb.setPadding(30, 10, 30, 10);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.setMargins(40,10,40,10);
                                nrb.setLayoutParams(params);
                                binding.listSelect.addView(nrb);
                            }


                        }


                    }
                    //if delete pressed on the edit screen
                    else if (iteration == 1 && popup_value != -1){
                        listRemove(finalOldName);
                        refreshList();
                    }

                    popupWindow.dismiss();
                }
            });
        }
    }
    
}