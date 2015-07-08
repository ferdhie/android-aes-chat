package com.ferdianto.android.encryptedchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * Login fragment
 */
public class LoginFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private LoginFragmentListener mListener;

    EditText userText;
    EditText pwText;
    EditText serverText;
    EditText secretText;

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        userText = (EditText)v.findViewById(R.id.username);
        pwText = (EditText)v.findViewById(R.id.password);
        serverText = (EditText)v.findViewById(R.id.server);
        secretText = (EditText)v.findViewById(R.id.secret);

        v.findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed(v);
            }
        });

        return v;
    }

    private void msgbox(String title, String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setTitle(title);
        builder1.setMessage(msg);
        builder1.setCancelable(true);
        builder1.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    ProgressDialog dialog;

    public void onButtonPressed(View v) {
        String username = userText.getText().toString().trim();
        String password = pwText.getText().toString().trim();
        String server = serverText.getText().toString().trim();
        String secret = secretText.getText().toString().trim();

        if (username.length() == 0 || password.length() == 0 || server.length()==0 || secret.length()==0) {
            msgbox("Error", "Username, password, server dan secret tidak boleh kosong");
            return;
        }

        new AsyncTask<String,Void,Object>() {
            @Override
            protected Object doInBackground(String... params) {
                String username = params[0];
                String password = params[1];
                String server = params[2];
                String secret = params[3];

                try {
                    mListener.login(username,password,server,secret);
                    return true;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(getActivity(), "Connectin", "Membuka koneksi ke server", true, false);
            }

            @Override
            protected void onPostExecute(Object result) {
                dialog.dismiss();
                if (result instanceof Exception) {
                    Exception ex = (Exception) result;
                    msgbox("Error", ex.getMessage());
                } else {
                    mListener.loginSuccess();
                }
            }
        }.execute(username, password, server, secret);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LoginFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface LoginFragmentListener {
        public void login(String username, String password, String server, String secret) throws Exception;
        public void loginSuccess();
    }

}
