package cat.jaffa.multitwitchwhitelist.forge;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jordan on 05/07/2017.
 */
public class WhitelistData {

    public static class User {
        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayname() {
            return displayname;
        }

        public boolean isGlobaladmin() {
            return globaladmin;
        }

        public Date getAccountcreation() {
            return accountcreation;
        }

        private int id;
        private String username;
        private String displayname;
        private boolean globaladmin;
        private Date accountcreation;
    }

    public class Ban {
        public int getBanid() {
            return banid;
        }

        public int getTwitchid() {
            return twitchid;
        }

        public UUID getMcuuid() {
            return mcuuid;
        }

        public String getReason() {
            return reason;
        }

        public Timestamp getCreated() {
            return created;
        }

        public Timestamp getExpiry() {
            return expiry;
        }

        public int getInvoker() {
            return invoker;
        }

        public String getInvokerUsername() {
            return invokerUsername;
        }

        public String getInvokerDisplayname() {
            return invokerDisplayname;
        }

        public boolean isGlobal() {
            return global;
        }

        private int banid;
        private int twitchid;
        private UUID mcuuid;
        private String reason;
        private Timestamp created;
        private Timestamp expiry;
        private int invoker;
        private String invokerUsername;
        private String invokerDisplayname;
        private boolean global;
    }

    public class Sub {
        private Timestamp created_at;
        private String _id;
        private String sub_plan;
        private String sub_plan_name;
        private int id;
        private Timestamp lastupdate;

        public Timestamp getCreated_at() {
            return created_at;
        }

        public String get_id() {
            return _id;
        }

        public String getSub_plan() {
            return sub_plan;
        }

        public String getSub_plan_name() {
            return sub_plan_name;
        }

        public int getId() {
            return id;
        }

        public Timestamp getLastupdate() {
            return lastupdate;
        }
    }

    private User user;
    public User getUser() {
        return user;
    }

    public List<Ban> getBans() {
        return bans;
    }
    private List<Ban> bans;

    private List<Sub> subs;
    public List<Sub> getSubs() {
        return subs;
    }

    private Boolean subbed;
    public Boolean isSubbed() {
        return subbed;
    }

    private Boolean banned;
    public Boolean isBanned() {
        return banned;
    }

    private Boolean manual;
    public Boolean isManual() {
        return manual;
    }

    private Boolean whitelisted;
    public Boolean isWhitelisted() { return whitelisted;}

    private int statusCode = 500;
    protected int getStatusCode() {
        return statusCode;
    }
    protected void setStatusCode(int code) {
        statusCode = code;
    }
}
