package com.rentlar.taskmo.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.rentlar.taskmo.MainActivity;
import com.rentlar.taskmo.R;
import com.rentlar.taskmo.databinding.FragmentHomeBinding;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private String[] sampleArray = {"Task 1", "Task 2", "Task 3", "Celebrate!"};
    private ArrayList<CheckBox> taskList = new ArrayList<>();
    private int lastPlacementSelection = -1;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadList();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext());
        binding.textView2.setTextSize(sharedPreferences.getInt("list_size",26));

        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMeThePopup(v,-1);
            }
        });
        //Hold to remove button
        binding.floatingActionButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                binding.floatingActionButton.setVisibility(View.GONE);
                binding.floatingActionButton.setEnabled(false);
                return true;
            }
        });


        return root;
    }


    //gets String[] list of all tasks.
    public String[] getStringList(){
        if (taskList.isEmpty())
            return new String[0];

        String[] r = new String[taskList.size()];

        for (int a = 0; a < taskList.size(); a++){
            r[a] = taskList.get(a).getText().toString();
        }
        return r;
    }
    public String[] getStringList(boolean addExtra){
        if (taskList.isEmpty()){
            if(addExtra){
                return new String[1];
            }
            return new String[0];
        }

        String[] r;
        if(addExtra){
            r = new String[taskList.size()+1];
        }
        else {
            r = new String[taskList.size()];
        }

        for (int a = 0; a < taskList.size(); a++){
            r[a] = taskList.get(a).getText().toString();
        }

        Bundle test;
        return r;
    }
    public boolean[] getBooleans(){
        if (taskList.isEmpty())
            return new boolean[0];

        boolean[] r = new boolean[taskList.size()];

        for (int a = 0; a < taskList.size(); a++){
            r[a] = taskList.get(a).isChecked();
        }
        return r;
    }

    public void showMeThePopup(View view, int popup_value) {

        // inflate the layout of the popup window
        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.entry_popup, null);
        //hide the plus
        binding.floatingActionButton.setVisibility(View.INVISIBLE);

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
                binding.floatingActionButton.setVisibility(View.VISIBLE);
            }
        });

        Button[] setButtons = {popupView.findViewById(R.id.button_1),popupView.findViewById(R.id.button_2),popupView.findViewById(R.id.button_3)};
        String[] phrases;

        EditText ed = popupView.findViewById(R.id.entry);

        TextView popupPrompt = popupView.findViewById(R.id.prompt);
        int buttonLimit = 1;
        //change popup based on selection type.
        if (popup_value == -1){ // add
            buttonLimit = 2;
          phrases = getResources().getStringArray(R.array.add_actions);
            //set drop down list now.
            String[] items = getStringList(true);
            items[items.length-1] = getString(R.string.add_to_end);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_spinner_dropdown_item, items);

            Spinner dropdown = popupView.findViewById(R.id.spinner);

            popupPrompt.setText(getString(R.string.new_task));
            dropdown.setAdapter(adapter);
            if(lastPlacementSelection < 0 || lastPlacementSelection >= items.length){
                dropdown.setSelection(items.length-1);
            }
            else{
                dropdown.setSelection(lastPlacementSelection);
            }
            dropdown.setVisibility(Spinner.VISIBLE);
            setButtons[2].setVisibility(Button.GONE);
        }
        else{ // edit
            buttonLimit = 3;
            phrases = getResources().getStringArray(R.array.edit_actions);
            Spinner dropdown = popupView.findViewById(R.id.spinner);
            dropdown.setVisibility(Spinner.GONE);
            popupView.findViewById(R.id.prompt2).setVisibility(View.GONE);

            popupPrompt.setText(getString(R.string.edit_task));
            ed.setText(taskList.get(popup_value).getText());
            setButtons[2].setVisibility(Button.VISIBLE);

        }

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
            setButtons[iteration].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Spinner dropdown = popupView.findViewById(R.id.spinner);
                    lastPlacementSelection = dropdown.getSelectedItemPosition();
                    //new variable introduced so that lastPlacement can be saved to end.
                    int placement_index = lastPlacementSelection;
                    if(lastPlacementSelection == taskList.size()){
                        lastPlacementSelection = -1;
                    }
                    //first button clicked (Add or Rename)
                    if(iteration == 0){
                        EditText ed = popupView.findViewById(R.id.entry);

                        //rename clicked
                        if (popup_value > -1 && popup_value < taskList.size()){
                            if(taskList.get(popup_value).getText().length() > 0){
                                taskList.get(popup_value).setText(ed.getEditableText().toString());
                            }
                            saveList();
                        }
                        //add clicked
                        else if (popup_value == -1){
                            CheckBox nc = new CheckBox(getContext());
                            if(ed.getEditableText().toString().length() == 0){
                                int taskset = 1;
                                //used first unused task # for unnamed.
                                String[] templist = getStringList();
                                for(int a = 0; a < templist.length; a++){
                                    if (templist[a].equals(getString(R.string.unnamed_task,taskset))){
                                        taskset++;
                                        a = 0;
                                    }
                                }
                                nc.setText(getString(R.string.unnamed_task,taskset));

                            }
                            else{
                                nc.setText(ed.getEditableText().toString());
                            }

                            taskList.add(placement_index,nc);
                            //adjust last placement according to list.
                            if(lastPlacementSelection != -1){
                                lastPlacementSelection ++;
                            }
                            updateButtons(placement_index,true);
                        }


                    }
                    //if delete pressed on the edit screen
                    else if (iteration == 1 && popup_value != -1){
                        taskList.remove(popup_value);
                        updateButtons(popup_value,true);
                    }

                    popupWindow.dismiss();
                }
            });
        }
    }

    public void updateButtons(int startIndex, boolean saveAtEnd){

        LinearLayout holder = binding.mainLayout;
        while(holder.getChildCount() > startIndex + 1){
            holder.removeViewAt(startIndex+1);
        }
        for (int a = startIndex; a < taskList.size(); a++){
            formatCheckBox(taskList.get(a),a,false);
            holder.addView(taskList.get(a));
            }


        if(saveAtEnd){
            boolean result = saveList();
            if(!result){
                Toast.makeText(getContext(), getString(R.string.failure_save), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public CheckBox formatCheckBox(CheckBox input, int index, boolean remake){

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext());
        CheckBox nc;

        if(!remake || input != null){
                nc = input;
            }
            else {
                nc = new CheckBox(getContext());
            }
        nc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveList();
            }
        });
        nc.setTextSize(sharedPreferences.getInt("task_size",26));
        if(sharedPreferences.getString("task_check_direction","right").equals("right")){
            nc.setLayoutDirection(CheckBox.LAYOUT_DIRECTION_RTL);
        }
        else{
            nc.setLayoutDirection(CheckBox.LAYOUT_DIRECTION_LTR);
        }

        nc.setPadding(30, 10, 30, 10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(40,10,40,10);
        nc.setLayoutParams(params);
        nc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showMeThePopup(v, index);
                return true;
            }
        });
        return nc;
    }

    public boolean loadList(){
        String filename = MainActivity.currentList;
        taskList.clear();

        BufferedInputStream reader;
        binding.textView2.setText(filename);
        ArrayList<Byte> reading = new ArrayList<>();

        try{
            reader = new BufferedInputStream(getContext().openFileInput(filename+".list"));
            //alright plan is to read 1 byte at a time until end into an arraylist byte
            byte[] readbuf = {0};

                while(reader.read(readbuf) != -1){
                    reading.add(readbuf[0]);
                }

            int count = (reading.get(0) & 0xFF)*256 + (reading.get(1) & 0xFF);

                if(count == 0){
                    Toast.makeText(getContext(), getString(R.string.empty_list_prompt), Toast.LENGTH_SHORT).show();
                    return false;
                }
            int checkCount = (reading.get(2) & 0xFF)*256 + (reading.get(3) & 0xFF);
            ArrayList<Integer> checkedBoxes = new ArrayList<>();
            //read all checks into the checkBoxes
            for(int a = 0; a < checkCount; a ++){
                checkedBoxes.add((reading.get(a*2+4) & 0xFF)*256 + (reading.get(a*2+5) & 0xFF));
            }

            byte[] readingBytes = new byte[reading.size()-(checkCount*2+4)];
            int iterator = 0;
            for(int a = checkCount*2+4; a < reading.size(); a++){
                readingBytes[iterator] = reading.get(a);
                iterator++;
            }
            reading.clear();
            String fullString = new String(readingBytes, StandardCharsets.UTF_8);

            //now get all the strings and add them to checkboxes.
            int currentindex = 0, boxnumber = 0;
            for(int a = 0; a < fullString.length(); a++){
                if(fullString.charAt(a) =='\0'){

                    if(!fullString.substring(currentindex,a-1).isEmpty()){
                        CheckBox nc = new CheckBox(getContext());
                        nc.setText(fullString.substring(currentindex,a));
                        taskList.add(nc);
                        binding.mainLayout.addView(nc);
                        formatCheckBox(nc,boxnumber,false);
                        boxnumber ++;
                    }


                    a++;
                    currentindex = a;
                }
            }
            //now check all boxes
            for(int a = 0; a < checkedBoxes.size(); a++){
                taskList.get(checkedBoxes.get(a)).setChecked(true);
            }


            LinearLayout holder = binding.mainLayout;
            if(MainActivity.currentList.equals(getString(R.string.default_list))){
                binding.textView2.setVisibility(View.GONE);
            }
            else{
                binding.textView2.setVisibility(View.VISIBLE);
                binding.textView2.setText(MainActivity.currentList);
            }

        }
        catch(Exception e){
            if(e.getClass()== FileNotFoundException.class){
                Toast.makeText(getContext(), getString(R.string.empty_list_prompt), Toast.LENGTH_SHORT).show();
            }
            else{
                e.printStackTrace();
                Toast.makeText(getContext(), getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        return true;
    }

    public ArrayList<Integer> countCheckedItems(){
        ArrayList<Integer>r = new ArrayList<>();
        for(int a = 0 ; a< taskList.size(); a++){
            if(taskList.get(a).isChecked()){
                r.add(a);
            }
        }
        return r;
    }

    public boolean saveList(){
        String filename = MainActivity.currentList;
        String[] finalList = getStringList();
        //boolean[] finalBoolean = getBooleans();
        FileOutputStream writer;

        //saving plan will look like this: first a two byte integer representing the list size, then add number of checked items. then series of two byte integers representing the checked items until FF. FF
        //lastly the text will encoded and separated by null characters.
        try{
            //open and replace file related to that list. (use extension .list)
            writer = getContext().openFileOutput(filename+".list", Context.MODE_PRIVATE);

            //add number of items
            byte[] saveListSize = {(byte)(finalList.length/256),(byte)(finalList.length%256)};
            writer.write(saveListSize);

            //add number of checks
            ArrayList<Integer> checks = countCheckedItems();
            byte[] checksSize = {(byte)(checks.size()/256),(byte)(checks.size()%256)};
            writer.write(checksSize);
            //now add each location of check
            for(int a = 0; a < checks.size(); a++){
                saveListSize[0] = (byte)(checks.get(a)/256);
                saveListSize[1] = (byte)(checks.get(a)%256);
                writer.write(saveListSize);
            }

            for (int a = 0; a < finalList.length; a++){
                writer.write(finalList[a].getBytes(StandardCharsets.UTF_8));
                writer.write('\0');
            }

            writer.close();
        }
        catch(Exception e){
            Toast.makeText(getContext(), getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private int convertToInt(String s){
        try {return Integer.parseInt(s);} catch (NumberFormatException e) {return 0;}
    }
    private int convertToInt(String s, int defaultVal){
        try {return Integer.parseInt(s);} catch (NumberFormatException e) {return defaultVal;}
    }

}
