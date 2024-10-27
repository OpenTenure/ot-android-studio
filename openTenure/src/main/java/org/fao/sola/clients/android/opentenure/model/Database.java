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
package org.fao.sola.clients.android.opentenure.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.h2.tools.RunScript;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Database {

	private Context context;

	private String DB_PATH;

	private String DB_NAME;

	private String DB_FILE_NAME;
	private Connection conn;
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (password != null && !password.equals("")) {
			this.password = password;
		} else {
			this.password = null;
		}
	}

	public Database(Context context, String password) {
		this.context = context;
		this.password = password;
		DB_PATH = context.getFilesDir().getPath();
		DB_NAME = "opentenure";
		DB_FILE_NAME = "opentenure.mv.db";
	}

	private String getUrl() {
		String url = "jdbc:h2:file:" + DB_PATH + "/" + DB_NAME + ";USER=sa;PASSWORD=opentenure";
		return url;
	}

	public boolean init() {
		if (!databaseExists()) {
			return createDataBase();
		}
		return false;
	}

	public boolean performUpgrade() {
		List<String> upgradePath = getUpgradePath();
		for (String script : upgradePath) {
			if (!executeScript(script)) {
				return false;
			}
		}
		return true;
	}

	public boolean checkCanOpen() {
		try {
			if(conn !=null) {
				return true;
			}
			// This connection object is likely used by H2 engine as cached pool. Screens get opened faster when this connection is available.
			//Class.forName("org.h2.Driver");
			conn = getConnection();
			if(conn == null) {
				return false;
			}
			Log.d(this.getClass().getName(), "... opened");
			return true;
		} catch (Exception e) {
			conn = null;
			Log.d(this.getClass().getName(), e.getMessage());
		}
		return false;
	}

	public void close() {
		if (conn != null) {
			try {
				password = "";
				Log.d(this.getClass().getName(), "closing db ...");
				conn.close();
				conn = null;
				Log.d(this.getClass().getName(), "... closed");
			} catch (Exception e) {
				Log.d(this.getClass().getName(), e.getMessage());
			}
		}
	}

	public void unlock(final Context context, Runnable okRun, Runnable cancelRun) {
		// Create it, if it doesn't exist
		init();

		// Try to open with default password
		boolean opened = checkCanOpen();
		if(okRun != null) {
			okRun.run();
		}

		/*if (!opened) {
			// We failed so we ask for a password
			AlertDialog.Builder dbPasswordDialog = new AlertDialog.Builder(context);
			dbPasswordDialog.setTitle(R.string.message_db_locked);
			final EditText dbPasswordInput = new EditText(context);
			dbPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

			dbPasswordDialog.setView(dbPasswordInput);
			dbPasswordDialog.setMessage(context.getResources().getString(
					R.string.message_db_password));

			dbPasswordDialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							setPassword(dbPasswordInput.getText().toString());
							if(okRun != null) {
								okRun.run();
							}
						}
					});
			dbPasswordDialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(cancelRun != null) {
								cancelRun.run();
							}
						}
					});
			dbPasswordDialog.show();
		} else {
			if(okRun != null) {
				okRun.run();
			}
		}*/
	}

	public void changeEncryption(String oldPassword, String newPassword) {
		sync();
		close();
		try {
			if (newPassword != null && !newPassword.equals("")) {
				if (oldPassword != null && !oldPassword.equals("")) {
					org.h2.tools.ChangeFileEncryption.execute(DB_PATH, DB_NAME,
							"AES", oldPassword.toCharArray(),
							newPassword.toCharArray(), true);
				} else {
					org.h2.tools.ChangeFileEncryption.execute(DB_PATH, DB_NAME,
							"AES", null, newPassword.toCharArray(), true);
				}
				this.password = newPassword;

			} else {
				if (oldPassword != null && !oldPassword.equals("")) {
					org.h2.tools.ChangeFileEncryption.execute(DB_PATH, DB_NAME,
							"AES", oldPassword.toCharArray(), null, true);
				} else {
					org.h2.tools.ChangeFileEncryption.execute(DB_PATH, DB_NAME,
							"AES", null, null, true);
				}
				this.password = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		checkCanOpen();
	}


	public void sync() {
		//Log.d(this.getClass().getName(), "synching db ...");
		//exec("CHECKPOINT SYNC");
		//Log.d(this.getClass().getName(), "... synched");
	}

	public void syncDb() {
		Log.d(this.getClass().getName(), "synching db ...");
		exec("CHECKPOINT SYNC");
		Log.d(this.getClass().getName(), "... synched");
	}

	private boolean databaseExists() {
		File db = null;
		String dbFile = DB_PATH + "/" + DB_FILE_NAME;
		db = new File(dbFile);
		if (db.exists()) {
			return true;
		}
		return false;
	}

	private boolean createDataBase() {
		InputStream is = null;
		OutputStream os = null;
		try {
			try {
				// Look for a database to import in the public directory: useful for debugging 
				is = new FileInputStream(FileSystemUtilities.getOpentenureFolder() + File.separator + DB_FILE_NAME);
			} catch (Exception e) {
				// Otherwise, use the one provided with the package 
				is = context.getAssets().open(DB_FILE_NAME);
			}
			String outFileName = DB_PATH + "/" + DB_FILE_NAME;

			os = new FileOutputStream(outFileName);

			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}

			os.flush();
			os.close();
			is.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}

	public Connection getConnection() {
		try {
			return DriverManager.getConnection(getUrl());
		} catch (Exception e) {
			Log.d(this.getClass().getName(), "getConnection first try exception");
			Toast.makeText(context, "Failed to get connection: " + e.getMessage(), Toast.LENGTH_LONG);
			OpenTenureApplication.getInstance().exportLog();
			e.printStackTrace();
		}
		return null;
	}

	public void exec(String command) {
		Connection localConnection = null;
		Statement statement = null;
		try {
			localConnection = getConnection();
			statement = localConnection.createStatement();
			statement.execute(command);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public boolean executeScript(String script) {
		Connection localConnection = null;
		ResultSet rs = null;

		try {
			localConnection = getConnection();
			Log.d(this.getClass().getName(), "Executing script <" + script
					+ ">");
			InputStream scriptStream = context.getAssets().open(script);

			rs = RunScript.execute(localConnection, new InputStreamReader(
					scriptStream));
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return true;
	}

	public void exportDB() {
		String exportPath = null;
		try{
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
			exportPath = FileSystemUtilities.getOpentenureFolder() + File.separator + dateFormat.format(new Date()) + "-db-backup.zip";
		}catch(Exception e){
		}
		Log.d(this.getClass().getName(), "Exporting OpenTenure database to a public folder in <" + exportPath
				+ ">");
		exec("BACKUP TO '" + exportPath + "'");
		Log.d(this.getClass().getName(), "Export complete");
	}

	public List<String> getUpgradePath() {
		Connection localConnection = null;
		ResultSet rs = null;
		List<String> upgradePath = new ArrayList<String>();

		try {

			localConnection = getConnection();
			InputStream scriptStream = context.getAssets().open(
					"upgradepath.sql");

			rs = RunScript.execute(localConnection, new InputStreamReader(
					scriptStream));
			while (rs != null && rs.next()) {
				upgradePath.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return upgradePath;
	}

}
