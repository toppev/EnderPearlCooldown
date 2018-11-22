package com.gmail.thetoppe5.enderpearlcooldown;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EnderPearlCooldown extends JavaPlugin implements CommandExecutor{


	private static final String WEBSITE_URL = "https://api.spigotmc.org/legacy/update.php?resource=43307/";

	private String pearlName;
	private double cooldown;
	private int interval;
	private DecimalFormat format;
	private boolean asyncUpdate, xpBar, updateAvailable;

	@Override
	public void onEnable() {
		loadConfig();
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

		new BukkitRunnable() {

			@Override
			public void run() {
				checkUpdate();
			}
		}.runTaskAsynchronously(this);
	}


	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		long st = System.currentTimeMillis();
		reloadConfig();
		loadConfig();
		long et = System.currentTimeMillis();
		sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "EnderPearlCooldown was reloaded in " + (et-st) + "ms.");
		sender.sendMessage(ChatColor.DARK_AQUA + "Checking for updates...");

		new BukkitRunnable() {

			@Override
			public void run() {
				checkUpdate();
				if(sender != null) {
					if(updateAvailable) {
						sender.sendMessage(ChatColor.GRAY + "There is a new update available for " + getDescription().getName());
						sender.sendMessage(ChatColor.GRAY + "Link: https://www.spigotmc.org/resources/43307/updates");
					}
					else {
						sender.sendMessage(ChatColor.GRAY + "No updates found for " + getDescription().getName());
					}
				}
			}
		}.runTaskAsynchronously(this);
		return true;
	}


	private void loadConfig() {
		saveDefaultConfig();
		format = new DecimalFormat(getConfig().getString("format"));
		pearlName = getConfig().getString("pearl-name");
		cooldown = getConfig().getDouble("cooldown");
		interval = getConfig().getInt("interval");
		xpBar = getConfig().getBoolean("xp-bar");
		asyncUpdate = getConfig().getBoolean("async-update");
	}


	private void checkUpdate() {
		try {
			URLConnection conn = new URL(WEBSITE_URL).openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.connect();
			BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder localStringBuilder = new StringBuilder();
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				localStringBuilder.append(str1);
			}
			String str2 = localStringBuilder.toString();
			if(!this.getDescription().getVersion().equals(str2)) {
				updateAvailable = true;
			}
			Bukkit.getLogger().info("There is a new update available for " + getDescription().getName());
			Bukkit.getLogger().info("Link: https://www.spigotmc.org/resources/43307/updates");
		}catch(IOException e) {
			Bukkit.getLogger().warning("Failed to check updates for " + getDescription().getName() + " " + getDescription().getVersion());
		}
	}



	public DecimalFormat getFormat() {
		return format;
	}

	public void setFormat(DecimalFormat format) {
		this.format = format;
	}

	public double getCooldown() {
		return cooldown;
	}

	public void setCooldown(double cooldown) {
		this.cooldown = cooldown;
	}

	public String getPearlName() {
		return pearlName;
	}

	public void setPearlName(String pearlName) {
		this.pearlName = pearlName;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int task) {
		this.interval = task;
	}

	public void setAsyncUpdate(boolean asyncUpdate) {
		this.asyncUpdate = asyncUpdate;
	}

	public boolean isAsyncUpdate() {
		return asyncUpdate;
	}

	public boolean isXpBar() {
		return xpBar;
	}

	public void setXpBar(boolean xpBar) {
		this.xpBar = xpBar;
	}

	public boolean isUpdateAvailable() {
		return updateAvailable;
	}

	public void setUpdateAvailable(boolean updateAvailable) {
		this.updateAvailable = updateAvailable;
	}
}
