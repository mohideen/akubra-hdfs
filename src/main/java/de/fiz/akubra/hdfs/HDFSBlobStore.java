/*
   Copyright 2011 FIZ Karlsruhe 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package de.fiz.akubra.hdfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.transaction.Transaction;

import org.akubraproject.BlobStore;
import org.akubraproject.BlobStoreConnection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * {@link BlobStore} implementation for the Hadoop filesystem.
 * 
 * @author frank asseg
 * 
 */
public class HDFSBlobStore implements BlobStore {
    private  FileSystem hdfs;

    private final URI id;

    /**
     * create a new {@link HDFSBlobStore} at a specific URI in {@link String}
     * format
     * 
     * @param id
     *            the {@link URI} pointing to the HDFS namenode
     * @throws URISyntaxException
     *             if the supplied {@link URI} was not valid
     */
    public HDFSBlobStore(final URI uri) {
        this.id = uri;
    }

    /**
     * get the id
     * 
     * @return this {@link HDFSBlobStore}'s id
     */
    public URI getId() {
        return id;
    }

    /**
     * open a new {@link HDFSBlobStoreConnection} to a HDFS namenode
     * 
     * @param tx
     *            since transactions are not supported. this must be set to null
     * @param hints
     *            not used
     * @return a new {@link HDFSBlobStoreConnection} th this
     *         {@link HDFSBlobStore}'s id
     * @throws UnsupportedOperationException
     *             if the transaction parameter was not null
     * @throws IOException
     *             if the operation did not succeed
     */
    public BlobStoreConnection openConnection(final Transaction tx, final Map<String, String> hints) throws UnsupportedOperationException, IOException {
        if (tx != null) {
            throw new UnsupportedOperationException("Transactions are not supported");
        }
        return new HDFSBlobStoreConnection(this);
    }

    synchronized FileSystem getFilesystem() throws IOException {
        if (hdfs==null){
            Configuration conf = new Configuration();
            Configuration clientConf = new Configuration();
            String fedoraHome = System.getenv("FEDORA_HOME");
            if(fedoraHome != null) {
              File confFile = new File(fedoraHome + "/server/config/akubra-hdfs-site.xml");
              if(confFile.exists()) {
                clientConf.addResource(new Path(confFile.getPath()));
              }
            }
            conf.setInt("dfs.replication", clientConf.getInt("dfs.replication", 3));
            conf.setInt("dfs.block.size", clientConf.getInt("dfs.block.size", 128 * 1024 * 1024)); 
            conf.setInt("dfs.blocksize", clientConf.getInt("dfs.blocksize", 128 * 1024 * 1024)); 
            hdfs = FileSystem.get(this.id, conf);
        }
        return hdfs;
    }
}
