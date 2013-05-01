package de.fiz.akubra.hdfs;

/*
 * Copyright (C) 2011-2012 MIT Mobile Experience Lab
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.akubraproject.MissingBlobException;
import org.apache.commons.io.IOUtils;

import edu.umd.lib.hadoop.filecache.DiskCache;


/**
 * 
 * A cache for Akubra HDFSBlobs. The blobs are stored in the local 
 * filesystem (local filesystem cache-location should be specified 
 * in the akubra-llstore.xml file). 
 * 
 * This is an extenstion of the {@link DiskCache}.
 *
 * @author mohideen
 *
 */
public class HDFSBlobCache extends DiskCache<String, HDFSBlob> {
  
  private CachedHDFSBlobStoreConnection conn;
  private static HDFSBlobCache _self;

  public HDFSBlobCache(File cacheBase) {
    super(cacheBase);
    if(!cacheBase.exists()) {
      cacheBase.mkdirs();
    }
    _self = this;
  }
  
  public static HDFSBlobCache getHDFSBlobCache() {
    return _self;
  }
  
  public boolean hasHDFSConnection() {
    return conn==null?false:true;
  }

  public void setHDFSConnection(CachedHDFSBlobStoreConnection conn) {
    this.conn = conn;
  }

  @Override
  protected void toDisk(String key, HDFSBlob in, OutputStream out) {
    try {
      IOUtils.copy(in.openInputStream(), out);
    } catch (MissingBlobException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
  }

  @Override
  protected HDFSBlob fromDisk(String key, InputStream in) {
    URI uri;
    try {
      uri = new URI(key);
      if(uri != null) {
        HDFSBlob blob = new CachedHDFSBlob(uri, conn);
        return blob;
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public InputStream getInputStream(String key) throws FileNotFoundException {
    final File readFrom = getFile(key);
    final InputStream is = new FileInputStream(readFrom);
    return is;
  }
    
  
}
