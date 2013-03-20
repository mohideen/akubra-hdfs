package de.fiz.akubra.hdfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.akubraproject.Blob;
import org.akubraproject.UnsupportedIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * An extension of the {@link HDFSBlobStoreConnection} that uses 
 * {@link CachedHDFSBlob} to serve requests for files whose paths ends
 * with {@literal pathToCache} value.
 * 
 * The {@literal pathToCache} and {@literal cacheBase} value should be 
 * specified in the akubra-llstore.xml file.
 * 
 * 
 * @author mohideen
 *
 */

public class CachedHDFSBlobStoreConnection extends HDFSBlobStoreConnection {
  
  private final HDFSBlobStore store;
  private static final Logger log = LoggerFactory.getLogger(CachedHDFSBlobStoreConnection.class);
  
  public final String cacheBase;
  public final String pathToCache;
  final HDFSBlobCache cache;

  public CachedHDFSBlobStoreConnection(HDFSBlobStore store) throws IOException {
    super(store);
    this.store = store;
    this.cacheBase = "/apps/fedora/hdfs/cache";
    this.pathToCache = "TN/TN.0";
    File cacheBaseFile = new File(cacheBase);
    this.cache = new HDFSBlobCache(cacheBaseFile);
  }
  
  public CachedHDFSBlobStoreConnection(HDFSBlobStore store, String cacheBase) throws IOException {
    super(store);
    this.store = store;
    this.cacheBase = cacheBase;
    this.pathToCache = "TN/TN.0";
    File cacheBaseFile = new File(cacheBase);
    this.cache = new HDFSBlobCache(cacheBaseFile);
  }
  
  public CachedHDFSBlobStoreConnection(HDFSBlobStore store, String cacheBase, String pathToCache) throws IOException {
    super(store);
    this.store = store;
    this.cacheBase = cacheBase;
    this.pathToCache = pathToCache;
    
    File cacheBaseFile = new File(cacheBase);
    this.cache = new HDFSBlobCache(cacheBaseFile);
  }
  
  @Override
  public Blob getBlob(final URI uri, final Map<String, String> hints) throws UnsupportedIdException, IOException {
    if (isClosed()){
        throw new IllegalStateException("Connection to hdfs is closed");
    }
    if (uri == null) {
        URI tmp = URI.create(store.getId() + UUID.randomUUID().toString());
        log.debug("creating new Blob uri " + tmp.toASCIIString());
        // return getBlob(new ByteArrayInputStream(new byte[0]),0, null);
        return new HDFSBlob(tmp, this);
    }
    log.debug("fetching blob " + uri);
    if (uri.getRawSchemeSpecificPart().startsWith("info:")) {
        log.debug("special object " + uri);
    }
    if (!uri.toASCIIString().startsWith("hdfs:")) {
        throw new UnsupportedIdException(uri, "HDFS URIs have to start with 'hdfs:'");
    }
    if(!cache.hasHDFSConnection()) {
      cache.setHDFSConnection(this);
    }
    //create non-cached hdfs blob object
    HDFSBlob blob = new HDFSBlob(uri, this);
    boolean isThumbnail = uri.toString().endsWith(pathToCache);
    if(isThumbnail /* return from cache*/) {
      if(!cache.contains(uri.toString())) {
        //if key does not exists in cache, do
        if(blob.exists()) {

          //if blob exists in HDFS, add blob cache & return from cache
          cache.put(uri.toString(), blob);
          blob = (HDFSBlob)cache.get(uri.toString());
        }
      } else {
        //return blob from cache
        blob = (HDFSBlob)cache.get(uri.toString());
      }
    }
    return blob;
}

}

  