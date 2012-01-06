package com.cloudbees.sdk.commands;

import com.cloudbees.api.DatabaseCreateResponse;
import com.cloudbees.api.StaxClient;
import com.cloudbees.sdk.utils.Helper;

/**
 * @Author: Fabian Donze
 */
public class DatabaseCreate extends Command {
    private String databaseName;
    private String username;
    private String password;
    private String account;

    public DatabaseCreate() {
        setArgumentExpected(1);
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    protected String getUsageMessage() {
        return "DATABASE_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        // add the Options
        addOption( "u", "username", true, "Database username (must be unique)" );
        addOption( "p", "password", true, "Database password" );
        addOption( "a", "account", true, "Account Name" );

        return true;
    }

    /**
     * This method is call after postParseCommandLine.
     * This is the place to validate all inputs
     *
     * @return true if successful, false otherwise
     */
    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setDatabaseName(getParameters().get(0));
            return true;
        }
        return false;
    }

    @Override
    protected boolean execute() throws Exception {
        if (databaseName == null) databaseName = Helper.promptFor("Database name: ", true);
        if (username == null) username = Helper.promptFor("Database Username (must be unique): ", true);
        if (password == null) password = Helper.promptFor("Database Password: ", true);

        if (account == null)
            account = getConfigProperties().getProperty("bees.project.app.domain");
        if (account == null) account = Helper.promptFor("Account name: ", true);

        StaxClient client = getStaxClient();
        DatabaseCreateResponse res = client.databaseCreate(account, databaseName, username, password);

        if (isTextOutput()) {
            System.out.println("database created: " + res.getDatabaseId());
        } else
            printOutput(res, DatabaseCreateResponse.class);


        return true;
    }


}
