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
import org.apache.hadoop.security.SecurityUtil;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BlobStore} implementation for the Hadoop filesystem.
 * 
 * @author frank asseg
 * 
 */
public class HDFSBlobStore implements BlobStore {
    private  FileSystem hdfs;

    private final URI id;
    
    private static final Logger log = LoggerFactory.getLogger(HDFSBlobStore.class);

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
            File coreSite = new File("/etc/hadoop/core-site.xml");
            File hdfsSite = new File("/etc/hadoop/hdfs-site.xml");
            if(coreSite.exists() && hdfsSite.exists()) {
              conf.clear();
              conf.addResource(new Path(coreSite.getPath()));
              conf.addResource(new Path(hdfsSite.getPath()));
              log.info("Using /etc/hadoop as configuration dir.");
            } else {
              log.warn("Using default hadoop configuration! Add core-site.xml and hdfs-site.xml to /etc/hadoop directory.");
            }
            if(conf.get("hadoop.security.authentication").equals("kerberos")) {
              UserGroupInformation.setConfiguration(conf);
              SecurityUtil.login(conf, "akubra.hdfs.keytab.file", "akubra.hdfs.kerberos.principal");
              log.info("Login success for principal " + conf.get("akubra.hdfs.kerberos.principal") + " using keytab " + conf.get("akubra.hdfs.keytab.file"));
            }
            hdfs=FileSystem.get(this.id, conf);
        }
        return hdfs;
    }
}
