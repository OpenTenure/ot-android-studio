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
package org.fao.sola.clients.android.opentenure.button.listener;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.R.string;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class DeletePersonListener implements OnClickListener {

	String personId;

	public DeletePersonListener(String personId) {

		this.personId = personId;

	}

	@Override
	public void onClick(View v) {
		final Person person = Person.getPerson(personId);
		AlertDialog.Builder deletePersonDialog = new AlertDialog.Builder(v.getContext());
		deletePersonDialog.setTitle(R.string.title_remove_person_dialog);
		String dialogMessage = String
				.format(OpenTenureApplication.getContext().getString(
						R.string.message_remove_person_dialog,
						person.getFirstName() + " " + person.getLastName()));

		deletePersonDialog.setMessage(dialogMessage);

		deletePersonDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				Toast toast;

				int result = Person.deletePerson(person);

				if (result > 0) {

					FileSystemUtilities.deleteCLaimant(personId);

					String toastMessage = String
							.format(OpenTenureApplication.getContext().getString(
									R.string.message_remove_person,
									person.getFirstName() + " " + person.getLastName()));

					toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage,
							Toast.LENGTH_LONG);
					toast.show();

//					OpenTenureApplication.getPersonsFragment().refresh();

				}

				else {

					String toastMessage = String
							.format(OpenTenureApplication.getContext().getString(
									R.string.message_error_remove_person,
									person.getFirstName() + " " + person.getLastName()));

					toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage,
							Toast.LENGTH_LONG);
					toast.show();

				}

			}
		});
		deletePersonDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		deletePersonDialog.show();

	}

}
