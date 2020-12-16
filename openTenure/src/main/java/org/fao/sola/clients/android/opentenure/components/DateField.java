package org.fao.sola.clients.android.opentenure.components;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import org.fao.sola.clients.android.opentenure.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import static android.text.InputType.*;

public class DateField extends EditText {

    private final Calendar cal = Calendar.getInstance();
    private Drawable calImage;
    private DatePickerDialog.OnDateSetListener datePicker = null;
    private DatePickerDialog datePickerDialog = null;

    public DateField(Context context) {
        super(context);
        init(context);
    }

    public DateField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DateField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DateField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context ctx){
        setInputType(TYPE_CLASS_DATETIME);
        setHint(R.string.date_of_start);
        setBackgroundColor(Color.WHITE);
        setTextAppearance(android.R.style.TextAppearance_Medium);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        int width = (int) (145 * scale + 0.5f);
        int padding = (int) (5 * scale + 0.5f);

        setMinimumWidth(width);
        setPadding(padding, padding, 1, padding);

        calImage = ResourcesCompat.getDrawable(getResources(), R.drawable.calendar, null);

        datePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, monthOfYear);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                setText(sdf.format(cal.getTime()));
            }
        };

        showCalButton();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((getCompoundDrawablesRelative()[2] != null)) {
                    float clearButtonStart; // Used for LTR languages
                    float clearButtonEnd;  // Used for RTL languages
                    boolean isCalButtonClicked = false;
                    // Detect the touch in RTL or LTR layout direction.
                    if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                        // If RTL, get the end of the button on the left side.
                        clearButtonEnd = calImage.getIntrinsicWidth() + getPaddingStart();
                        // If the touch occurred before the end of the button,
                        if (event.getX() < clearButtonEnd) {
                            isCalButtonClicked = true;
                        }
                    } else {
                        // Layout is LTR.
                        clearButtonStart = (getWidth() - getPaddingEnd() - calImage.getIntrinsicWidth());
                        if (event.getX() > clearButtonStart) {
                            isCalButtonClicked = true;
                        }
                    }

                    // Check for actions if the button is tapped.
                    if (isCalButtonClicked) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            calImage = ResourcesCompat.getDrawable(getResources(), R.drawable.calendar_black, null);
                            showCalButton();
                        }
                        // Check for ACTION_UP.
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            calImage =ResourcesCompat.getDrawable(getResources(), R.drawable.calendar, null);
                            showCalButton();
                            showCalendar(v);
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
                return false;
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showCalendar(v);
                return true;
            }
        });

        // Add input formatting
        addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newValue = s.toString();

                if (!newValue.equals(current) && !newValue.equals("")) {

                    boolean appending = before <= 0;
                    int pos = start;

                    if(appending){
                        // Move one char further
                       pos+=1;
                        // Jump over "-"
                        if(pos == 4 || pos == 7 || start == 4 || start == 7){
                            pos+=1;
                        }
                    }

                    // Replace new string in old string to allow replacing date parts
                    if(appending && start < newValue.length() - 1){
                        StringBuilder sbNew = new StringBuilder(current);
                        int end = start + count;
                        int replaceStrat = start;
                        int replaceEnd = end;

                        if(start == 4 || start==7){
                            replaceStrat+=1;
                            replaceEnd+=1;
                        }

                        if(current.length() - 1 >= replaceEnd && newValue.length() - 1 >= end) {
                            newValue = sbNew.replace(replaceStrat, replaceEnd, newValue.substring(start, end)).toString();
                        }
                    }

                    String clean = newValue.replaceAll("[^\\d.]|\\.", "");

                    if(clean.length() > 8){
                        clean = clean.substring(0, 8);
                    }

                    int cl = clean.length();
                    int currLengh = newValue.length();

                    StringBuilder sb = new StringBuilder(clean);


                    if (cl >= 4){
                        int year = Integer.parseInt(clean.substring(0,4));
                        year = (year<1800)?1800:(year>2200)?2200:year;
                        cal.set(Calendar.YEAR, year);

                        sb.replace(0, 4, String.format("%02d", year));
                        if(appending || currLengh > 5) {
                            sb.insert(4, "-");
                        }
                    }

                    if (cl >= 6){
                        int mon  = Integer.parseInt(clean.substring(4,6));
                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon-1);

                        sb.replace(5, 7, String.format("%02d", mon));
                        if(appending || currLengh > 8) {
                            sb.insert(7, "-");
                        }
                    }

                    if (cl >= 8){
                        int day  = Integer.parseInt(clean.substring(6,8));
                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        cal.set(Calendar.DAY_OF_MONTH, day);

                        sb.replace(8, 10, String.format("%02d", day));
                    }

                    current = sb.toString();
                    setText(current);
                    setSelection(pos > current.length() ? current.length() : pos);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Validate date on losing focus
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String myFormat = "yyyy-MM-dd";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    sdf.setLenient(false);
                    try {
                        Date date = sdf.parse(getText().toString());
                    } catch (ParseException e) {
                        // Date is invalid, revert to empty
                        setText("");
                    }
                }
            }
        });
    }

    private void showCalendar(View v){
        if(datePickerDialog == null) {
            datePickerDialog = new DatePickerDialog(v.getContext(), datePicker,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
        }
        if(!datePickerDialog.isShowing()) {
            datePickerDialog.getDatePicker().updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
    }

    private void showCalButton() {
        setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, calImage,null);
    }
}
