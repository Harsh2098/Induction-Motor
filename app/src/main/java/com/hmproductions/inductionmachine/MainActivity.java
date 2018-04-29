package com.hmproductions.inductionmachine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.config_radioGroup)
    RadioGroup configurationRadioGroup;

    @BindView(R.id.ratedVoltage_editText)
    EditText ratedVoltageEditText;

    @BindView(R.id.ratedCurrent_editText)
    EditText ratedCurrentEditText;

    @BindView(R.id.ratedSpeed_editText)
    EditText ratedSpeedEditText;

    @BindView(R.id.poles_editText)
    EditText polesEditText;

    @BindView(R.id.slip_editText)
    EditText slipEditText;

    @BindView(R.id.statorResistance_editText)
    EditText statorResistanceEditText;

    @BindView(R.id.noLoadInput_editText)
    EditText noLoadInputEditText;

    @BindView(R.id.blockedInput_editText)
    EditText blockedInputEditText;

    @BindView(R.id.noLoadCurrent_editText)
    EditText noLoadCurrentEditText;

    @BindView(R.id.blockedVoltage_editText)
    EditText blockedVoltageEditText;

    // Binding text views
    @BindView(R.id.shaftTorque_textView)
    TextView shaftTorqueTextView;

    @BindView(R.id.rotorResistance_textView)
    TextView rotorResistanceTextView;

    @BindView(R.id.rotorReactance_textView)
    TextView rotorReactanceTextView;

    @BindView(R.id.efficiency_textView)
    TextView efficiencyTextView;

    @BindView(R.id.magnetizingReactance_textView)
    TextView magnetizingReactance;

    double blockedCurrent, noloadCurrent, slip, statorResistance;
    double blockedVoltage, noloadVoltage, poles;
    double blockedInput, noLoadInput;

    // Computed parameters
    double Znl, Rnl, Xnl, rotationalLoss, Zbr, Rbr, Xbr, x1, x2, r1, r2, Xm, X2, wS;
    double Ve, Re, Xe, I2, Pm, Tshaft, Pshaft, totalLoss, efficiency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.calculate_action) {

            if (configurationRadioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Select Delta/Star connection", Toast.LENGTH_SHORT).show();
                return false;
            }

            calculateParameters();

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } else if (item.getItemId() == R.id.about_action) {
            startActivity(new Intent(this, AboutActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    void calculateParameters() {

        statorResistance = Double.parseDouble(statorResistanceEditText.getText().toString()) * 1.2;
        poles = Double.parseDouble(polesEditText.getText().toString());

        blockedInput = Double.parseDouble(blockedInputEditText.getText().toString());
        noLoadInput = Double.parseDouble(noLoadInputEditText.getText().toString());
        slip = Double.parseDouble(slipEditText.getText().toString());

        blockedCurrent = Double.parseDouble(ratedCurrentEditText.getText().toString());
        noloadVoltage = Double.parseDouble(ratedVoltageEditText.getText().toString());

        noloadCurrent = Double.parseDouble(noLoadCurrentEditText.getText().toString());
        blockedVoltage = Double.parseDouble(blockedVoltageEditText.getText().toString());

        if (configurationRadioGroup.getCheckedRadioButtonId() == R.id.delta_radioButton) {
            blockedCurrent /= Math.sqrt(3);
            noloadCurrent /= Math.sqrt(3);
        } else {
            blockedVoltage /= Math.sqrt(3);
            noloadVoltage /= Math.sqrt(3);
        }

        // Taking frequency as 50Hz
        wS = 200 / poles;

        Znl = noloadVoltage / noloadCurrent;
        Rnl = noLoadInput / (3 * Math.pow(noloadCurrent, 2));
        Xnl = Math.sqrt(Math.pow(Znl, 2) - Math.pow(Rnl, 2));

        rotationalLoss = noLoadInput  - 3 * Math.pow(noloadCurrent, 2) * statorResistance;

        Zbr = blockedVoltage / blockedCurrent;
        Rbr = blockedInput / (3 * Math.pow(blockedCurrent, 2));
        Xbr = Math.sqrt(Math.pow(Zbr, 2) - Math.pow(Rbr, 2));

        x1 = x2 = Xbr / 2;
        Xm = Xnl - x1;
        X2 = Xm + x2;

        if (Rbr < statorResistance * 1.2) {
            Toast.makeText(this, "Infeasible data as Rbr < 1.2 x r1", Toast.LENGTH_SHORT).show();
            return;
        }

        r1 = statorResistance;
        r2 = (Rbr - r1) * Math.pow(X2 / Xm, 2);

        String temp = ": " + String.format(Locale.ENGLISH, "%.3f", r2) + " ohm";
        rotorResistanceTextView.setText(temp);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", Xm) + " ohm";
        magnetizingReactance.setText(temp);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", x2) + " ohm";
        rotorReactanceTextView.setText(temp);

        // Calculating efficiency
        Ve = (noloadVoltage * Xm) / (Xm + x1);
        Re = (r1 * Xm) / (Xm + x1);
        Xe = (x1 * Xm) / (Xm + x1);

        I2 = Ve / (Math.sqrt(Math.pow(Re + r2/slip, 2) + Math.pow(Xe + x2, 2)));
        Pm = 3 * Math.pow(I2, 2) * r2 * (1 - slip) / slip;

        Pshaft = (Pm - rotationalLoss ) / 1000;
        Tshaft = Pshaft / ((1 - slip) * wS);

        totalLoss = rotationalLoss + 3 * Math.pow(I2, 2) * (r1 + r2);

        efficiency = Pm / (Pm + totalLoss);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", efficiency * 100) + " %";
        efficiencyTextView.setText(temp);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", Pshaft) + " Nm";
        shaftTorqueTextView.setText(temp);
    }
}
