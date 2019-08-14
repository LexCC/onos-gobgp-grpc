/*
 * Copyright 2019-present Open Networking Foundation
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
package org.foo.app;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import io.opencensus.tags.Tags;

/**
 * Sample Apache Karaf CLI command
 */
@Service
@Command(scope = "onos", name = "gobgp",
         description = "Sample Apache Karaf CLI command")
public class AppCommand extends AbstractShellCommand {
    @Override
    protected void doExecute() {
        GobgpClient client = new GobgpClient();
        String response = client.setup("gobgp");
        try {
            client.shutdown();
        } catch (InterruptedException e) {
            print("channel closed error：err={%s}",e.getMessage());
        }
        print("UUID: %s", response);
    }

}
