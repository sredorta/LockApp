package com.locker.lockapp.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.locker.lockapp.R;
import com.locker.lockapp.dao.QueryPreferences;
import com.locker.lockapp.toolbox.Logs;

import java.util.ArrayList;
import java.util.List;

import static com.locker.lockapp.authentication.AccountGeneral.*;


/**
 * Created by sredorta on 1/24/2017.
 */
public class AccountListFragment extends Fragment {
        private AccountGeneral myAccountSettings;
        private AccountListAdapter mAdapter;
        private RecyclerView mAccountsRecycleView;
        private List<Account> mAccounts = new ArrayList<>();
        // Constructor
        public static AccountListFragment newInstance() {
            return new AccountListFragment();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //Get account details from Singleton either from intent of from preferences comming from AuthenticatorActivity
            myAccountSettings = AccountGeneral.getInstance();

        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_account_list, container, false);
            mAccountsRecycleView = (RecyclerView) v.findViewById(R.id.account_list_recycle_view);
            mAccountsRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

            updateUI();
            return v;
        }
        private void updateUI() {
            AccountManager mAccountManager;
            mAccountManager = AccountManager.get(getActivity().getApplicationContext());
           for (Account account : myAccountSettings.getAllAccounts()) {
                mAccounts.add(account);
            }
            //Do the swap to make sure that we start with last login as first element
            Account myAccount;
            for (Account account : myAccountSettings.getAllAccounts()) {
                if (myAccountSettings.getAccountName().equals(mAccountManager.getUserData(account, PARAM_USER_ACCOUNT))) {
                    int index = mAccounts.indexOf(account);
                    if (index != 0) {
                        myAccount = mAccounts.get(0);
                        mAccounts.set(0, account);
                        mAccounts.set(index, myAccount);
                    }
                    break;
                }
            }

            mAdapter = new AccountListAdapter(mAccounts);
            mAccountsRecycleView.setAdapter(mAdapter);

        }



        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onStop() {
            super.onStop();
            Logs.i("onStop");

        }

        @Override
        public void onResume() {
            super.onResume();
            Logs.i("onResume");
        }



        @Override
        public void onPause() {
            super.onPause();
            Logs.i("onPause");
        }

    private class AccountListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Account mAccount;
        private TextView mUserFullNameTextView;
        private TextView mAccountNameTextView;
        private ImageView mAvatarImageView;
        public ImageView buttonViewOption;
        public ImageView buttonActiveView;

        private AccountListHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mUserFullNameTextView = (TextView) itemView.findViewById(R.id.fragment_account_textView_user);
            mAccountNameTextView =  (TextView) itemView.findViewById(R.id.fragment_account_textView_account);
            mAvatarImageView = (ImageView) itemView.findViewById(R.id.fragment_account_imageView_avatar);
            buttonViewOption = (ImageView) itemView.findViewById(R.id.fragment_account_imageView_more);
            buttonActiveView = (ImageView) itemView.findViewById(R.id.fragment_account_imageView_active);

        }


        @Override
        public void onClick(View view) {
            AccountManager mAccountManager;
            mAccountManager = AccountManager.get(getActivity().getApplicationContext());
            Account myAccount;
            //Update the user name so that we check against new account
            /////myAccountSettings.setAccountName(mAccountManager.getUserData(mAccount, PARAM_USER_ACCOUNT));
            myAccountSettings.setDataFromAccount(mAccount);
            Toast.makeText(getActivity(),"account first name:" + myAccountSettings.getAccountFirstName(), Toast.LENGTH_LONG).show();
            ////////////////////////////////////////////// Missing !!!! We need to update all the User singleton at each swap !
            int index = mAccounts.indexOf(mAccount);
            mAdapter.notifyDataSetChanged();
            /*
            if (index != 0) {
                myAccount = mAccounts.get(0);
                mAccounts.set(0, mAccount);
                mAccounts.set(index,myAccount);
                mAdapter.notifyItemChanged(0);
                mAdapter.notifyItemChanged(index);
                mAdapter.notifyItemMoved(index, 0);
            }
            */
        }

        private void deleteItem() {
            mAccounts.remove(getAdapterPosition());
            mAdapter.notifyItemRemoved(getAdapterPosition());
            mAdapter.notifyItemRangeChanged(getAdapterPosition(), mAccounts.size());
            //holder.itemView.setVisibility(View.GONE);
        }

        public void bindAccount(Account account, AccountListHolder holder ) {
            final AccountManager mAccountManager;
            mAccountManager = AccountManager.get(getActivity().getApplicationContext());

            String fullName = mAccountManager.getUserData(account, PARAM_USER_FIRST_NAME);
            fullName = fullName + " " + mAccountManager.getUserData(account, PARAM_USER_LAST_NAME);
            mAccount = account;
            mUserFullNameTextView.setText(fullName);
            mAccountNameTextView.setText(mAccountManager.getUserData(account, PARAM_USER_EMAIL));
            mAvatarImageView.setImageResource(R.drawable.user_default);
            //Define color for active or not active account (last log-in)
            if (myAccountSettings.getAccountName().equals(mAccountManager.getUserData(account, PARAM_USER_ACCOUNT))) {
                buttonActiveView.setImageResource(android.R.drawable.presence_online);
            } else {
                buttonActiveView.setImageResource(android.R.drawable.presence_invisible);
            }

            //Handle here the options menu of each account
            buttonViewOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(getContext(), buttonViewOption);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.options_menu_account_item);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.options_menu_account_item_edit:
                                    //TODO be able to edit the properties of the account
                                    Intent i = new Intent(getContext(),QueryPreferences.class);
                                    startActivity(i);
                                    //mAccountManager.editProperties();
                                    //handle menu1 click
                                    break;
                                case R.id.options_menu_account_item_remove:
                                    new AsyncTask<Void,Void,Void>() {
                                        Boolean isRemoved = false;
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            Account myAccount = myAccountSettings.getAccount();
                                            String accountName = myAccountSettings.getAccountName();
                                            isRemoved = sServerAuthenticate.userRemove(accountName,null);
                                            if (isRemoved)
                                                myAccountSettings.removeAccount(myAccount);

                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void s) {
                                            if (isRemoved) {
                                                deleteItem();
                                                Toast.makeText(getActivity().getApplicationContext(), "Account removed !", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(getActivity().getApplicationContext(), "Account not removed !", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }.execute();


                                    //handle menu2 click
                                    break;
                            }
                            return false;
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });


        }

    }

    private class AccountListAdapter extends RecyclerView.Adapter<AccountListHolder> {

        public AccountListAdapter(List<Account> accounts) {
            mAccounts = accounts;
        }

        @Override
        public AccountListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.recycleview_account_item, parent,false);
            return new AccountListHolder(view);
        }

        @Override
        public void onBindViewHolder(AccountListHolder holder, int position) {
            Account account = mAccounts.get(position);
            holder.bindAccount(account,holder);
        }

        @Override
        public int getItemCount() {
            return mAccounts.size();
        }

    }




}


