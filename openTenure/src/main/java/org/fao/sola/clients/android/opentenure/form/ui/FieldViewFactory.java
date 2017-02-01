/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.fao.sola.clients.android.opentenure.form.ui;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.ModeDispatcher.Mode;
import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintOption;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldTemplate;
import org.fao.sola.clients.android.opentenure.form.constraint.DateTimeFormatConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.OptionConstraint;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class FieldViewFactory {

	private static final long MIN_TIME_BETWEEN_TOAST = 500;

	public static View getSpinner(final Activity activity,
			final DisplayNameLocalizer dnl,
			List<FieldConstraintOption> options, final FieldTemplate field,
			final FieldPayload payload, Mode mode) {

		List<String> displayNames = new ArrayList<String>();
		final List<String> names = new ArrayList<String>();
		int selected = -1;

		boolean isOptional = true;
		List<FieldConstraint> fieldConstraintList = field.getFieldConstraintList();

		if(fieldConstraintList != null){
			for (FieldConstraint constraint : fieldConstraintList) {
				if (constraint != null
						&& FieldConstraintType.NOT_NULL == constraint
								.getFieldConstraintType()) {
					isOptional = false;
				}
			}
		}

		for (FieldConstraintOption option : options) {
			if (payload.getStringPayload() != null
					&& payload.getStringPayload().equals(option.getName())) {
				// Previous selection: select it in the list
				selected = names.size();
			}
			names.add(option.getName());
			displayNames.add(dnl.getLocalizedDisplayName(option.getDisplayName()));
		}

		if (isOptional) {
			if (selected == -1) {
				// No previous selection: select the null value if available
				selected = names.size();
			}
			String nullDisplayName = activity.getBaseContext().getString(
					R.string.null_label);
			options.add(new FieldConstraintOption(null, nullDisplayName, 0));
			names.add(null);
			displayNames.add(nullDisplayName);
		}

		if (names.size() > 0 && selected == -1) {
			// No previous selection, not null constraint and at least one
			// option: select the first option in the list
			selected = 0;
		}

		final Spinner spinner = new Spinner(activity);
		spinner.setPadding(0, 10, 0, 8);
		spinner.setBackgroundColor(activity.getResources().getColor(
				android.R.color.white));

		if (mode == Mode.MODE_RO) {
			spinner.setEnabled(false);
			spinner.setClickable(false);
			spinner.setLongClickable(false);
		}

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
				activity, android.R.layout.simple_spinner_dropdown_item,
				displayNames);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				payload.setStringPayload(names.get(arg2));
				FieldConstraint constraint;
				if ((constraint = field.getFailedConstraint(
						dnl.getLocalizedDisplayName(field.getDisplayName()), payload)) != null) {
					((TextView) arg0.getChildAt(0)).setTextColor(Color.RED);
					Toast.makeText(activity.getBaseContext(),
							constraint.displayErrorMsg(), Toast.LENGTH_SHORT)
							.show();
				} else {
					((TextView) arg0.getChildAt(0)).setTextColor(Color.BLACK);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		spinner.setAdapter(spinnerArrayAdapter);
		if (selected != -1) {
			spinner.setSelection(selected);
		}

		return spinner;
	}

	public static View getViewForTextField(final Activity activity,
			final DisplayNameLocalizer dnl,
			final FieldTemplate field,
			final FieldPayload payload, Mode mode) {
		List<FieldConstraint> fieldConstraintList = field.getFieldConstraintList();
		if(fieldConstraintList != null){
			for (FieldConstraint constraint : fieldConstraintList) {
				if (constraint instanceof OptionConstraint) {
					return getSpinner(activity, dnl,
							((OptionConstraint) constraint)
									.getFieldConstraintOptionList(), field,
							payload, mode);
				}
			}
		}
		final EditText text;
		text = new EditText(activity);
		text.setPadding(0, 10, 0, 8);
		text.setTextSize(20);
		text.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
		text.setBackgroundColor(activity.getResources().getColor(
				android.R.color.white));
		text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		text.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		if (payload.getStringPayload() != null) {
			text.setText(payload.getStringPayload());
		}
		if (mode == Mode.MODE_RO) {
			text.setEnabled(false);
			text.setClickable(false);
			text.setLongClickable(false);
		} else {
			text.setHint(dnl.getLocalizedDisplayName(field.getHint()));
			text.addTextChangedListener(new TextWatcher() {
				long lastTime = System.currentTimeMillis();

				@Override
				public void afterTextChanged(Editable s) {

					if ("".toString().equalsIgnoreCase(s.toString())) {
						payload.setStringPayload(null);
					} else {
						payload.setStringPayload(s.toString());
					}

					FieldConstraint constraint;
					if ((constraint = field.getFailedConstraint(
							dnl.getLocalizedDisplayName(field.getDisplayName()), payload)) != null) {
						text.setTextColor(Color.RED);
						if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
							Toast.makeText(activity.getBaseContext(),
									constraint.displayErrorMsg(),
									Toast.LENGTH_SHORT).show();
							lastTime = System.currentTimeMillis();
						}

					} else {
						text.setTextColor(Color.BLACK);
					}

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}
			});
		}
		return text;
	}

	public static View getViewForNumberField(final Activity activity,
			final DisplayNameLocalizer dnl,
			final FieldTemplate field, final FieldPayload payload, Mode mode) {
		final EditText number;
		number = new EditText(activity);
		number.setPadding(0, 10, 0, 8);
		number.setTextSize(20);
		number.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
		number.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		number.setBackgroundColor(activity.getResources().getColor(
				android.R.color.white));
		number.setInputType(InputType.TYPE_CLASS_NUMBER);
		if (payload.getBigDecimalPayload() != null) {
			if(isInteger(payload.getBigDecimalPayload())){
				number.setText(payload.getBigDecimalPayload().toBigInteger().toString());
			}else{
				number.setText(payload.getBigDecimalPayload().toPlainString());
			}
		}
		if (mode == Mode.MODE_RO) {
			number.setEnabled(false);
			number.setClickable(false);
			number.setLongClickable(false);
		} else {
			number.setHint(dnl.getLocalizedDisplayName(field.getHint()));
			number.addTextChangedListener(new TextWatcher() {
				long lastTime = System.currentTimeMillis();

				@Override
				public void afterTextChanged(Editable s) {
					if ("".toString().equalsIgnoreCase(s.toString())) {
						payload.setBigDecimalPayload(null);
					} else {
						payload.setBigDecimalPayload(new BigDecimal(Double
								.parseDouble(s.toString())));
					}

					FieldConstraint constraint;
					if ((constraint = field.getFailedConstraint(
							dnl.getLocalizedDisplayName(field.getDisplayName()), payload)) != null) {
						number.setTextColor(Color.RED);
						if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
							Toast.makeText(activity.getBaseContext(),
									constraint.displayErrorMsg(),
									Toast.LENGTH_SHORT).show();
							lastTime = System.currentTimeMillis();
						}
					} else {
						number.setTextColor(Color.BLACK);
					}

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}
			});
		}
		return number;
	}
	
	private static boolean isInteger(BigDecimal bd) {
		  return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
		}

	public static View getViewForDecimalField(final Activity activity,
			final DisplayNameLocalizer dnl,
			final FieldTemplate field, final FieldPayload payload, Mode mode) {
		final EditText number;
		number = new EditText(activity);
		number.setPadding(0, 10, 0, 8);
		number.setTextSize(20);
		number.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
		number.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		number.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		number.setBackgroundColor(activity.getResources().getColor(
				android.R.color.white));
		if (payload.getBigDecimalPayload() != null) {
			if(isInteger(payload.getBigDecimalPayload())){
				number.setText(payload.getBigDecimalPayload().toBigInteger().toString());
			}else{
				number.setText(payload.getBigDecimalPayload().toPlainString());
			}
		}
		if (mode == Mode.MODE_RO) {
			number.setEnabled(false);
			number.setClickable(false);
			number.setLongClickable(false);
		} else {
			number.setHint(dnl.getLocalizedDisplayName(field.getHint()));
			number.addTextChangedListener(new TextWatcher() {
				long lastTime = System.currentTimeMillis();

				@Override
				public void afterTextChanged(Editable s) {
					if ("".toString().equalsIgnoreCase(s.toString())) {
						payload.setBigDecimalPayload(null);
					} else {
						payload.setBigDecimalPayload(new BigDecimal(Double
								.parseDouble(s.toString())));
					}

					FieldConstraint constraint;
					if ((constraint = field.getFailedConstraint(
							dnl.getLocalizedDisplayName(field.getDisplayName()), payload)) != null) {
						number.setTextColor(Color.RED);
						if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
							Toast.makeText(activity.getBaseContext(),
									constraint.displayErrorMsg(),
									Toast.LENGTH_SHORT).show();
							lastTime = System.currentTimeMillis();
						}
					} else {
						number.setTextColor(Color.BLACK);
					}

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}
			});
		}
		return number;
	}

	public static View getViewForBooleanField(final Activity activity,
			final DisplayNameLocalizer dnl,
			final FieldTemplate field, final FieldPayload payload, Mode mode) {
		final Switch bool;
		bool = new Switch(activity);
		bool.setPadding(0, 10, 0, 8);
		bool.setTextSize(20);
		bool.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
		bool.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		bool.setTextOn(activity.getResources().getString(R.string.yes));
		bool.setTextOff(activity.getResources().getString(R.string.no));
		if (payload.getBooleanPayload() != null) {
			bool.setChecked(payload.getBooleanPayload().booleanValue());
		} else {
			payload.setBooleanPayload(Boolean.valueOf(false));
			bool.setChecked(false);
		}
		if (mode == Mode.MODE_RO) {
			bool.setEnabled(false);
			bool.setClickable(false);
			bool.setLongClickable(false);
		} else {
			bool.setHint(dnl.getLocalizedDisplayName(field.getHint()));
			bool.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (((Switch) v).isChecked()) {
						payload.setBooleanPayload(Boolean.valueOf(true));
					} else {
						payload.setBooleanPayload(Boolean.valueOf(false));
					}
				}

			});
		}
		return bool;
	}

	public static View getViewForDateField(final Activity activity,
			final DisplayNameLocalizer dnl,
			final FieldTemplate field, final FieldPayload payload, Mode mode) {
		String tmpFormat = null;
		List<FieldConstraint> fieldConstraintList = field.getFieldConstraintList();
		if(fieldConstraintList != null){
			for (FieldConstraint constraint : fieldConstraintList) {
				if (constraint instanceof DateTimeFormatConstraint
						&& constraint.getFormat() != null) {
					tmpFormat = constraint.getFormat();
				}
			}
		}

		if (tmpFormat == null) {
			tmpFormat = "yyyy-MM-dd";
		}

		final String format = tmpFormat;
		final EditText datetime;
		final Calendar localCalendar = Calendar.getInstance();
		datetime = new EditText(activity);
		datetime.setPadding(0, 10, 0, 8);
		datetime.setTextSize(20);
		datetime.setTextAppearance(activity,
				android.R.style.TextAppearance_Medium);
		datetime.setBackgroundColor(activity.getResources().getColor(
				android.R.color.white));
		datetime.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		datetime.setInputType(InputType.TYPE_CLASS_DATETIME);
		if (payload.getStringPayload() != null) {
			datetime.setText(payload.getStringPayload());
		}
		if (mode == Mode.MODE_RO) {
			datetime.setEnabled(false);
			datetime.setClickable(false);
			datetime.setLongClickable(false);
		} else {
			datetime.setHint(dnl.getLocalizedDisplayName(field.getHint()));
			final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					localCalendar.set(Calendar.YEAR, year);
					localCalendar.set(Calendar.MONTH, monthOfYear);
					localCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					if (format != null) {
						SimpleDateFormat sdf = new SimpleDateFormat(format,
								Locale.US);
						sdf.format(localCalendar.getTime());
						datetime.setText(sdf.format(localCalendar.getTime()));
					}
				}

			};

			datetime.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					new DatePickerDialog(activity, date, localCalendar
							.get(Calendar.YEAR), localCalendar
							.get(Calendar.MONTH), localCalendar
							.get(Calendar.DAY_OF_MONTH)).show();
					return true;
				}
			});
			datetime.addTextChangedListener(new TextWatcher() {
				long lastTime = System.currentTimeMillis();

				@Override
				public void afterTextChanged(Editable s) {

					if ("".toString().equalsIgnoreCase(s.toString())) {
						payload.setStringPayload(null);
					} else {
						payload.setStringPayload(s.toString());
					}

					FieldConstraint constraint;
					if ((constraint = field.getFailedConstraint(
							dnl.getLocalizedDisplayName(field.getDisplayName()), payload)) != null) {
						datetime.setTextColor(Color.RED);
						if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
							Toast.makeText(activity.getBaseContext(),
									constraint.displayErrorMsg(),
									Toast.LENGTH_SHORT).show();
							lastTime = System.currentTimeMillis();
						}
					} else {
						datetime.setTextColor(Color.BLACK);
					}

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}
			});
		}
		return datetime;
	}

	public static View getViewForTimeField(final Activity activity,
			final DisplayNameLocalizer dnl,
			final FieldTemplate field, final FieldPayload payload, Mode mode) {
		String tmpFormat = null;
		List<FieldConstraint> fieldConstraintList = field.getFieldConstraintList();
		if(fieldConstraintList != null){
			for (FieldConstraint constraint : fieldConstraintList) {
				if (constraint instanceof DateTimeFormatConstraint
						&& constraint.getFormat() != null) {
					tmpFormat = constraint.getFormat();
				}
			}
		}

		if (tmpFormat == null) {
			tmpFormat = "HH:mm";
		}

		final String format = tmpFormat;
		final EditText datetime;
		final Calendar localCalendar = Calendar.getInstance();
		datetime = new EditText(activity);
		datetime.setPadding(0, 10, 0, 8);
		datetime.setTextSize(20);
		datetime.setTextAppearance(activity,
				android.R.style.TextAppearance_Medium);
		datetime.setBackgroundColor(activity.getResources().getColor(
				android.R.color.white));
		datetime.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		datetime.setInputType(InputType.TYPE_CLASS_DATETIME);
		if (payload.getStringPayload() != null) {
			datetime.setText(payload.getStringPayload());
		}
		if (mode == Mode.MODE_RO) {
			datetime.setEnabled(false);
			datetime.setClickable(false);
			datetime.setLongClickable(false);
		} else {
			datetime.setHint(dnl.getLocalizedDisplayName(field.getHint()));
			final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

				@Override
				public void onTimeSet(TimePicker view, int hour, int min) {
					localCalendar.set(Calendar.HOUR_OF_DAY, hour);
					localCalendar.set(Calendar.MINUTE, min);
					if (format != null) {
						SimpleDateFormat sdf = new SimpleDateFormat(format,
								Locale.US);
						sdf.format(localCalendar.getTime());
						datetime.setText(sdf.format(localCalendar.getTime()));
					}
				}

			};

			datetime.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					new TimePickerDialog(activity, time, localCalendar
							.get(Calendar.HOUR_OF_DAY), localCalendar
							.get(Calendar.MINUTE), true).show();
					return true;
				}
			});
			datetime.addTextChangedListener(new TextWatcher() {
				long lastTime = System.currentTimeMillis();

				@Override
				public void afterTextChanged(Editable s) {

					if ("".toString().equalsIgnoreCase(s.toString())) {
						payload.setStringPayload(null);
					} else {
						payload.setStringPayload(s.toString());
					}

					FieldConstraint constraint;
					if ((constraint = field.getFailedConstraint(
							dnl.getLocalizedDisplayName(field.getDisplayName()), payload)) != null) {
						datetime.setTextColor(Color.RED);
						if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
							Toast.makeText(activity.getBaseContext(),
									constraint.displayErrorMsg(),
									Toast.LENGTH_SHORT).show();
							lastTime = System.currentTimeMillis();
						}
					} else {
						datetime.setTextColor(Color.BLACK);
					}

				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

				}
			});
		}

		return datetime;
	}
}
