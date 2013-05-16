Akubra (https://wiki.duraspace.org/display/AKUBRA/Akubra+Project) is a file system abstraction layer
which is used by fedora-commons (http://fedora-commons.org/)

This implementation enables fedora-commons to use a Hadoop filesystem (http://hadoop.apache.org/)
as an underlying object and datastream storage.

akubra-hdfs is still in an early development state and in no way ready for production use!

## Modifications 
* A cache feature has been added. The interface can be configured to cache specfic locations in HDFS to the local filesystem.
* Hadoop configurations are loaded from core-site.xml and hdfs-site.xml files in ```/etc/hadoop``` directory.
* Akubra-HDFS can connect to Kerberos secured Hadoop cluster.


Installation instructions (Fedora Commons 3.6.2, Hadoop 1.0.3):
---------------------------------------------------------------

### Dependencies

Copy the following dependencies to your fedora webapp's WEB-INF/lib directory:
* akubra-hdfs-0.0.1-SNAPSHOT.jar (can be found in target/ after building the project)
* hadoop-core-1.0.3.jar from $HADOOP_HOME/
* hadoop-client-1.0.3.jar from $HADOOP_HOME/
* commons-configuration-1.6.jar from $HADOOP_HOME/lib/
* commons-lang-2.4.jar from $HADOOP_HOME/lib/ 


### Configuration

Open the file ```$FEDORA_HOME/server/config/akubra-llstore.xml``` and edit the two beans ```fsObjectStore``` and ```fsDataStreamStore``` to use the class ```de.fiz.akubra.hdfs.HDFSBlobStore``` and the two beans ```fsObjectStoreMapper``` and ```fsDatastreamStoreMapper``` to be of class ```de.fiz.akubra.hdfs.HDFSIdMapper```


	<bean name="fsObjectStore" class="de.fiz.akubra.hdfs.HDFSBlobStore" singleton="true">
		<constructor-arg value="hdfs://localhost:9000/fedora/objects"/>
	</bean>
	<bean name="fsObjectStoreMapper" class="de.fiz.akubra.hdfs.HDFSIdMapper" singleton="true">
		<constructor-arg ref="fsObjectStore"/>
	</bean>

	<bean name="fsDatastreamStore" class="de.fiz.akubra.hdfs.HDFSBlobStore" singleton="true">
		<constructor-arg value="hdfs://localhost:9000/fedora/datastreams"/>
	</bean>
	<bean name="fsDatastreamStoreMapper" class="de.fiz.akubra.hdfs.HDFSIdMapper" singleton="true">
		<constructor-arg ref="fsDatastreamStore"/>
	</bean>


For using the cached version - checkout the 'develop' branch

	<bean name="fsDatastreamStore"
        class="de.fiz.akubra.hdfs.CachedHDFSBlobStore"
        singleton="true">
    	<!-- HDFS Namenode: Fedora Base URI -->
    	<constructor-arg index="0" value="hdfs://192.168.56.107:9000/fedorads/"/>
    	<!-- Local cache location -->
    	<constructor-arg index="1" value="/apps/fedora/hdfs/cache"/>
    	<!-- Pattern to cache (All file paths that end with the below specified value will be cached.) -->
    	<constructor-arg index="2" value="TN/TN.0"/>
	</bean>


Add core-site.xml and hdfs-site.xml configuration files to ```/etc/hadoop```

For kerberos enabled Hadoop clusters, the below configuration needs to be added to the hdfs-site.xml file.

	<property>
		<name>akubra.hdfs.kerberos.principal</name>
		<value>fedora@YOUR-REALM</value> <!-- Kerberos principal. -->
	</property>
	<property>
		<name>akubra.hdfs.keytab.file</name>
		<value>/etc/hadoop/fedora.keytab</value> <!-- Path to your keytab file. -->
	</property>


### License

akubra-hdfs is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
