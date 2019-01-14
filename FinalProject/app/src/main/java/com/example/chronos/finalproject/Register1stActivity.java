package com.example.chronos.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public class Register1stActivity extends AppCompatActivity {

    Spinner daySpinner, monthSpinner, yearSpinner;
    EditText nameEditText, lastNameEditText, mLastNameEditText, emailEditText, passwordEditText;
    TextView validNameTextView, validLastNameTextView, validmLastNameTextView, validEmailTextView, validPasswordTextView, validDateTextView;

    public int isValidName(EditText editText) {
        String text = editText.getText().toString();
        if (text.equals("")) {
            return 1; // Empty string
        }
        if (text.contains(" ") && (text.startsWith(" ") || text.endsWith(" "))) {
            return 2; // Starts or ends with whitespace
        }
        String noSpacesText = text.trim().replaceAll(" +", " ");
        if (!text.equals(noSpacesText)) {
            return 3; // Many whitespaces between words
        }
        return 0; // valid name
    }

    public boolean isValidEmail() {
        return (!TextUtils.isEmpty(emailEditText.getText()) && Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches());
    }

    public boolean isValidPassword() {
        return (passwordEditText.getText().length() >= 8);
    }

    public boolean isValidDate() {
        String day = daySpinner.getSelectedItem().toString();
        String month = monthSpinner.getSelectedItem().toString();
        String year = yearSpinner.getSelectedItem().toString();
        ArrayList<String> months31 = new ArrayList<>(asList("1", "3", "5", "7", "8", "10", "12"));

        if (day.equals("31") && !months31.contains(month)) {
            return false;
        }
        if (day.equals("30") && month.equals("2")) {
            return false;
        }
        if (day.equals("29") && month.equals("2")) {
            if (Integer.valueOf(year) % 4 != 0) {
                return false;
            } else if (Integer.valueOf(year) % 100 == 0) {
                if (Integer.valueOf(year) % 400 != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public void continueRegister(View view) {
        if (isValidName(nameEditText) == 0 && isValidName(lastNameEditText) == 0 && isValidName(mLastNameEditText) == 0 && isValidEmail() && isValidPassword() && isValidDate()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios");
            final boolean[] userNameExists = {false};
            final boolean[] userEmailExists = {false};
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Map<String, Object> userValues = (Map<String, Object>) data.getValue();
                            String userName = userValues.get("Nombre").toString();
                            String userLastName = userValues.get("ApellidoPat").toString();
                            String usermLastName = userValues.get("ApellidoMat").toString();
                            String userEmail = userValues.get("Correo").toString();
                            if (userName.equals(nameEditText.getText().toString()) && userLastName.equals(lastNameEditText.getText().toString()) && usermLastName.equals(mLastNameEditText.getText().toString())) {
                                userNameExists[0] = true;
                            } else if (userEmail.equals(emailEditText.getText().toString())) {
                                userEmailExists[0] = true;
                            }
                        }
                        if (userNameExists[0]) {
                            new AlertDialog.Builder(Register1stActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle(getString(R.string.username_alredy_exists))
                                    .setMessage(getString(R.string.username_alredy_exists_detail))
                                    .setPositiveButton(getString(R.string.ok_option), null)
                                    .show();
                        }
                        else if (userEmailExists[0]) {
                            new AlertDialog.Builder(Register1stActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle(getString(R.string.email_alredy_exists))
                                    .setMessage(getString(R.string.email_alredy_exists_detail))
                                    .setPositiveButton(getString(R.string.ok_option), null)
                                    .show();
                        }
                    }
                    if (!userNameExists[0] && !userEmailExists[0]) {
                        Intent intent = new Intent(getApplicationContext(), Register2ndActivity.class);
                        intent.putExtra("name", nameEditText.getText().toString());
                        intent.putExtra("lastName", lastNameEditText.getText().toString());
                        intent.putExtra("mLastName", mLastNameEditText.getText().toString());
                        intent.putExtra("email", emailEditText.getText().toString());
                        intent.putExtra("password", passwordEditText.getText().toString());
                        intent.putExtra("birthDay", daySpinner.getSelectedItem().toString());
                        intent.putExtra("birthMonth", monthSpinner.getSelectedItem().toString());
                        intent.putExtra("birthYear", yearSpinner.getSelectedItem().toString());
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            passwordEditText.requestFocus();
            emailEditText.requestFocus();
            mLastNameEditText.requestFocus();
            lastNameEditText.requestFocus();
            nameEditText.requestFocus();
            getCurrentFocus().clearFocus();
            if (!isValidDate()) {
                validDateTextView.setText(getString(R.string.invalid_date));
            } else {
                validDateTextView.setText("");
            }
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.general_error))
                    .setMessage(getString(R.string.description_error))
                    .setPositiveButton(getString(R.string.ok_option), null)
                    .show();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1st);

        // Inicializar variables para comprobar mas adelante si los datos son correctos
        // Spinner
        daySpinner = findViewById(R.id.daySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);

        // EditText
        nameEditText = findViewById(R.id.nameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        mLastNameEditText = findViewById(R.id.mLastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // TextView
        validNameTextView = findViewById(R.id.validNameTextView);
        validLastNameTextView = findViewById(R.id.validLastNameTextView);
        validmLastNameTextView = findViewById(R.id.validmLastNameTextView);
        validEmailTextView = findViewById(R.id.validEmailTextView);
        validPasswordTextView = findViewById(R.id.validPasswordTextView);
        validDateTextView = findViewById(R.id.validDateTextView);

        // Inicializar listas de numeros para poder mostrarlos en los spinner
        Integer[] dayNumbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        Integer[] monthNumbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        Integer[] yearNumbers = new Integer[39];
        for (int i = 0, year = 1980; i < yearNumbers.length; i++, year++) {
            yearNumbers[i] = year;
        }

        // Unir las listas de numeros a los spinner
        daySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayNumbers));
        monthSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthNumbers));
        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearNumbers));

        // FocusListener para validar campos
        // nameEditText
        nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    switch (isValidName(nameEditText)) {
                        case 1:
                            validNameTextView.setText(R.string.empty_error);
                            break;
                        case 2:
                            validNameTextView.setText(R.string.whitespace_eror);
                            break;
                        case 3:
                            validNameTextView.setText(R.string.many_whitespace_error);
                            break;
                        default:
                            validNameTextView.setText("");
                    }
                }
            }
        });
        // lastNameEditText
        lastNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    switch (isValidName(lastNameEditText)) {
                        case 1:
                            validLastNameTextView.setText(R.string.empty_error);
                            break;
                        case 2:
                            validLastNameTextView.setText(R.string.whitespace_eror);
                            break;
                        case 3:
                            validLastNameTextView.setText(R.string.many_whitespace_error);
                            break;
                        default:
                            validLastNameTextView.setText("");
                    }
                }
            }
        });
        // mLastNameEditText
        mLastNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    switch (isValidName(mLastNameEditText)) {
                        case 1:
                            validmLastNameTextView.setText(R.string.empty_error);
                            break;
                        case 2:
                            validmLastNameTextView.setText(R.string.whitespace_eror);
                            break;
                        case 3:
                            validmLastNameTextView.setText(R.string.many_whitespace_error);
                            break;
                        default:
                            validmLastNameTextView.setText("");
                    }
                }
            }
        });
        // emailEditText
        emailEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!isValidEmail()) {
                        validEmailTextView.setText(getString(R.string.invalid_email));
                    } else {
                        validEmailTextView.setText("");
                    }
                }
            }
        });
        // passwordEditText. validar tambien al presionar cada tecla
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!isValidPassword()) {
                        validPasswordTextView.setText(getString(R.string.invalid_password));
                    } else {
                        validPasswordTextView.setText("");
                    }
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isValidPassword()) {
                    validPasswordTextView.setText(getString(R.string.invalid_password));
                } else {
                    validPasswordTextView.setText("");
                }
            }
        });
        // Spinners (solo OnItemSelected)
        Spinner.OnItemSelectedListener spinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isValidDate()) {
                    validDateTextView.setText(getString(R.string.invalid_date));
                } else {
                    validDateTextView.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        daySpinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        monthSpinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);
        yearSpinner.setOnItemSelectedListener(spinnerOnItemSelectedListener);

        View.OnTouchListener keyboardOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(validNameTextView);
                v.performClick();
                return true;
            }
        };
        daySpinner.setOnTouchListener(keyboardOnTouchListener);
        monthSpinner.setOnTouchListener(keyboardOnTouchListener);
        yearSpinner.setOnTouchListener(keyboardOnTouchListener);
    }
}