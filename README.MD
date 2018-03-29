# DB Migrator


Usage:

Add to pom

```
<plugins>
<plugin>
<groupId>com.thoughtworks.dbmigrator</groupId>
<artifactId>dbmigrator-maven-plugin</artifactId>
<version>1.0-SNAPSHOT</version>
<inherited>false</inherited>
<configuration>
<pathToMigrations>/Users/syedatifakhtar/tmp</pathToMigrations>
<changeLogTableName>changelog</changeLogTableName>
<jdbcUrl>jdbc:mysql://localhost:3306/database</jdbcUrl>
<username>root</username>
<password>root123</password>
</configuration>
</plugin>
</plugins>
```

Migrations should be of the format <UnixTimestamp>#<Name>.sql where unixtimestamp is the timestamp when you created the migration file

Eg-:

1522306751#ADDTABLEBLAH.sql

Migrations should be idempotent for easy fallback

to run

```mvn dbmigrator:migrate <params>```

To debug ```mvnDebug dbmigrator:migrate```
