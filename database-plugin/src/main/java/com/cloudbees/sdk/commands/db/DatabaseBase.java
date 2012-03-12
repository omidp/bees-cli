package com.cloudbees.sdk.commands.db;

import com.cloudbees.sdk.commands.Command;
import com.cloudbees.sdk.utils.Helper;

import java.io.IOException;

/**
 * @Author: Fabian Donze
 */
public abstract class DatabaseBase extends Command {
    private String databaseName;

    public DatabaseBase() {
        setArgumentExpected(1);
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    protected String getDatabaseName() throws IOException {
        if (databaseName == null) databaseName = Helper.promptFor("Database name: ", true);
        return databaseName;
    }

    @Override
    protected String getUsageMessage() {
        return "DATABASE_NAME";
    }

    @Override
    protected boolean preParseCommandLine() {
        return true;
    }

    @Override
    protected boolean postParseCheck() {
        if (super.postParseCheck()) {
            setDatabaseName(getParameters().get(0));
            return true;
        }
        return false;
    }

}
