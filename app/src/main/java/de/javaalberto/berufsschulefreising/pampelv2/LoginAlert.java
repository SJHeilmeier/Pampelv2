package de.javaalberto.berufsschulefreising.pampelv2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class LoginAlert extends DialogFragment {

    public interface LoginAlertListener {
        void onLoginListenerClicked(DialogFragment dialog);
        void onLoginRegisterListenerClicked(DialogFragment dialog);
    }

    LoginAlertListener m_Listener;

    @Override
    public void onAttach(Context  context) {
        super.onAttach(context);
        try {m_Listener = (LoginAlertListener) context; }
        catch (ClassCastException e ) {
            e.printStackTrace();
        }
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstance) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        AlertDialog.Builder l_Builder = new AlertDialog.Builder(getActivity()).setView(inflater.inflate(R.layout.dialog_login, null));
        l_Builder.setPositiveButton(R.string.login_title,null);
        l_Builder.setNeutralButton(R.string.regist, null);


        AlertDialog l_Logdlg = l_Builder.create();
        DialogFragment l_Frag = this;
        l_Logdlg.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) l_Logdlg).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        m_Listener.onLoginListenerClicked(l_Frag);
                    }
                });
                Button button2 = ((AlertDialog) l_Logdlg).getButton(AlertDialog.BUTTON_NEUTRAL);
                button2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        m_Listener.onLoginRegisterListenerClicked(l_Frag);
                    }
                });
            }
        });

        return l_Logdlg;
    }
}