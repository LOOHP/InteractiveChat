package com.loohp.interactivechat.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.data.PlayerDataManager.PlayerData;

import net.md_5.bungee.api.ChatColor;

public class Database {
	
	private static final Map<String, String> COLUMNS = new HashMap<>();
	
	static {
		COLUMNS.put("UUID", "Text");
		COLUMNS.put("NAME", "Text");
		COLUMNS.put("DISABLED_MENTION", "Boolean");
		COLUMNS.put("INV_DISPLAY_LAYOUT", "int");
	}
	
	public boolean isMYSQL = false;
	
	private boolean bungee;
	
	private Connection connection;
	private File dataFolder;
	private String storageType;
	private String host;
	private String database;
	private String username;
	private String password;
	private String table = "InteractiveChat_USER_PERFERENCES";
	private int port;
    
    public Database(boolean bungee, File dataFolder, String storageType, String host, String database, String username, String password, int port) {
    	this.bungee = bungee;
    	this.dataFolder = dataFolder;
    	this.storageType = storageType;
    	this.host = host;
    	this.database = database;
    	this.username = username;
    	this.password = password;
    	this.port = port;
    }
    
    private void consoleMessage(String str) {
    	if (bungee) {
    		net.md_5.bungee.api.ProxyServer.getInstance().getConsole().sendMessage(new net.md_5.bungee.api.chat.TextComponent(str));
    	} else {
    		org.bukkit.Bukkit.getConsoleSender().sendMessage(str);
    	}
    }
	
	public void setup() {
		if (storageType.equalsIgnoreCase("MYSQL")) {
			isMYSQL = true;
		} else {
			isMYSQL = false;
		}
		synchronized (this) {
			if (isMYSQL) {
				mysqlSetup(true);
				createTable();
				checkColumns();
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				sqliteSetup(true);
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void open() {
		if (isMYSQL) {
			mysqlSetup(false);
		} else {
			sqliteSetup(false);
		}
	}
	
	private void mysqlSetup(boolean echo) {
        try {
			if (getConnection() != null && !getConnection().isClosed()) {
				return;
			}
			
			Class.forName("com.mysql.jdbc.Driver");
			setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
			
			if (echo) {
				consoleMessage(ChatColor.GREEN + "[InteractiveChat] MYSQL CONNECTED");
			}
		} catch (SQLException e) {
			consoleMessage(ChatColor.RED + "[InteractiveChat] MYSQL Failed to connect! (SQLException)");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			consoleMessage(ChatColor.RED + "[InteractiveChat] MYSQL Failed to connect! (ClassNotFoundException)");
			e.printStackTrace();
		}
	}
	
	private void sqliteSetup(boolean echo) {	   
		try {
			Class.forName("org.sqlite.JDBC");
	        connection = DriverManager.getConnection("jdbc:sqlite:plugins/" + dataFolder.getName() + "/database.db");
	        if (echo) {
	        	consoleMessage(ChatColor.GREEN + "[InteractiveChat] Opened Sqlite database successfully");
	        }

	        Statement stmt = connection.createStatement();
	        String sql = "CREATE TABLE IF NOT EXISTS " + table + " " +
	                      "(UUID TEXT PRIMARY KEY, " +
	                       "NAME TEXT NOT NULL, " + 
	                       "DISABLED_MENTION BOOLEAN);"; 
	        stmt.executeUpdate(sql);
	        
	        Statement statement = connection.createStatement();
	        String query = "PRAGMA table_info(" + table + ");";
	        ResultSet result = statement.executeQuery(query);
	        List<String> columns = new ArrayList<>();
	        while (result.next()) {
	        	columns.add(result.getString("name"));
	        }
	        for (Entry<String, String> entry : COLUMNS.entrySet()) {
        		String name = entry.getKey();
        		String type = entry.getValue();
        		if (!columns.contains(name)) {
        			PreparedStatement alter = getConnection().prepareStatement("ALTER TABLE " + table + " ADD " + name + " " + type);
        			alter.execute();
        		}
        	}
	        
	        stmt.close(); 
	    } catch (Exception e) {
	    	consoleMessage(ChatColor.RED + "[InteractiveChat] Unable to connect to sqlite database!!!");
	    	e.printStackTrace();
	    }
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
    public void createTable() {
    	synchronized (this) {
	    	open();
	        try {
	        	PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (UUID Text, NAME Text, DISABLED_MENTION Boolean, INV_DISPLAY_LAYOUT int)");
	
	            statement.execute();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public void checkColumns() {
    	synchronized (this) {
	    	open();
	        try {
	        	for (Entry<String, String> entry : COLUMNS.entrySet()) {
	        		String name = entry.getKey();
	        		String type = entry.getValue();
	        		PreparedStatement statement = getConnection().prepareStatement("SHOW COLUMNS FROM " + table + " LIKE '" + name + "'");
	        		ResultSet results = statement.executeQuery();
	        		if (!results.next()) {
	        			PreparedStatement alter = getConnection().prepareStatement("ALTER TABLE " + table + " ADD " + name + " " + type);
	        			alter.execute();
	        		}
	        	}
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
	public boolean playerExists(UUID uuid) {
		synchronized (this) {
			boolean exist = false;
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, uuid.toString());
	
				ResultSet results = statement.executeQuery();
				if (results.next()) {
					exist = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return exist;
		}
	}

	public void createPlayer(UUID uuid, String name) {
		synchronized (this) {
			open();
			try {
				PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + table + " (UUID,NAME,DISABLED_MENTION,INV_DISPLAY_LAYOUT) VALUES (?,?,?,?)");
				insert.setString(1, uuid.toString());
				insert.setString(2, name);
				insert.setBoolean(3, false);
				insert.setInt(4, InteractiveChat.invDisplayLayout);
				insert.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void save(PlayerData data) {
		synchronized (this) {
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("UPDATE " + table + " SET NAME=?, DISABLED_MENTION=?, INV_DISPLAY_LAYOUT=? WHERE UUID=?");
				statement.setString(1, data.getPlayerName());
				statement.setBoolean(2, data.isMentionDisabled());
				statement.setInt(3, data.getInventoryDisplayLayout());
				statement.setString(4, data.getUniqueId().toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public PlayerData getPlayerInfo(PlayerData data) {
		synchronized (this) {
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, data.getUniqueId().toString());
				ResultSet results = statement.executeQuery();
				results.next();
				
				String displayName = results.getString("NAME");
				Boolean disableMention = results.getBoolean("DISABLED_MENTION");
				Integer invDisplayLayout = results.getInt("INV_DISPLAY_LAYOUT");
				
				data.setPlayerName(displayName == null ? "" : displayName);
				data.setMentionDisabled(disableMention == null ? false : disableMention);
				data.setInventoryDisplayLayout(invDisplayLayout == null ? InteractiveChat.invDisplayLayout : invDisplayLayout);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public PlayerData getPlayerInfo(UUID uuid) {
		return getPlayerInfo(new PlayerData(this, uuid, "", false, 0));
	}

}
