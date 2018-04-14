package com.hmproductions.inductionmachine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

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

    @BindView(R.id.ratedPower_editText)
    EditText ratedPowerEditText;

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
    @BindView(R.id.statorCopperLoss_textView)
    TextView statorCopperLossTextView;

    @BindView(R.id.rotorResistance_textView)
    TextView rotorResistanceTextView;

    @BindView(R.id.rotorReactance_textView)
    TextView rotorReactanceTextView;

    @BindView(R.id.efficiency_textView)
    TextView efficiencyTextView;

    double blockedCurrent, noloadCurrent, slip, statorResistance;

    double blockedVoltage, noloadVoltage, ratedPower;
    double blockedInput, noLoadInput;

    double Xm, Rw, Zsh, Rt1, Xt1, Zt1, R2_, Rl, I1;

    // Computed parameters
    double statorCopperLoss;

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
            calculateParameters();
        }

        return super.onOptionsItemSelected(item);
    }

    void calculateParameters() {
        statorResistance = Double.parseDouble(statorResistanceEditText.getText().toString());
        ratedPower = Double.parseDouble(ratedPowerEditText.getText().toString());

        blockedInput = Double.parseDouble(blockedInputEditText.getText().toString());
        noLoadInput = Double.parseDouble(noLoadInputEditText.getText().toString());
        slip = Double.parseDouble(slipEditText.getText().toString());

        blockedCurrent = Double.parseDouble(ratedCurrentEditText.getText().toString());
        noloadVoltage = Double.parseDouble(ratedVoltageEditText.getText().toString());

        noloadCurrent = Double.parseDouble(noLoadCurrentEditText.getText().toString());
        blockedVoltage = Double.parseDouble(blockedVoltageEditText.getText().toString());

        // Calculating parameters

        if (configurationRadioGroup.getCheckedRadioButtonId() == R.id.delta_radioButton) {
            blockedCurrent /= Math.sqrt(3);
            noloadCurrent /= Math.sqrt(3);
        } else {
            blockedVoltage /= Math.sqrt(3);
            noloadVoltage /= Math.sqrt(3);
        }

        statorCopperLoss = 3 * Math.pow(noloadCurrent,2) * statorResistance;

        noLoadInput -= statorCopperLoss;

        Rw = (3 * Math.pow(noloadVoltage, 2)) / noLoadInput;
        Zsh = noloadVoltage / noloadCurrent;
        Xm = Math.sqrt(Math.pow(Zsh, 2) - Math.pow(Rw, 2));

        Rt1 = blockedInput / (3 * Math.pow(blockedCurrent, 2));
        Zt1 = blockedVoltage / blockedCurrent;
        Xt1 = Math.sqrt(Math.pow(Zt1, 2) - Math.pow(Rt1, 2));

        R2_ = Rt1 - statorResistance;
        Rl = (R2_ / slip) - R2_;

        String temp = ": " + String.format(Locale.ENGLISH, "%.3f",statorCopperLoss) + " W";
        statorCopperLossTextView.setText(temp);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", Rt1) + " ohm";
        rotorResistanceTextView.setText(temp);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", Xt1) + " ohm";
        rotorReactanceTextView.setText(temp);

        I1 = noloadVoltage/(Rl + Rt1) + noloadVoltage/Rw;

        Log.v(":::", "output power" + 3 * noloadVoltage * I1 * 0.001);

        temp = ": " + String.format(Locale.ENGLISH, "%.3f", (ratedPower / (3 * noloadVoltage * I1 * 0.001)) * 100) + " %";
        efficiencyTextView.setText(temp);
    }
}
