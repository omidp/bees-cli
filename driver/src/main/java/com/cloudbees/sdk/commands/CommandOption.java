/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.sdk.commands;

import org.apache.commons.cli.Option;

/**
 * @author Fabian Donze
 */
public class CommandOption {
    private Option option;
    private boolean hidden;

    public CommandOption(Option option, boolean hidden) {
        this.option = option;
        this.hidden = hidden;
    }

    public CommandOption(String opt, String longOpt, boolean hasArg, String description, boolean hidden) {
        if (longOpt != null)
            option = new Option(opt, longOpt, hasArg, description);
        else
            option = new Option(opt, hasArg, description);
        this.hidden = hidden;
    }

    public Option getOption() {
        return option;
    }

    public boolean isHidden() {
        return hidden;
    }

}
