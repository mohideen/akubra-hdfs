package de.fiz.akubra.hdfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.akubraproject.MissingBlobException;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extension of {@link HDFSBlob}. This blob tries to serve
 * the read request from the cache whenever possible. If the requested file 
 * is not found in the cache, the requested is served directly from HDFS.
 * 
 * @author mohideen
 *
 */

public class CachedHDFSBlob extends HDFSBlob {
  
  private final CachedHDFSBlobStoreConnection conn;
  private Path path;
  private URI uri;
 
  private static final Logger log = LoggerFactory.getLogger(CachedHDFSBlob.class);
  
  
  public CachedHDFSBlob(URI uri, CachedHDFSBlobStoreConnection conn) {
    super(uri, conn);
    this.conn = conn;
    this.uri = uri;
    this.path = new Path(this.uri.toASCIIString());
    log.debug("opening blob " + uri.toASCIIString() + " at " + this.path.toString());
  }
  
  
  @Override
  public InputStream openInputStream() throws IOException, MissingBlobException {
    if (this.conn.isClosed()) {
        throw new IllegalStateException("Unable to open Inputstream, because connection is closed");
    }
    try {
      try {
        //return from cache
        return conn.cache.getInputStream(uri.toString());
      } catch(FileNotFoundException e) {
        //if return from cache did not succeed, return from HDFS.
        return this.conn.getFileSystem().open(path);
      }
    } catch (FileNotFoundException e) {
        throw new MissingBlobException(uri, e.getLocalizedMessage());
    }
  }


  @Override
  public void delete() throws IOException {
    //Remove from cache
    conn.cache.clear(uri.toString());
    //Delete from HDFS
    super.delete();
  }

}
