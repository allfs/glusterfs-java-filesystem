package com.peircean.glusterfs;

import org.fusesource.glfsjni.internal.GLFS;
import org.fusesource.glfsjni.internal.structs.statvfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import static org.fusesource.glfsjni.internal.GLFS.*;

/**
 * @author <a href="http://about.me/louiszuckerman">Louis Zuckerman</a>
 */
public class GlusterFileSystemProvider extends FileSystemProvider {

    public static final String GLUSTER = "gluster";
    public static final int GLUSTERD_PORT = 24007;
    public static final String TCP = "tcp";

    @Override
    public String getScheme() {
        return GLUSTER;
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> stringMap) throws IOException {
        String authorityString = uri.getAuthority();
        String[] authority = parseAuthority(authorityString);

        String volname = authority[1];
        long volptr = glfsNew(volname);

        glfsSetVolfileServer(authority[0], volptr);

        glfsInit(authorityString, volptr);

        return new GlusterFileSystem(this, authority[0], volname, volptr);
    }

    String[] parseAuthority(String authority) {
        if (!authority.contains(":")) {
            throw new IllegalArgumentException("URI must be of the form 'gluster://server:volume/path");
        }
        String[] aarr = authority.split(":");
        if (aarr.length != 2 || aarr[0].isEmpty() || aarr[1].isEmpty()) {
            throw new IllegalArgumentException("URI must be of the form 'gluster://server:volume/path");
        }

        return aarr;
    }

    long glfsNew(String volname) {
        long volptr = glfs_new(volname);
        if (0 == volptr) {
            throw new IllegalArgumentException("Failed to create new client for volume: " + volname);
        }
        return volptr;
    }

    void glfsSetVolfileServer(String host, long volptr) {
        int setServer = glfs_set_volfile_server(volptr, TCP, host, GLUSTERD_PORT);
        if (0 != setServer) {
            throw new IllegalArgumentException("Failed to set server address: " + host);
        }
    }

    void glfsInit(String authorityString, long volptr) {
        int init = glfs_init(volptr);
        if (0 != init) {
            throw new IllegalArgumentException("Failed to initialize glusterfs client: " + authorityString);
        }
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return null;
    }

    @Override
    public Path getPath(URI uri) {
        return null;
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> openOptions, FileAttribute<?>... fileAttributes) throws IOException {
        return null;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return null;
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {

    }

    @Override
    public void delete(Path path) throws IOException {

    }

    @Override
    public void copy(Path path, Path path2, CopyOption... copyOptions) throws IOException {

    }

    @Override
    public void move(Path path, Path path2, CopyOption... copyOptions) throws IOException {

    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return false;
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    @Override
    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> vClass, LinkOption... linkOptions) {
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    @Override
    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }

    int close(long volptr) {
        return glfs_fini(volptr);
    }

    public long getTotalSpace(long volptr) throws IOException {
        statvfs buf = new statvfs();
        GLFS.glfs_statvfs(volptr, "/", buf);
        return buf.f_bsize*buf.f_blocks;
    }

    public long getUsableSpace(long volptr) throws IOException {
        throw new UnsupportedOperationException();
    }

    public long getUnallocatedSpace(long volptr) throws IOException {
        throw new UnsupportedOperationException();
    }
}
