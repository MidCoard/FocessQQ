package top.focess.qq.api.util.version;

/**
 * Represents a version of a plugin.
 */
public class Version {
    /**
     * Represents an alpha version of a plugin.
     */
    public static final Version ALPHA_VERSION = new Version("alpha");

    /**
     * Represents a beta version of a plugin.
     */
    public static final Version BETA_VERSION = new Version("beta");

    /**
     * Represents a build version of a plugin.
     */
    public static final Version BUILD_VERSION = new Version("build");

    /**
     * Represents a default release version of a plugin.
     */
    public static final Version DEFAULT_VERSION = new Version("1.0.0");
    private final int length;
    /**
     * The major version
     */
    private int major;
    /**
     * The minor version
     */
    private int minor;
    /**
     * The revision version
     */
    private int revision;
    /**
     * The build version
     */
    private String build;

    /**
     * Constructs a new version with the specified version numbers and build.
     *
     * @param major    the major version number
     * @param minor    the minor version number
     * @param revision the revision version number
     * @param build    the build version
     */
    public Version(final int major, final int minor, final int revision, final String build) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.build = build;
        this.length = 4;
    }

    /**
     * Constructs a new version with the specified version numbers.
     *
     * @param major    the major version number
     * @param minor    the minor version number
     * @param revision the revision version number
     */
    public Version(final int major, final int minor, final int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.length = 3;
    }

    /**
     * Constructs a new version with the specified version numbers.
     *
     * @param major the major version number
     * @param minor the minor version number
     */
    public Version(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
        this.length = 2;
    }

    /**
     * Constructs a new version with the specified version.
     *
     * @param version the version to be parsed.
     */
    public Version(final String version) {
        final String[] temp = version.split("\\.");
        try {
            if (temp.length == 1)
                this.build = temp[0];
            else if (temp.length == 2) {
                this.major = Integer.parseInt(temp[0]);
                final String[] temp2 = temp[1].split("-");
                this.minor = Integer.parseInt(temp2[0]);
                if (temp2.length == 2)
                    this.build = temp2[1];
                else if (temp2.length > 2) throw new VersionFormatException(version);
            } else if (temp.length == 3) {
                this.major = Integer.parseInt(temp[0]);
                this.minor = Integer.parseInt(temp[1]);
                final String[] temp2 = temp[2].split("-");
                this.revision = Integer.parseInt(temp2[0]);
                if (temp2.length == 2)
                    this.build = temp2[1];
                else if (temp2.length > 2) throw new VersionFormatException(version);
            } else if (temp.length == 4) {
                this.major = Integer.parseInt(temp[0]);
                this.minor = Integer.parseInt(temp[1]);
                this.revision = Integer.parseInt(temp[2]);
                this.build = temp[3];
            } else throw new VersionFormatException(version);
        } catch (final NumberFormatException e) {
            throw new VersionFormatException(version);
        }
        this.length = temp.length;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getRevision() {
        return this.revision;
    }

    public String getBuild() {
        return this.build;
    }

    @Override
    public String toString() {
        if (this.length == 1)
            return this.build;
        else if (this.length == 2)
            return this.major + "." + this.minor + (this.build == null ? "" : "-" + this.build);
        else if (this.length == 3)
            return this.major + "." + this.minor + "." + this.revision + (this.build == null ? "" : "-" + this.build);
        else if (this.length == 4)
            return this.major + "." + this.minor + "." + this.revision + "." + this.build;
        throw new VersionFormatException("");
    }
}
