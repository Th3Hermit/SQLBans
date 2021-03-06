/*
 * SQLBans
 * Copyright 2012 Matt Baxter
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
package org.kitteh.sqlbans.commands;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kitteh.sqlbans.*;
import org.kitteh.sqlbans.SQLBans.Messages;

public class UnbanCommand implements CommandExecutor {

    public SQLBans plugin;

    public UnbanCommand(SQLBans plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        final String targetName = args[0];

        final String unbanMessage = Messages.getIngameUnbanned(targetName, sender.getName(), false);
        final String unbanAdminMessage = Messages.getIngameUnbanned(targetName, sender.getName(), true);
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            if ((player != null) && player.isOnline()) {
                if (Perm.MESSAGE_UNBAN_ADMIN.has(player)) {
                    player.sendMessage(unbanAdminMessage);
                } else if (Perm.MESSAGE_UNBAN_NORMAL.has(player)) {
                    player.sendMessage(unbanMessage);
                }
            }
        }
        this.plugin.getServer().getConsoleSender().sendMessage(unbanAdminMessage);

        final String username = targetName;
        this.plugin.removeCachedBan(username);
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
            public void run() {
                try {
                    SQLHandler.unban(username);
                } catch (final Exception e) {
                    UnbanCommand.this.plugin.getLogger().log(Level.SEVERE, "Could not unban " + username, e);
                    Util.queueMessage(UnbanCommand.this.plugin, Perm.MESSAGE_UNBAN_ADMIN.toString(), ChatColor.RED + "[SQLBans] Failed to unban " + username);
                }
            }
        });
        return true;
    }
}
