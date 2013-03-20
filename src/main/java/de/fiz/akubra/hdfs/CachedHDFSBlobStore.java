package de.fiz.akubra.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.transaction.Transaction;

import org.akubraproject.BlobStoreConnection;

/**
 * This is an extension of {@link HDFSBlobStore}. This creates a 
 * {@link CachedHDFSBlobStoreConnection} instead of usual 
 * {@link BlobStoreConnection}.
 * 
 * The parameters specified in the akubra-llstore.xml are passed
 * on to the {@link CachedHDFSBlobStoreConnection} initialization.
 * 
 * @author mohideen
 *
 */

public class CachedHDFSBlobStore extends HDFSBlobStore {
  
  private String cacheBase;
  private String pathToCache;

  public CachedHDFSBlobStore(URI namenodeURI) {
    super(namenodeURI);
  }
  
  public CachedHDFSBlobStore(URI namenodeURI, String cacheBase) {
    super(namenodeURI);
    this.cacheBase = cacheBase;
  }
  
  public CachedHDFSBlobStore(URI namenodeURI, String cacheBase, String pathToCache) {
    super(namenodeURI);
    this.cacheBase = cacheBase;
    this.pathToCache = pathToCache;
  }

  @Override
  public BlobStoreConnection openConnection(Transaction tx,
      Map<String, String> hints) throws UnsupportedOperationException,
      IOException {
    if (tx != null) {
      throw new UnsupportedOperationException("Transactions are not supported");
    }
    if(cacheBase != null) {
      if(pathToCache != null) {
        return (BlobStoreConnection)(new CachedHDFSBlobStoreConnection(this, cacheBase, pathToCache));
      }
      return (BlobStoreConnection)(new CachedHDFSBlobStoreConnection(this, cacheBase));
    }
    return (BlobStoreConnection)(new CachedHDFSBlobStoreConnection(this));
  }
  
  

}
