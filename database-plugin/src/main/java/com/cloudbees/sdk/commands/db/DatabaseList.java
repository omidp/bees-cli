package com.cloudbees.sdk.commands.db;


import com.cloudbees.api.BeesClient;
import com.cloudbees.api.DatabaseListResponse;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandGroup;
import com.cloudbees.sdk.commands.Command;

/**
 * @author Fabian Donze
 */
@CommandGroup("Database")
@CLICommand("db:list")
public class DatabaseList extends Command {
    private String account;

    public DatabaseList() {
    }

    @Override
    protected boolean preParseCommandLine() {
        addOption( "a", "account", true, "Account Name" );
        return true;
    }

    @Override
    protected boolean postParseCommandLine() {
        return true;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    protected boolean execute() throws Exception {
        BeesClient client = getBeesClient();
        DatabaseListResponse res = client.databaseList(getAccount());

        if (isTextOutput()) {
            System.out.println("Databases:");
            for (com.cloudbees.api.DatabaseInfo applicationInfo: res.getDatabases()) {
                System.out.println(applicationInfo.getOwner() + "/" + applicationInfo.getName());
            }
        } else
            printOutput(res, DatabaseListResponse.class, DatabaseInfo.class);

        return true;
    }


}
