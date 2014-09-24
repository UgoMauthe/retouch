package com.mauthe.retouch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.mauthe.retouch.effects.*;
import android.support.v4.app.ActionBarDrawerToggle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class IREditActivity extends Activity {


    public String openedFile;


    //Menus & Views
    Menu menuBar;
    ImageView background;
    LinearLayout cardContainer;
    LinearLayout singleEffect;
    LinearLayout editEffectContainer;
    View scrollEffectContainer;
    LinearLayout confirmChangesContainer;
    Button btnConfirmChanges;
    Button btnCancelChanges;
    ProgressBar pbProgress;
    LinearLayout masterEffectContainer;
    SeekBar effectSlider;


    //Global vars
    Bitmap fLastEditBitmap;
    Bitmap fSubmitBitmap;
    int fLastIdxEffect;
    float fLastAmountEffect;
    Context fContext;
    List<HistoryHelper> fUndoEffect;
    List<HistoryHelper> fRedoEffect;
    public boolean fEditMode;
    public List<BaseEffect> fEffectList;




    public void buildEffectList() {

        fEffectList = new ArrayList<BaseEffect>();
        fEffectList.add(new FxBrightness().setupEffect(getString(R.string.brightness), R.drawable.brightness, new EffectProperty(255.0f, -255.0f, 0.0f)));
        fEffectList.add(new FxContrast().setupEffect(getString(R.string.contrast),R.drawable.contrast, new EffectProperty(255.0f, -255.0f, 0.0f)));
        fEffectList.add(new FxSaturation().setupEffect(getString(R.string.saturation),R.drawable.saturation, new EffectProperty(200.0f, 0.0f, 100.0f)));
        fEffectList.add(new FxNegative().setupEffect(getString(R.string.negative),R.drawable.negative, new EffectProperty(0.0f, 0.0f, 0.0f)));

        fEffectList.add(new FxColorize().setupEffect(getString(R.string.colorize),R.drawable.colorize, new EffectProperty(360.0f, 0.0f, 0.0f)));
        fEffectList.add(new FxDesaturate().setupEffect(getString(R.string.desaturate),R.drawable.desaturate, new EffectProperty(0.0f, 0.0f, 0.0f)));

        fEffectList.add(new FxColorRed().setupEffect(getString(R.string.overlay_red), R.drawable.redtone, new EffectProperty(0.0f, 0.0f, 0.0f)));
        fEffectList.add(new FxColorGreen().setupEffect(getString(R.string.overlay_green),R.drawable.greentone, new EffectProperty(0.0f, 0.0f, 0.0f)));
        fEffectList.add(new FxColorBlue().setupEffect(getString(R.string.overlay_blue),R.drawable.bluetone, new EffectProperty(0.0f, 0.0f, 0.0f)));

        fEffectList.add(new FxReflect().setupEffect(getString(R.string.reflect),R.drawable.reflect, new EffectProperty(0.0f, 0.0f, 0.0f)));
        fEffectList.add(new FxRoundCorner().setupEffect(getString(R.string.round_corners),R.drawable.roundcorners, new EffectProperty(100.0f, 1.0f, 16.0f)));
    }


    public void pinViews() {
        background = (ImageView) findViewById(R.id.ivBackground);
        scrollEffectContainer = findViewById(R.id.hsvScroller);
        editEffectContainer = (LinearLayout) findViewById(R.id.llEditEffect);
        effectSlider = (SeekBar) findViewById(R.id.sbEffectBar);
        btnConfirmChanges = (Button) findViewById(R.id.btnConfirmChanges);
        btnCancelChanges = (Button) findViewById(R.id.btnCancelChanges);
        cardContainer = (LinearLayout) findViewById(R.id.llCardEffectContainer);
        confirmChangesContainer = (LinearLayout) findViewById(R.id.llConfirmChanges);
        confirmChangesContainer.setVisibility(View.GONE);
        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        pbProgress.setVisibility(View.GONE);
        masterEffectContainer = (LinearLayout) findViewById(R.id.llEffectMasterContainer);
        masterEffectContainer.bringToFront();

        btnConfirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fEditMode = false;
                fLastEditBitmap = Bitmap.createBitmap(fSubmitBitmap, 0, 0, fSubmitBitmap.getWidth(), fSubmitBitmap.getHeight(), null, true);
                HistoryHelper history = new HistoryHelper(fLastIdxEffect, fLastAmountEffect);
                fUndoEffect.add(history);
                fRedoEffect.clear();
                btnCancelChanges.performClick();

            }
        });

        btnCancelChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fEditMode = false;
                editEffectContainer.setVisibility(View.GONE);
                confirmChangesContainer.setVisibility(View.GONE);
                scrollEffectContainer.setVisibility(View.VISIBLE);
                updateBackground(fLastEditBitmap);
                fSubmitBitmap.recycle();
                scrollEffectContainer.bringToFront();
                masterEffectContainer.bringToFront();
            }
        });

    }

    public void openReceivedFile() {

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        openedFile = getIntent().getStringExtra(IRMainActivity.IR_FILE_TO_OPEN);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    openedFile = BitmapUtils.getRealImagePathFromURI(getApplicationContext(), imageUri, MediaStore.Images.Media.DATA);
                    if (openedFile.length() <= 0) {
                        openedFile = BitmapUtils.getRealImagePathFromURI(getApplicationContext(), imageUri, MediaStore.Images.Media.DISPLAY_NAME);
                    }

                }
            }
        }
        loadInitialImage(true);
    }




    @Override
    public void onBackPressed() {
        if (fUndoEffect.size() > 0) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.exit_without_saving_msg))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
        else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        catch (Exception e) {}

        fContext = this;
        fUndoEffect = new ArrayList<HistoryHelper>();
        fRedoEffect = new ArrayList<HistoryHelper>();
        setContentView(R.layout.activity_iredit);
        pinViews();
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        else {
            openReceivedFile();
        }
        buildEffectList();
        buildEffects();
        scrollEffectContainer.bringToFront();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        fLastEditBitmap = savedInstanceState.getParcelable("last_bitmap");

        openedFile = savedInstanceState.getString("filename");

        int undoCount = savedInstanceState.getInt("undo_count");
        fUndoEffect.clear();
        for (int i=0; i< undoCount; i++) {
            fUndoEffect.add( new HistoryHelper(savedInstanceState.getInt("undo_index_"+i),savedInstanceState.getFloat("undo_amount_"+i)));
        }

        int redoCount = savedInstanceState.getInt("redo_count");
        fRedoEffect.clear();
        for (int i=0; i< redoCount; i++) {
            fRedoEffect.add( new HistoryHelper(savedInstanceState.getInt("redo_index_"+i),savedInstanceState.getFloat("redo_amount_"+i)));
        }

        updateBackground(fLastEditBitmap);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("last_bitmap",fLastEditBitmap);

        outState.putString("filename", openedFile);

        for (int i=0; i< fUndoEffect.size(); i++) {
            outState.putInt("undo_index_"+i, fUndoEffect.get(i).getIndex());
            outState.putFloat("undo_amount_" + i, fUndoEffect.get(i).getAmount());
        }
        outState.putInt("undo_count",fUndoEffect.size());

        for (int i=0; i< fRedoEffect.size(); i++) {
            outState.putInt("redo_index_"+i, fRedoEffect.get(i).getIndex());
            outState.putFloat("redo_amount_" + i, fRedoEffect.get(i).getAmount());
        }
        outState.putInt("redo_count",fRedoEffect.size());
    }


    public void buildEffects() {

        for (int i=0; i<fEffectList.size(); i ++) {
            View view = getLayoutInflater().inflate(R.layout.effect_card, null);
            TextView textView = (TextView) view.findViewById(R.id.cardText);
            ImageView imageView = (ImageView) view.findViewById(R.id.cardImage);

            fEffectList.get(i).setEffectIndex(i);
            textView.setText(fEffectList.get(i).getEffectName());
            imageView.setImageDrawable(getResources().getDrawable(fEffectList.get(i).getEffectThumbnail()));
            view.setTag(fEffectList.get(i));
            textView.setTag(fEffectList.get(i));
            imageView.setTag(fEffectList.get(i));


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BaseEffect effect = (BaseEffect) view.getTag();

                    fEditMode = true;
                    scrollEffectContainer.setVisibility(View.GONE);




                    if ((effect.getEffectProperties().getDefValue() == 0.0f) &&
                            (effect.getEffectProperties().getMaxValue() == 0.0f) &&
                            (effect.getEffectProperties().getMinValue() == 0.0f)) {


                        effectSlider.setVisibility(View.GONE);
                        pbProgress.setVisibility(View.VISIBLE);
                        pbProgress.bringToFront();
                        doEffect(effect, true);
                    }
                    else {

                        effectSlider.setVisibility(View.VISIBLE);
                        effectSlider.setTag(effect);

                        effectSlider.setMax((int) ((effect.getEffectProperties().getMaxValue() - effect.getEffectProperties().getMinValue())));

                        effectSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                                BaseEffect effect = (BaseEffect) seekBar.getTag();
                                effect.setEffectAmount(i + effect.getEffectProperties().getMinValue());
                                doEffect(effect,false);


                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                        int oldprg = effectSlider.getProgress();
                        effectSlider.setProgress((int) (effect.getEffectProperties().getDefValue() - effect.getEffectProperties().getMinValue()));

                        if (oldprg == effectSlider.getProgress()) {
                          doEffect(effect, false);
                        }

                        editEffectContainer.setVisibility(View.VISIBLE);
                        editEffectContainer.bringToFront();
                    }




                }
            });

            cardContainer.addView(view);
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.iredit, menu);
        menuBar = menu;
        try {
          updateButtonUndoRedoRestore();

        } catch (Exception e) { }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final Activity act = this;


        if (id == android.R.id.home) {
            if (fUndoEffect.size() > 0) {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.exit_without_saving_msg))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(act);
                            }

                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            }
            else {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }

        if (id == R.id.action_savefile) {
            storeFile();
            return true;
        }

        if (id == R.id.action_restorefile) {
            restoreOriginalFile();
            return true;
        }

        if (id == R.id.action_undo) {
            undo();
            return true;
        }

        if (id == R.id.action_redo) {
            redo();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(IREditActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void loadInitialImage(boolean updateBkg) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.y;
        int height = size.x;
        fLastEditBitmap = BitmapUtils.decodeSampledBitmapFromResource(openedFile, width, height);
        if (updateBkg) updateBackground(fLastEditBitmap);
    }

    public void restoreOriginalFile() {

        final AlertDialog deleteAlert = new AlertDialog.Builder(this).create();
        deleteAlert.setTitle(getString(R.string.restore_original));
        deleteAlert.setMessage(getString(R.string.confirm_restore_image_msg));
        deleteAlert.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.yes), new DialogInterface.OnClickListener(){
        @Override
            public void onClick(DialogInterface dialog, int which) {
              fUndoEffect.clear();
              fRedoEffect.clear();
              loadInitialImage(true);
              deleteAlert.dismiss();
            }
        });
        deleteAlert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAlert.dismiss();
            }
        });
        deleteAlert.show();


    }


    public void undo() {

        if (fUndoEffect.size() <= 0) return;

        ProgressDialog progressDialog = ProgressDialog.show(this,getString(R.string.wait),getString(R.string.undo)+"...",true);
        new AsyncUnDo().execute(progressDialog);
    }

    public void redo() {

        if (fRedoEffect.size() <= 0) return;

        ProgressDialog progressDialog = ProgressDialog.show(this,getString(R.string.wait),getString(R.string.redo)+"...",true);
        new AsyncReDo().execute(progressDialog);
    }

    public void storeFile() {

        if (fSubmitBitmap != null) fSubmitBitmap.recycle();
        ProgressDialog progressDialog = ProgressDialog.show(this,getString(R.string.wait),getString(R.string.save_in_progress)+"...",false);
        new fileStorer().execute(progressDialog);
    }



    public void doEffect(BaseEffect effect,boolean async) {

        if (async) {
            new effectBackgroundRender().execute(effect);
        }
        else {


            fSubmitBitmap = effect.doEffect(fLastEditBitmap);
            fLastIdxEffect = effect.getEffectIndex();
            fLastAmountEffect = effect.getEffectAmount();

            updateBackground(fSubmitBitmap);
            disableAllMenuButtons();
            if (confirmChangesContainer.getVisibility() != View.VISIBLE) {
                confirmChangesContainer.setVisibility(View.VISIBLE);
                confirmChangesContainer.bringToFront();

            }

        }

    }


    private class effectBackgroundRender extends AsyncTask<BaseEffect, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(BaseEffect... baseEffects) {

            fLastIdxEffect = baseEffects[0].getEffectIndex();
            fLastAmountEffect = baseEffects[0].getEffectAmount();
            return baseEffects[0].doEffect(fLastEditBitmap);
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (fEditMode) {
                synchronized (background) {
                    updateBackground(result);
                    disableAllMenuButtons();
                }
                fSubmitBitmap = result;

                if (pbProgress.getVisibility() != View.GONE) pbProgress.setVisibility(View.GONE);
                if (confirmChangesContainer.getVisibility() != View.VISIBLE) {
                    confirmChangesContainer.setVisibility(View.VISIBLE);
                    confirmChangesContainer.bringToFront();

                }
            }
        }
    }

    public void updateBackground(Bitmap b) {
        background.setImageBitmap(b);
        updateButtonUndoRedoRestore();

    }


    private void disableAllMenuButtons() {
        if (menuBar != null) {
         int alphaValue = 130;
         menuBar.findItem(R.id.action_undo).setEnabled(false).getIcon().setAlpha(alphaValue);
         menuBar.findItem(R.id.action_redo).setEnabled(false).getIcon().setAlpha(alphaValue);
         menuBar.findItem(R.id.action_restorefile).setEnabled(false).getIcon().setAlpha(alphaValue);
         menuBar.findItem(R.id.action_savefile).setEnabled(false).getIcon().setAlpha(alphaValue);
        }
    }

    private void updateButtonUndoRedoRestore() {

        if (menuBar != null) {
            int alphaUndo = 255;
            int alphaRedo = 255;
            if (fUndoEffect.size() <= 0) alphaUndo = 130;
            if (fRedoEffect.size() <= 0) alphaRedo = 130;

            menuBar.findItem(R.id.action_undo).setEnabled(fUndoEffect.size() > 0).getIcon().setAlpha(alphaUndo);
            menuBar.findItem(R.id.action_redo).setEnabled(fRedoEffect.size() > 0).getIcon().setAlpha(alphaRedo);
            menuBar.findItem(R.id.action_restorefile).setEnabled(fUndoEffect.size() > 0).getIcon().setAlpha(alphaUndo);
            menuBar.findItem(R.id.action_savefile).setEnabled(fUndoEffect.size() > 0).getIcon().setAlpha(alphaUndo);

        }
    }



    private class fileStorer extends AsyncTask<ProgressDialog, Void, ProgressDialog> {
        private boolean myresult;

        @Override
        protected ProgressDialog doInBackground(ProgressDialog... dialogs) {

            myresult = false;

            String path = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);

            OutputStream fOut = null;

            String newFilename = openedFile.substring(openedFile.lastIndexOf("/") + 1, openedFile.length());

            newFilename = newFilename.substring(0, newFilename.lastIndexOf(".") - 1);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);


            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(fContext);
            boolean saveAsPng = sharedPrefs.getBoolean("save_as_png_to_maintain_transparency", false);

            String ext = ".jpg";
            if (saveAsPng) { ext = ".png"; }

            newFilename += "-" + sdf.format(new Date()) + "_0"+ext;


            File tmp = new File(path);
            tmp.mkdir();


            File file = new File(path, newFilename);
            int k = 0;
            while (file.exists()) {
                k++;
                newFilename += "-" + sdf.format(new Date()) +'_' + k + ext;
                file = new File(path, newFilename);
            }

            try {
                fOut = new FileOutputStream(file);
                try {

                    if (saveAsPng) {
                        fLastEditBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    }
                    else {
                        fLastEditBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    }
                    fOut.flush();
                } finally {
                    fOut.close();

                    String[] fileToScan = new String[1];
                    fileToScan[0] = path+"/"+newFilename;
                    MediaScannerConnection.scanFile(fContext,fileToScan,null,null);

                    openedFile = fileToScan[0];
                    fUndoEffect.clear();
                    fRedoEffect.clear();



                    myresult = true;

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dialogs[0];


        }

        @Override
        protected void onPostExecute(ProgressDialog result) {
            result.dismiss();

            if (! myresult) {
              Toast.makeText(fContext,getString(R.string.unable_to_save_data),Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(fContext,getString(R.string.save_completed),Toast.LENGTH_SHORT).show();
            }
            updateButtonUndoRedoRestore();
        }
    }



    private class AsyncUnDo extends AsyncTask<ProgressDialog, Void, ProgressDialog> {
        private boolean myResult;

        @Override
        protected ProgressDialog doInBackground(ProgressDialog... dialogs) {
            myResult = false;

            loadInitialImage(false);
            for (int i = 0; i<fUndoEffect.size()-1; i++) {
                fEffectList.get(fUndoEffect.get(i).getIndex()).setEffectAmount(fUndoEffect.get(i).getAmount());
                fLastEditBitmap = fEffectList.get(fUndoEffect.get(i).getIndex()).doEffect(fLastEditBitmap);
            }
            fRedoEffect.add(fUndoEffect.get(fUndoEffect.size()-1));
            fUndoEffect.remove(fUndoEffect.size()-1);
            myResult = true;
            return dialogs[0];
        }

        @Override
        protected void onPostExecute(ProgressDialog result) {
            result.dismiss();
            if (! myResult) {
                 Toast.makeText(fContext, getString(R.string.there_was_an_error_while)+" "+getString(R.string.undo), Toast.LENGTH_SHORT).show();
            }
            else {
                updateBackground(fLastEditBitmap);
            }
        }
    }

    private class AsyncReDo extends AsyncTask<ProgressDialog, Void, ProgressDialog> {
        private boolean myResult;

        @Override
        protected ProgressDialog doInBackground(ProgressDialog... dialogs) {
            myResult = false;

            fEffectList.get(fRedoEffect.get(fRedoEffect.size()-1).getIndex()).setEffectAmount(fRedoEffect.get(fRedoEffect.size()-1).getAmount());
            fLastEditBitmap = fEffectList.get(fRedoEffect.get(fRedoEffect.size()-1).getIndex()).doEffect(fLastEditBitmap);
            fUndoEffect.add(fRedoEffect.get(fRedoEffect.size()-1));
            fRedoEffect.remove(fRedoEffect.size()-1);
            myResult = true;
            return dialogs[0];
        }

        @Override
        protected void onPostExecute(ProgressDialog result) {
            result.dismiss();
            if (! myResult) {
                Toast.makeText(fContext,getString(R.string.there_was_an_error_while)+" "+getString(R.string.redo),Toast.LENGTH_SHORT).show();
            }
            else {
                updateBackground(fLastEditBitmap);
            }
        }
    }






}
