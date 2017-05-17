/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.loginapi.login.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.instructure.loginapi.login.R;

public class AuthenticationDialog extends DialogFragment {

    public interface OnAuthenticationSet {
        void onRetrieveCredentials(String username, String password);
    }

    private OnAuthenticationSet mCallback;
    private EditText mUsername, mPassword;

    public static AuthenticationDialog get(Fragment...target) {
        AuthenticationDialog dialog = new AuthenticationDialog();
        if(target != null && target.length > 0) {
            dialog.setTargetFragment(target[0], 1);
        }
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnAuthenticationSet) {
            mCallback = (OnAuthenticationSet) context;
        } else {
            throw new IllegalStateException("Context required to implement AuthenticationDialog.OnAuthenticationSet callback");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.authenticationRequired);
        View root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_auth, null);
        mUsername = (EditText) root.findViewById(R.id.username);
        mPassword = (EditText) root.findViewById(R.id.password);
        builder.setView(root);

        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mCallback != null) {
                    mCallback.onRetrieveCredentials(mUsername.getText().toString(), mPassword.getText().toString());
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }
}
