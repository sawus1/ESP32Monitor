package SystemInfo;

public class OS {
    private String name;
    private String version;
    private String id;
    private String id_like;
    private String pretty_name;
    private String version_id;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getId_like() { return id_like; }
    public void setId_like(String id_like) { this.id_like = id_like; }

    public String getPretty_name() { return pretty_name; }
    public void setPretty_name(String pretty_name) { this.pretty_name = pretty_name; }

    public String getVersion_id() { return version_id; }
    public void setVersion_id(String version_id) { this.version_id = version_id; }
}

